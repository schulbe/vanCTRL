import threading
import time

import numpy as np
from BluetoothController import BluetoothController
# from BluetoothController import logger as bt_logger
from GpioController import GpioController
# from GpioController import logger as gpio_logger
import configparser
import sys
import os
import logging
import sqlite3
from datetime import datetime
from contextlib import suppress

from RadioController import RadioController

SENDING_POWER_MEASUREMENTS = False
SENDING_TEMPERATURE_MEASUREMENTS = False


class Processor:
    def __init__(self, conf):
        self.config = conf
        self.codes = config['CODES']

        logger.info("Initializing Radio Controller")
        # TODO: PIN 25 should not be hardcoded!!!
        self.radio_controller = RadioController(25)

        logger.info("Initializing GPIO Controll")
        self.gpio_controller = GpioController(config=config)

        logger.info("Initializing Database")
        self.initialize_database()

        logger.info("Initializing BT Controll")
        self.bt_controller = BluetoothController(uuid=config['GENERAL']['UUID'])

    def run_main_loop(self):
        logger.info("Starting regular logger...")
        self.schedule_measurement_logger(config.getint('GENERAL', 'LOGGING_UPDATE_SEC'))

        logger.info("Starting connect_and_listen loop...")
        self.bt_controller.connect_and_listen(callback=self.process_message)

    def process_message_error_wrapper(self, msg, lock):
        try:
            self.process_message(msg, lock)
        except Exception as e:
            logger.error(f'Error in "process_message: {e}', exc_info=True)

    def process_message(self, msg, lock):
        logger.debug(f'Received Message: {msg}')

        msg_flag, msg_type, msg_details = msg.split('\u0003')

        # IN CASE COMMAND WAS RECEIVED
        if msg_flag == self.codes['COMMAND_FLAG']:
            if msg_type == self.codes['CMD_SWITCH_ON']:
                with suppress(IndexError):
                    switch = [k for k, v in self.codes.items() if v == msg_details][0]
                    with lock:
                        self.gpio_controller.switch(switch, on=True)

            elif msg_type == self.codes['CMD_SWITCH_OFF']:
                with suppress(IndexError):
                    switch = [k for k, v in self.codes.items() if v == msg_details][0]
                    with lock:
                        self.gpio_controller.switch(switch, on=False)

            elif msg_type == self.codes['CMD_SWITCH_TOGGLE']:
                with suppress(IndexError):
                    switch = [k for k, v in self.codes.items() if v == msg_details][0]
                    with lock:
                        if self.gpio_controller.switch_is_on(switch):
                            self.gpio_controller.switch(switch, on=False)
                        else:
                            self.gpio_controller.switch(switch, on=True)

            elif msg_type == self.codes['CMD_SEND_DATA']:
                global SENDING_POWER_MEASUREMENTS
                global SENDING_TEMPERATURE_MEASUREMENTS

                if msg_details == self.codes['DATA_POWER_MEASUREMENTS']:
                    global SENDING_POWER_MEASUREMENTS

                    if not SENDING_POWER_MEASUREMENTS:
                        SENDING_POWER_MEASUREMENTS = True
                        t = datetime.now()
                        self.send_power_measurements(lock)
                        SENDING_POWER_MEASUREMENTS = False
                        logger.debug(f"Send Power Took: {(datetime.now() - t).total_seconds()}s")

                elif msg_details == self.codes['DATA_TEMPERATURE_MEASUREMENTS']:
                    global SENDING_TEMPERATURE_MEASUREMENTS

                    if not SENDING_TEMPERATURE_MEASUREMENTS:
                        SENDING_TEMPERATURE_MEASUREMENTS = True
                        t = datetime.now()
                        self.send_temperature_measurements(lock)
                        SENDING_TEMPERATURE_MEASUREMENTS = False
                        logger.debug(f"Send Temperature Took: {(datetime.now() - t).total_seconds()}s")

                elif msg_details == self.codes['DATA_SWITCH_STATUS']:
                    self.send_switch_status(lock)

                elif msg_details.startswith(f"{self.codes['DATA_MEASUREMENT_HISTORY']}\u0004"):
                    since = msg_details.split("\u0004")[1]
                    self.send_measurement_history(since)

        elif msg_flag == self.codes['DATA_FLAG']:
            logger.debug(f"{msg} is a DATA message")
            if msg_type == self.codes['DATA_INPUT_SPECS']:
                specs = msg_details.split('\u0004')
                #todo: try except
                inp = [k for k, v in self.codes.items() if v == specs[0]][0]
                if inp in self.gpio_controller.power_inputs:
                    self.gpio_controller.update_power_measurement_mapping_entry(inp, *specs[1:])
                elif inp in self.gpio_controller.temperature_inputs:
                    self.gpio_controller.temp_measurement_mapping[inp]['id'] = specs[1]

            elif msg_type == self.codes['DATA_RADIO']:
                logger.debug(f'Should send "{msg_details}" to radio')
                self.radio_controller._send_code(msg_details)

    def send_switch_status(self, lock):
        s = {self.codes[switch_name]: int(status) for switch_name, status in self.gpio_controller.get_switch_status().items()}
        status_string = "\u0004".join([str(i[1]) for i in sorted(s.items())])
        msg = f'\u0002{self.codes["DATA_FLAG"]}' \
              f'\u0003{self.codes["DATA_SWITCH_STATUS"]}' \
              f'\u0003{status_string}\u0002'
        # with lock:
        self.bt_controller.send(msg)

    def send_power_measurements(self, lock):
        s = list()
        for inp in self.gpio_controller.power_inputs:
            try:
                I, U = self.gpio_controller.get_power_measurements(inp)
            except Exception as e:
                logger.error(f"Error when getting measurements for  Inut {inp}: {e}", exc_info=True)
                I, U = (0, 0)
            s.extend([I, U])
        meas_string = '\u0004'.join("{0:.2f}".format(v) for v in s)
        msg = f'\u0002{self.codes["DATA_FLAG"]}' \
              f'\u0003{self.codes["DATA_POWER_MEASUREMENTS"]}' \
              f'\u0003{meas_string}\u0002'
        # with lock:
        self.bt_controller.send(msg)

    def send_temperature_measurements(self, lock):
        s = list()
        for inp in self.gpio_controller.temperature_inputs:
            try:
                temp = self.gpio_controller.get_temperature_measurement(inp)
            except Exception as e:
                logger.error(f"Error when getting measurements for  Input {inp}: {e}", exc_info=True)
                temp = -100
            s.append(temp)
        meas_string = '\u0004'.join("{0:.2f}".format(v) for v in s)
        msg = f'\u0002{self.codes["DATA_FLAG"]}' \
              f'\u0003{self.codes["DATA_TEMPERATURE_MEASUREMENTS"]}' \
              f'\u0003{meas_string}\u0002'
        # with lock:
        self.bt_controller.send(msg)

    def send_measurement_history(self, since):
        sql_data = f"""SELECT IN_1_A , IN_1_U, IN_2_A, IN_2_U, IN_3_A, IN_3_U, IN_4, IN_5, TIME 
                      FROM {config['GENERAL']['MEASUREMENT_TABLE']}
                      WHERE TIME > {since}"""
        con = sqlite3.connect(self.config['GENERAL']['DB_NAME'])
        c = con.cursor()
        c.execute(sql_data)
        data = c.fetchall()
        for history in data:
            self.send_measurement_history_entry(history=history)

    def send_measurement_history_entry(self, history=None):
        if history is None:
            sql_last = f"SELECT IN_1_A , IN_1_U, IN_2_A, IN_2_U, IN_3_A, IN_3_U, IN_4, IN_5, MAX(TIME) FROM {config['GENERAL']['MEASUREMENT_TABLE']};"
            con = sqlite3.connect(self.config['GENERAL']['DB_NAME'])
            c = con.cursor()
            c.execute(sql_last)
            history = c.fetchall()
        try:
            meas_string = '\u0004'.join(history)
            msg = f'\u0002{self.codes["DATA_FLAG"]}' \
                  f'\u0003{self.codes["DATA_MEASUREMENT_HISTORY"]}' \
                  f'\u0003{meas_string}\u0002'
            # with self.bt_controller._lock:
            self.bt_controller.send(msg)
        except Exception as e:
            print('EXCEPTION IN LINE 177: e')
            print(f'history: {history}')

    def schedule_measurement_logger(self, schedule_s):
        t = threading.Thread(target=self._schedule_measurement_logger, args=(schedule_s,))
        t.setDaemon(True)
        t.start()

    def _schedule_measurement_logger(self, schedule_s):
        db_connection = sqlite3.connect(self.config['GENERAL']['DB_NAME'])
        insert_measurements_sql = f"""INSERT INTO {config['GENERAL']['MEASUREMENT_TABLE']}
                                              (IN_1_A , IN_1_U, IN_2_A, IN_2_U, IN_3_A, IN_3_U, IN_4, IN_5)
                                              VALUES (?,?,?,?,?,?,?,?)"""
        try:
            while True:
                measurements_glob = list()
                min_schedule = schedule_s/5
                for i in range(10):

                    measurements = list()
                    t = datetime.now()
                    for inp in ['IN_1', 'IN_2', 'IN_3']:
                        measurements.extend(self.gpio_controller.get_power_measurements(inp))

                    for inp in ['IN_4', 'IN_5']:
                        measurements.append(self.gpio_controller.get_temperature_measurement(inp))

                    measurements_glob.append(measurements)
                    time.sleep(min_schedule-(datetime.now()-t).total_seconds())

                measurements_mean = ["{0:.2f}".format(m) for m in np.mean(measurements_glob, axis=0)]
                db_connection.cursor().execute(insert_measurements_sql, measurements_mean)
                db_connection.commit()

                if self.bt_controller.is_connected():
                    t = threading.Thread(target=self.send_measurement_history_entry())
                    t.setDaemon(True)
                    t.start()
        finally:
            db_connection.close()

    def initialize_database(self):
        db_connection = sqlite3.connect(self.config['GENERAL']['DB_NAME'])
        create_measurements_table_sql = """CREATE TABLE IF NOT EXISTS {tbl} 
                                          (IN_1_A FLOAT,
                                           IN_1_U FLOAT,
                                           IN_2_A FLOAT,
                                           IN_2_U FLOAT,
                                           IN_3_A FLOAT,
                                           IN_3_U FLOAT,
                                           IN_4 FLOAT,
                                           IN_5 FLOAT,
                                           TIME TIMESTAMP DEFAULT (strftime('%s', 'now')))""".format(tbl=config['GENERAL']['MEASUREMENT_TABLE'])

        db_connection.cursor().execute(create_measurements_table_sql)
        db_connection.commit()
        db_connection.close()


if __name__ == '__main__':

    #todo execute somewehere else: turnoff leds
    os.system("sudo bash -c \"echo none > /sys/class/leds/led0/trigger\"")
    os.system("sudo bash -c \"echo none > /sys/class/leds/led1/trigger\"")
    os.system("sudo bash -c \"echo 0 > /sys/class/leds/led1/brightness\"")
    os.system("sudo bash -c \"echo 0 > /sys/class/leds/led1/brightness\"")

    config = configparser.ConfigParser()
    config.optionxform = str

    config.read('/home/pi/repos/vancontrol/raspi/config.ini')

    config.read('/home/pi/repos/vancontrol/input_specifications.ini')

    formatter = logging.Formatter("%(asctime)s;%(levelname)s;%(message)s",
                                  "%Y-%m-%d %H:%M:%S")

    fileHandler = logging.FileHandler(f'/home/pi/logs/{datetime.now().strftime("%Y-%m-%d_%H-%M-%S")}.log')
    fileHandler.setLevel(logging.DEBUG)
    fileHandler.setFormatter(formatter)

    streamHandler = logging.StreamHandler(sys.stderr)
    streamHandler.setLevel(logging.DEBUG)
    streamHandler.setFormatter(formatter)

    gpio_logger = logging.getLogger('GpioController')
    gpio_logger.setLevel(logging.DEBUG)
    gpio_logger.handlers = [fileHandler, streamHandler]

    bt_logger = logging.getLogger('BluetoothController')
    bt_logger.setLevel(logging.DEBUG)
    bt_logger.handlers = [fileHandler, streamHandler]

    logger = logging.getLogger(__name__)
    logger.setLevel(logging.DEBUG)
    logger.handlers = [fileHandler, streamHandler]

    processor = Processor(config)
    processor.run_main_loop()

