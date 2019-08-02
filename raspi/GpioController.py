import RPi.GPIO as GPIO # Import Raspberry Pi GPIO library


# noinspection PyUnresolvedReferences
class GpioController:
    def __init__(self, pin_numbers):
        self.pins = pin_numbers

        GPIO.setwarnings(False)  # Ignore warning for now
        GPIO.setmode(GPIO.BOARD)  # Use physical pin numbering

        GPIO.setup(int(self.pins['LIGHT_FRONT']), GPIO.OUT, initial=GPIO.HIGH)
        GPIO.setup(int(self.pins['LIGHT_BACK']), GPIO.OUT, initial=GPIO.HIGH)
        GPIO.setup(int(self.pins['FRIDGE']), GPIO.OUT, initial=GPIO.HIGH)

    def switch(self, switch, on=True):
        io_pin = int(self.pins[switch])
        if on:
            GPIO.output(io_pin, GPIO.LOW)
        else:
            GPIO.output(io_pin, GPIO.HIGH)

    # def get_light_status(self):

