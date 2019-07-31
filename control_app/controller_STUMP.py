class Controller:

    def __init__(self):
        self.light_status = {'front': 'off', 'back': 'off'}

    def switch_light(self, light):
        print(f'Switched light: {light}')
        if self.light_status[light] == 'off':
            self.light_status[light] = 'on'
        else:
            self.light_status[light] = 'off'

    def get_light_status(self):
        return self.light_status