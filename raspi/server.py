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
        logging.debug(f'Received Command {cmd} (type{type(cmd)})')

        # FRONT_LIGHT
        if cmd == self.commands['SWITCH_FRONT_LIGHT_ON']:
            with lock:
                self.gpio_controller.switch('FRONT_LIGHT_SWITCH', on=True)

        elif cmd == self.commands['SWITCH_FRONT_LIGHT_OFF']:
            with lock:
                self.gpio_controller.switch('FRONT_LIGHT_SWITCH', on=False)

        elif cmd == self.commands['SWITCH_FRONT_LIGHT_TOGGLE']:
            logging.debug("SWITCH_FRONT_LIGHT_TOGGLE!")
            with lock:
                if self.gpio_controller.switch_is_on('FRONT_LIGHT_SWITCH'):
                    self.gpio_controller.switch('FRONT_LIGHT_SWITCH', on=False)
                else:
                    self.gpio_controller.switch('FRONT_LIGHT_SWITCH', on=True)

        # BACK LIGHT
        elif cmd == self.commands['SWITCH_BACK_LIGHT_ON']:
            with lock:
                self.gpio_controller.switch('BACK_LIGHT_SWITCH', on=True)

        elif cmd == self.commands['SWITCH_BACK_LIGHT_OFF']:
            with lock:
                self.gpio_controller.switch('BACK_LIGHT_SWITCH', on=False)

        elif cmd == self.commands['SWITCH_BACK_LIGHT_TOGGLE']:
            with lock:
                if self.gpio_controller.switch_is_on('BACK_LIGHT_SWITCH'):
                    self.gpio_controller.switch('BACK_LIGHT_SWITCH', on=False)
                else:
                    self.gpio_controller.switch('BACK_LIGHT_SWITCH', on=True)

        # FRIDGE
        elif cmd == self.commands['SWITCH_FRIDGE_ON']:
            with lock:
                self.gpio_controller.switch('FRIDGE_SWITCH', on=True)

        elif cmd == self.commands['SWITCH_FRIDGE_OFF']:
            with lock:
                self.gpio_controller.switch('FRIDGE_SWITCH', on=False)

        elif cmd == self.commands['SWITCH_FRIDGE_TOGGLE']:
            with lock:
                if self.gpio_controller.switch_is_on('FRIDGE_SWITCH'):
                    self.gpio_controller.switch('FRIDGE_SWITCH', on=False)
                else:
                    self.gpio_controller.switch('FRIDGE_SWITCH', on=True)

        # RADIO
        elif cmd == self.commands['SWITCH_RADIO_ON']:
            with lock:
                self.gpio_controller.switch('RADIO_SWITCH', on=True)

        elif cmd == self.commands['SWITCH_RADIO_OFF']:
            with lock:
                self.gpio_controller.switch('RADIO_SWITCH', on=False)

        elif cmd == self.commands['SWITCH_RADIO_TOGGLE']:
            with lock:
                if self.gpio_controller.switch_is_on('RADIO_SWITCH'):
                    self.gpio_controller.switch('RADIO_SWITCH', on=False)
                else:
                    self.gpio_controller.switch('RADIO_SWITCH', on=True)

        # SEND STATISTICS
        elif cmd == self.commands['SEND_STATISTICS_START']:
            send_statistics = True

            while send_statistics:
                s = self.gpio_controller.get_statistics()
                msg = f'\u0002{self.config.get("PREFIXES", "PFX_STATISTICS")}{"|".join([str(k)+"-"+str(v) for k,v in s.items()])}\u0002'
                with lock:
                    self.bt_controller.send(msg)
                time.sleep(self.dt)

        elif cmd == self.commands['SEND_STATISTICS_STOP']:
            send_statistics = False

        elif cmd == self.commands['SEND_STATISTICS']:
            s = self.gpio_controller.get_statistics()
            msg = f'\u0002{self.config.get("PREFIXES", "PFX_STATISTICS")}{"|".join([str(k)+"-"+str(v) for k,v in s.items()])}\u0002'
            with lock:
                self.bt_controller.send(msg)

        # SEND STATUS
        elif cmd == self.commands['SEND_SWITCH_STATUS']:

            s = {self.config.get('SWITCHES', k): int(v) for k, v in self.gpio_controller.get_switch_status().items()}
            msg = f'\u0002{self.config.get("PREFIXES", "PFX_SWITCH_STATUS")}{"|".join([str(k)+"-"+str(v) for k,v in s.items()])}\u0002'
            # with lock:
            self.bt_controller.send(msg)


if __name__ == '__main__':
    config = configparser.ConfigParser()
    config.read('/home/pi/repos/vancontrol/raspi/config.ini')

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
