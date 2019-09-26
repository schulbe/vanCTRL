from BluetoothController import BluetoothController
from GpioController import GpioController
import configparser
import os
import logging
import time
from datetime import datetime
from contextlib import suppress

send_statistics = False


class Processor:
    def __init__(self, conf):
        self.config = conf
        self.codes = config['CODES']

        self.dt = 1/int(conf['GENERAL']['updates_per_second'])

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
        global send_statistics
        logging.debug(f'Received Message {msg} (type{type(msg)})')

        msg_flag, msg_type, msg_details = msg.split('\u0003')

        # IN CASE COMMAND WAS RECEIVED
        if msg_flag == self.codes['COMMAND_FLAG']:
            if msg_type == self.codes['CMD_SWITCH_ON']:
                with suppress(IndexError):
                    switch = [k for k, v in self.codes if v == msg_details][0]
                    with lock:
                        self.gpio_controller.switch(switch, on=True)

            elif msg_type == self.codes['CMD_SWITCH_OFF']:
                with suppress(IndexError):
                    switch = [k for k,v in self.codes if v == msg_details][0]
                    with lock:
                        self.gpio_controller.switch(switch, on=False)

            elif msg_type == self.codes['CMD_SWITCH_TOGGLE']:
                with suppress(IndexError):
                    switch = [k for k, v in self.codes if v == msg_details][0]
                    with lock:
                        if self.gpio_controller.switch_is_on(switch):
                            self.gpio_controller.switch(switch, on=False)
                        else:
                            self.gpio_controller.switch(switch, on=True)

            elif msg_type == self.codes['CMD_SEND_DATA']:
                if msg_details == self.codes['DATA_POWER_MEASUREMENTS']:
                    s = list()
                    for inp in ['IN_1', 'IN_2', 'IN_3']:
                        U, I = self.gpio_controller.get_power_measurements(inp)
                        s.extend([I, U])
                    meas_string = '\u0004'.join(s)
                    msg = f'\u0002{self.codes["DATA_FLAG"]}' \
                          f'\u0003{self.codes["DATA_POWER_MEASUREMENTS"]}' \
                          f'\u0003{meas_string}\u0002'
                    with lock:
                        self.bt_controller.send(msg)

                elif msg_details == self.codes['DATA_SWITCH_STATUS']:
                    s = {self.codes[switch_name]: int(status) for switch_name, status in self.gpio_controller.get_switch_status().items()}
                    status_string = "\u0004".join([i[1] for i in sorted(s)])
                    msg = f'\u0002{self.codes["DATA_FLAG"]}' \
                          f'\u0003{self.codes["DATA_SWITCH_STATUS"]}' \
                          f'\u0003{status_string}\u0002'
                    with lock:
                        self.bt_controller.send(msg)

        elif msg_flag == self.codes['DATA_FLAG']:
            pass


            # elif cmd == self.codes['SEND_STATISTICS']:
            #     #TODO change message format to not use "-" as separator
            #     s = self.gpio_controller.get_statistics()
            #     msg = f'\u0002{self.config.get("PREFIXES", "PFX_STATISTICS")}{"|".join([str(k)+":"+str(v) for k,v in s.items()])}\u0002'
            #     with lock:
            #         self.bt_controller.send(msg)
            #
            # elif cmd == self.codes['SEND_SWITCH_STATUS']:
            #
            #     s = {self.config.get('SWITCHES', k): int(v) for k, v in self.gpio_controller.get_switch_status().items()}
            #     msg = f'\u0002{self.config.get("PREFIXES", "PFX_SWITCH_STATUS")}{"|".join([str(k)+":"+str(v) for k,v in s.items()])}\u0002'
            #     # with lock:
            #     self.bt_controller.send(msg)


if __name__ == '__main__':
    config = configparser.ConfigParser()
    config.optionxform = str

    config.read('/home/pi/repos/vancontrol/raspi/config.ini')

    config.read('/home/pi/repos/vancontrol/raspi/internal_wiring_config.ini')
    config.read('/home/pi/repos/vancontrol/external_wiring_config.ini')

    logger = logging.getLogger()
    logger.setLevel(logging.DEBUG)
    fileHandler = logging.FileHandler(f'/home/pi/logs/{datetime.now().strftime("%Y-%m-%d_%H-%M-%S")}.log')
    fileHandler.setLevel(logging.DEBUG)
    formatter = logging.Formatter("%(asctime)s;%(levelname)s;%(message)s",
                                    "%Y-%m-%d %H:%M:%S")
    fileHandler.setFormatter(formatter)
    logger.addHandler(fileHandler)

    processor = Processor(config)
    processor.run_main_loop()

