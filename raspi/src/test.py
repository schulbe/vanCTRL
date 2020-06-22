from BluetoothController import BluetoothController


def process_message(msg, lock):
    with lock:
        print(msg)


UUID = "1e0ca4ea-299d-4335-93eb-27fcfe7fa849"
bt_controller = BluetoothController(uuid=UUID)
bt_controller.connect_and_listen(callback=process_message)