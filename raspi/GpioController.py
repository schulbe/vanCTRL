import RPi.GPIO as GPIO # Import Raspberry Pi GPIO library
import random


# noinspection PyUnresolvedReferences
class GpioController:
    def __init__(self, pin_numbers, measurement_names):

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

    def get_statistics(self):
        stats = dict()

        stats[measurement_names['BATTERY_VOLT']] = random.randint(1220, 1360)/100
        stats[measurement_names['BATTERY_AMP']] = random.randint(200, 350)/100
        stats[measurement_names['SOLAR_VOLT']] = random.randint(2500, 3500)/100
        stats[measurement_names['BATTERY_VOLT']] = random.randint(300, 500)/100
        stats[measurement_names['TEMPERATURE_FRIDGE']] = random.randint(40, 70)/10
        stats[measurement_names['TEMPERATURE_INSIDE']] = random.randint(250, 280)/10

        return stats