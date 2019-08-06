from BluetoothController import BluetoothController
from GpioController import GpioController
import configparser
import os
import logging
import time
from datetime import datetime

send_statistics = False


class Processor:
    def __init__(self, conf):
        self.config = conf
        self.commands = config['COMMANDS']

        self.dt = 1/int(conf['GENERAL']['updates_per_second'])

        logging.info("Initializing GPIO Controll")
        self.gpio_controller = GpioController(pin_numbers=config['GPIOS'], measurement_names=conf['MEASUREMENT_NAMES'])

        logging.info("Initializing BT Controll")
        self.bt_controller = BluetoothController(uuid=config['GENERAL']['UUID'])

    def run_main_loop(self):
        logging.info("Starting connect_and_listen loop...")
        self.bt_controller.connect_and_listen(callback=self.process_command)

    def process_command(self, cmd, lock):
        global send_statistics

        if cmd == self.commands['SWITCH_FRONT_LIGHT_ON']:
            with lock:
                self.gpio_controller.switch('LIGHT_FRONT', on=True)

        elif cmd == self.commands['SWITCH_FRONT_LIGHT_OFF']:
            with lock:
                self.gpio_controller.switch('LIGHT_FRONT', on=False)

        elif cmd == self.commands['SWITCH_BACK_LIGHT_ON']:
            with lock:
                self.gpio_controller.switch('LIGHT_BACK', on=True)

        elif cmd == self.commands['SWITCH_BACK_LIGHT_OFF']:
            with lock:
                self.gpio_controller.switch('LIGHT_BACK', on=False)

        elif cmd == self.commands['SEND_STATISTICS_START']:
            send_statistics = True

            while send_statistics:
                s = self.gpio_controller.get_statistics()
                msg = f'\u0003{"|".join([str(k)+"-"+str(v) for k,v in s.items()])}\u0003'
                with lock:
                    self.bt_controller.send(msg)
                time.sleep(self.dt)

        elif cmd == self.commands['SEND_STATISTICS_STOP']:
            send_statistics = False


if __name__ == '__main__':
    config = configparser.ConfigParser(os.environ)
    config.read('/home/pi/repos/vancontrol/raspi/config.ini')

    #TODO: LOGGING
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
