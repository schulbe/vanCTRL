import bluetooth as bt


class BluetoothController:
    def __init__(self, uuid):
        self.uuid = uuid

        self.client_info = None
        self.client_sock = None

        self.server_sock = bt.BluetoothSocket(bt.RFCOMM)
        self.server_sock.bind(("", bt.PORT_ANY))
        self.server_sock.listen(1)

        self.port = self.server_sock.getsockname()[1]

        bt.advertise_service(self.server_sock, "VanController",
                             service_id=uuid,
                             service_classes=[uuid, bt.SERIAL_PORT_CLASS],
                             profiles=[bt.SERIAL_PORT_PROFILE])

    def wait_for_connection(self):

        self.client_sock, self.client_info = self.server_sock.accept()
        print('Client connection came in')
        print(self.client_info)
        print(self.client_sock)
        # return self.client_sock

    def close_connection(self):
        self.client_sock.close()

    def wait_for_command(self):
        data = self.client_sock.recv(1024)
        if len(data) == 0:
            raise TypeError
        data = data.decode("utf-8")
        print(data)
        if data.startswith("\u0002") and data.endswith("\u0002"):
            print(data)
            return data.replace('\u0002', '')
        else:
            raise TypeError(f'Didnt Understand the Command {data}')

    def send_message(self, msg):
        self.client_sock.send(bytes(msg, 'utf-8'))

