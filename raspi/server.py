from BluetoothController import BluetoothController
from GpioController import GpioController
import configparser
import sys
import logging
from datetime import datetime
from contextlib import suppress

SENDING_POWER_MEASUREMENTS = False
SENDING_TEMPERATURE_MEASUREMENTS = False

class Processor:
    def __init__(self, conf):
        self.config = conf
        self.codes = config['CODES']

        logging.info("Initializing GPIO Controll")
        self.gpio_controller = GpioController(config=config)

        logging.info("Initializing BT Controll")
        self.bt_controller = BluetoothController(uuid=config['GENERAL']['UUID'])

    def run_main_loop(self):
        logging.info("Starting connect_and_listen loop...")
        self.bt_controller.connect_and_listen(callback=self.process_message)

    def process_message_error_wrapper(self, msg, lock):
        try:
            self.process_message(msg, lock)
        except Exception as e:
            logging.error(f'Error in "process_message: {e}', exc_info=True)

    def process_message(self, msg, lock):
        logging.debug(f'Received Message: {msg}')

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
                        logging.debug(f"Send Power Took: {(datetime.now() - t).total_seconds()}s")

                elif msg_details == self.codes['DATA_TEMPERATURE_MEASUREMENTS']:
                    global SENDING_TEMPERATURE_MEASUREMENTS

                    if not SENDING_TEMPERATURE_MEASUREMENTS:
                        SENDING_TEMPERATURE_MEASUREMENTS = True
                        t = datetime.now()
                        self.send_temperature_measurements(lock)
                        SENDING_TEMPERATURE_MEASUREMENTS = False
                        logging.debug(f"Send Temperature Took: {(datetime.now() - t).total_seconds()}s")

                elif msg_details == self.codes['DATA_SWITCH_STATUS']:
                    self.send_switch_status(lock)

        elif msg_flag == self.codes['DATA_FLAG']:
            logging.debug(f"{msg} is a DATA message")
            pass

    def send_switch_status(self, lock):
        s = {self.codes[switch_name]: int(status) for switch_name, status in self.gpio_controller.get_switch_status().items()}
        status_string = "\u0004".join([str(i[1]) for i in sorted(s.items())])
        msg = f'\u0002{self.codes["DATA_FLAG"]}' \
              f'\u0003{self.codes["DATA_SWITCH_STATUS"]}' \
              f'\u0003{status_string}\u0002'
        with lock:
            self.bt_controller.send(msg)

    def send_power_measurements(self, lock):
        s = list()
        for inp in self.gpio_controller.power_inputs:
            try:
                U, I = self.gpio_controller.get_power_measurements(inp)
            except Exception as e:
                logging.error(f"Error when getting measurements for  Inut {inp}: {e}", exc_info=True)
                U, I = (0, 0)
            s.extend([I, U])
        meas_string = '\u0004'.join(str(v) for v in s)
        msg = f'\u0002{self.codes["DATA_FLAG"]}' \
              f'\u0003{self.codes["DATA_POWER_MEASUREMENTS"]}' \
              f'\u0003{meas_string}\u0002'
        with lock:
            self.bt_controller.send(msg)

    def send_temperature_measurements(self, lock):
        s = list()
        for inp in self.gpio_controller.temperature_inputs:
            try:
                temp = self.gpio_controller.get_temperature_measurement(inp)
            except Exception as e:
                logging.error(f"Error when getting measurements for  Inut {inp}: {e}", exc_info=True)
                temp = -100
            s.append(temp)
        meas_string = '\u0004'.join(str(v) for v in s)
        msg = f'\u0002{self.codes["DATA_FLAG"]}' \
              f'\u0003{self.codes["DATA_TEMPERATURE_MEASUREMENTS"]}' \
              f'\u0003{meas_string}\u0002'
        with lock:
            self.bt_controller.send(msg)


if __name__ == '__main__':
    config = configparser.ConfigParser()
    config.optionxform = str

    config.read('/home/pi/repos/vancontrol/raspi/config.ini')

    config.read('/home/pi/repos/vancontrol/input_specifications.ini')

    logger = logging.getLogger()
    logger.setLevel(logging.DEBUG)

    formatter = logging.Formatter("%(asctime)s;%(levelname)s;%(message)s",
                                  "%Y-%m-%d %H:%M:%S")

    fileHandler = logging.FileHandler(f'/home/pi/logs/{datetime.now().strftime("%Y-%m-%d_%H-%M-%S")}.log')
    fileHandler.setLevel(logging.DEBUG)
    fileHandler.setFormatter(formatter)

    streamHandler = logging.StreamHandler(sys.stderr)
    streamHandler.setLevel(logging.DEBUG)
    streamHandler.setFormatter(formatter)

    logger.addHandler(fileHandler)
    logger.addHandler(streamHandler)

    processor = Processor(config)
    processor.run_main_loop()

