from BluetoothController import BluetoothController
from GpioController import GpioController
import configparser
import os
import logging
import time


send_statistics = False


class Processor:
    def __init__(self, conf):
        self.config = conf
        self.commands = config['COMMANDS']

        self.dt = 1/conf['GENERAL']['updates_per_sec']

        logging.info("Initializing GPIO Controll")
        self.gpio_controller = GpioController(pin_numbers=config['GPIOS'])

        logging.info("Initializing BT Controll")
        self.bt_controller = BluetoothController(uuid=config['IDS']['UUID'])

    def run_main_loop(self):
        logging.info("Starting connect_and_listen loop...")
        self.bt_controller.connect_and_listen(callback=self.process_command)

    def process_command(self, cmd, lock):
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
            global send_statistics
            send_statistics = True

            while send_statistics:
                s = self.gpio_controller.get_statistics()
                msg = f'\u0003{"|".join([str(k)+"-"+str(v) for k,v in s.items()])}\u0003'
                with lock:
                    self.bt_controller.send(msg)
                time.sleep(self.dt)

        elif cmd == self.commands['SEND_STATISTICS_STOP']:
            global send_statistics
            send_statistics = False


if __name__ == '__main__':
    config = configparser.ConfigParser(os.environ)
    config.read('config.ini')

    #TODO: LOGGING

    processor = Processor(config)
    processor.run_main_loop()
