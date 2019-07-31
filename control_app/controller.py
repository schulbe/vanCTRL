import RPi.GPIO as GPIO # Import Raspberry Pi GPIO library

class Controller:

    def __init__(self, pin_numbers):
        self.pins = pin_numbers

        GPIO.setwarnings(False) # Ignore warning for now
        GPIO.setmode(GPIO.BOARD) # Use physical pin numbering

        GPIO.setup(int(self.pins['light_front']), GPIO.OUT, initial=GPIO.HIGH)
        GPIO.setup(int(self.pins['light_back']), GPIO.OUT, initial=GPIO.HIGH)

    def switch_light(self, light):
        io_pin = int(self.pins[f'light_{light}'])
        if GPIO.input(io_pin):
            GPIO.output(io_pin, GPIO.LOW)
        else:
            GPIO.output(io_pin, GPIO.HIGH)

    def get_light_status(self):
        return self.light_status
