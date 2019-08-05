from BluetoothController import BluetoothController
# from GpioController import GpioController
import configparser
import os
import threading
import random
import time
from datetime import datetime

# class TaskRecorder:
#     def __init__(self):
#         self._sending_tasks = list()
#         self._received_tasks = list()
#         self._lock = threading.Lock()
#
#     def put_sending_item(self, item):
#         with self._lock:
#             self._sending_tasks.append(item)
#
#     def get_sending_item(self):
#         with self._lock:
#             self._sending_tasks.pop(-1)
#
#     def put_received_item(self, item):
#         with self._lock:
#             self._received_tasks.append(item)
#
#     def get_received_item(self):
#         with self._lock:
#             self._received_tasks.pop(-1)


class Test:
    def __init__(self, callback):
        self._lock = threading.Lock()
        self.callback = callback

    def run(self):
        listener = threading.Thread(target=self._listen)
        # listener.daemon = True
        listener.start()

    def write(self, msg):
        write_start = str(datetime.now())
        with self._lock:
            with open('test_file.log', 'a') as f:
                f.write(f'msg_written: {msg} --> process_start: {write_start}\n')
                print(f'msg_written: {msg} --> process_start: {write_start}\n')

    def _listen(self):
        while True:
            time.sleep(random.randint(0, 50)/10)
            task = str(datetime.now())
            process = threading.Thread(target=self._process_task, args=(task,))
            process.daemon = True
            process.start()

    def _process_task(self, task):
        with self._lock:
            self.callback(task)


def print_callback(task):
    process_start = str(datetime.now())
    time.sleep(random.randint(0, 50)/10)
    with open('test_file.log', 'a') as f:
        f.write(f'task_received: {task} --> process_start: {process_start}\n')
        print(f'task_received: {task} --> process_start: {process_start}\n')


if __name__ == '__main__':
    t = Test(print_callback)
    t.run()

    while True:
        time.sleep(random.randint(20, 50)/10)
        t.write(str(datetime.now()))
