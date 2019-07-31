import RPi.GPIO as GPIO # Import Raspberry Pi GPIO library

while True: # Run forever
    i = input("type 0 or 1")
    if i == 0:
        GPIO.output(7, GPIO.HIGH) # Turn on
    elif i == 1:
        GPIO.output(7, GPIO.LOW) # Turn off


class Controller:

    def __init__(self, pin_numbers):
        self.pins = pin_numbers

        GPIO.setwarnings(False) # Ignore warning for now
        GPIO.setmode(GPIO.BOARD) # Use physical pin numbering

        GPIO.setup(self.pins['light_front'], GPIO.OUT, initial=GPIO.HIGH)
        GPIO.setup(self.pins['light_back'], GPIO.OUT, initial=GPIO.HIGH)

    def switch_light(self, light):
        io_pin = self.pins[f'light_{light}']
        print(f'Switched light: {light}')
        if GPIO.input(io_pin):
            GPIO.output(io_pin, GPIO.LOW)
        else:
            GPIO.output(io_pin, GPIO.HIGH)

    def get_light_status(self):
        return self.light_status