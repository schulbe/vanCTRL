from BluetoothController import BluetoothController
from GpioController import GpioController
import configparser
import os


def process_command(cmd, gpio_controller):
    if cmd == commands['SWITCH_FRONT_LIGHT_ON']:
        gpio_controller.switch('LIGHT_FRONT', on=True)
    elif cmd == commands['SWITCH_FRONT_LIGHT_OFF']:
        gpio_controller.switch('LIGHT_FRONT', on=False)
    elif cmd == commands['SWITCH_BACK_LIGHT_ON']:
        gpio_controller.switch('LIGHT_BACK', on=True)
    elif cmd == commands['SWITCH_BACK_LIGHT_OFF']:
        gpio_controller.switch('LIGHT_BACK', on=False)
    elif cmd == commands['CLOSE_CONNECTION']:
        bt_controller.close_connection()


if __name__ == '__main__':
    config = configparser.ConfigParser(os.environ)
    config.read('config.ini')
    commands = config['COMMANDS']

    print("Initializing GPIO Controll")
    gpio_controller = GpioController(config['GPIOS'])

    print("Initializing BT Controll")
    bt_controller = BluetoothController(config['IDS']['UUID'])


    while True:

        print("Waiting for connection...")
        try:
            bt_controller._wait_for_connection()
        except KeyboardInterrupt:
            break
        except:
            continue

        while True:
            try:
                cmd = bt_controller.wait_for_command()
            except KeyboardInterrupt:
                break
            except Exception as e:
                error_code = eval(str(e))[0]
                if error_code == 104:
                    bt_controller.close_connection()
                    break

            print(f'COMMAND: {cmd}')
            if cmd == commands['SWITCH_FRONT_LIGHT_ON']:
                gpio_controller.switch('LIGHT_FRONT', on=True)
            elif cmd == commands['SWITCH_FRONT_LIGHT_OFF']:
                gpio_controller.switch('LIGHT_FRONT', on=False)
            elif cmd == commands['SWITCH_BACK_LIGHT_ON']:
                gpio_controller.switch('LIGHT_BACK', on=True)
            elif cmd == commands['SWITCH_BACK_LIGHT_OFF']:
                gpio_controller.switch('LIGHT_BACK', on=False)
            elif cmd == commands['CLOSE_CONNECTION']:
                bt_controller.close_connection()
                break

