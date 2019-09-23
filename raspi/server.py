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
        self.commands = config['COMMANDS']

        self.dt = 1/int(conf['GENERAL']['updates_per_second'])

        logging.info("Initializing GPIO Controll")
        self.gpio_controller = GpioController(config=config)

        logging.info("Initializing BT Controll")
        self.bt_controller = BluetoothController(uuid=config['GENERAL']['UUID'])

    def run_main_loop(self):
        logging.info("Starting connect_and_listen loop...")
        self.bt_controller.connect_and_listen(callback=self.process_command)

    def process_command(self, cmd, lock):
        global send_statistics
        logging.debug(f'Received Command {cmd} (type{type(cmd)})')

        if cmd.startswith(self.commands['SWITCH_ON']):
            with suppress(IndexError):
                switch = cmd.split('\u0003')[1]
                switch = [k for k,v in self.config.items('SWITCHES') if v == switch][0]
                with lock:
                    self.gpio_controller.switch(switch, on=True)

        if cmd.startswith(self.commands['SWITCH_OFF']):
            with suppress(IndexError):
                switch = cmd.split('\u0003')[1]
                switch = [k for k,v in self.config.items('SWITCHES') if v == switch][0]
                with lock:
                    self.gpio_controller.switch(switch, on=False)

        if cmd.startswith(self.commands['SWITCH_TOGGLE']):
            with suppress(IndexError):
                switch = cmd.split('\u0003')[1]
                switch = [k for k,v in self.config.items('SWITCHES') if v == switch][0]
                with lock:
                    if self.gpio_controller.switch_is_on(switch):
                        self.gpio_controller.switch(switch, on=False)
                    else:
                        self.gpio_controller.switch(switch, on=True)


        elif cmd == self.commands['SEND_STATISTICS']:
            #TODO change message format to not use "-" as separator
            s = self.gpio_controller.get_statistics()
            msg = f'\u0002{self.config.get("PREFIXES", "PFX_STATISTICS")}{"|".join([str(k)+":"+str(v) for k,v in s.items()])}\u0002'
            with lock:
                self.bt_controller.send(msg)

        # SEND STATUS
        elif cmd == self.commands['SEND_SWITCH_STATUS']:

            s = {self.config.get('SWITCHES', k): int(v) for k, v in self.gpio_controller.get_switch_status().items()}
            msg = f'\u0002{self.config.get("PREFIXES", "PFX_SWITCH_STATUS")}{"|".join([str(k)+":"+str(v) for k,v in s.items()])}\u0002'
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
