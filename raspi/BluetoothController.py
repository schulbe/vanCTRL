import bluetooth as bt
import logging
import threading


class BluetoothController:
    def __init__(self, uuid):
        self.uuid = uuid
        self.callback = None

        self._lock = threading.RLock()
        self._client_info = None
        self._client_sock = None

        self._server_sock = bt.BluetoothSocket(bt.RFCOMM)
        self._port = bt.PORT_ANY
        self._server_sock.bind(("", self._port))
        self._server_sock.listen(1)

        self._port = self._server_sock.getsockname()[1]

        bt.advertise_service(self._server_sock, "VanController",
                             service_id=uuid,
                             service_classes=[uuid, bt.SERIAL_PORT_CLASS],
                             profiles=[bt.SERIAL_PORT_PROFILE])

        logging.info(f'Advertising BT Service on Port {self._port} // UUID: {self.uuid}')

    def close_connection(self):
        logging.info(f'Closing connection with {self._client_info}')
        self._client_sock.close()
        self._client_info = None
        self._client_sock = None

    def send(self, msg):
        try:
            with self._lock:
                self._client_sock.send(bytes(msg, 'utf-8'))
                logging.debug(f'Sent message: {msg}')
        except bt.btcommon.BluetoothError as e:
            logging.error(f'BluetoothError occured while trying to send message: {e}', exc_info=True)
            self._handle_error(e)

    def connect_and_listen(self, callback):
        self.callback = callback

        t = threading.Thread(target=self._listen)
        t.start()
        t.join()

    def _wait_for_connection(self):
        logging.info(f'Waiting for connection')
        self._client_sock, self._client_info = self._server_sock.accept()
        logging.info(f'Established Connection with {self._client_info}')

    def _listen(self):
        while True:
            if not self._client_sock:
                with self._lock:
                    self._wait_for_connection()
            try:
                data = self._client_sock.recv(1024)
                if len(data) == 0:
                    logging.info('Data had length 0.')
                    continue
                self._process_received_data(data)

            except bt.btcommon.BluetoothError as e:
                logging.error(f'BluetoothError occured while waiting or processing command: {e}', exc_info=True)
                self._handle_error(e)

    def _process_received_data(self, data):
        data = data.decode("utf-8")
        logging.debug(f'Processing Received Data: {data}')
        if data.startswith("\u0002") and data.endswith("\u0002"):
            for cmd in data.split("\u0002"):
                if cmd:
                    t = threading.Thread(target=self.callback(cmd, self._lock))
                    t.setDaemon(True)
                    t.start()
        else:
            logging.error(f"Did not understand command: {data}")

    def _handle_error(self, e):
        try:
            error_code = eval(str(e))[0]
            if error_code == 104:
                logging.error('Connection reset by peer. Closing Connection.')
                with self._lock:
                    self.close_connection()
            elif error_code == 110:
                logging.error('Connection timed out. Closing Connection.')
                with self._lock:
                    self.close_connection()
        except Exception as e2:
            logging.error(f"Could not process error: {e} (because of: {e2})")


class ConnectionClosedError(Exception):
    pass

