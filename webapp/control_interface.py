import requests


class Controller:
    
    def __init__(self, ctrl_host, ctrl_port):
        self.host = ctrl_host
        self.port = ctrl_port
        self.url = f'{ctrl_host}:{ctrl_port}'
        self.cmd_url = self.url + '/cmd'
        self.info_url = self.url + '/info'

    def switch_light(self, light):
        r = {'target': 'lights', 'cmd': 'switch', 'light_position': light}
        self.send_request(r, 'command')

    def get_light_status(self):
        pass

    def send_request(self, request_dict, type):
        if type == 'command':
            requests.post(f'http://{self.cmd_url}', json=request_dict)
        if type == 'status':
            requests.post(f'http://{self.info_url}', json=request_dict)