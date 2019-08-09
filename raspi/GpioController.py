import RPi.GPIO as GPIO # Import Raspberry Pi GPIO library
import random


# noinspection PyUnresolvedReferences
class GpioController:
    def __init__(self, pin_numbers, measurement_names):

        self.pins = pin_numbers
        self.measurement_names = measurement_names

        GPIO.setwarnings(False)  # Ignore warning for now
        GPIO.setmode(GPIO.BOARD)  # Use physical pin numbering

        GPIO.setup(int(self.pins['FRONT_LIGHT_SWITCH']), GPIO.OUT, initial=GPIO.LOW)
        GPIO.setup(int(self.pins['BACK_LIGHT_SWITCH']), GPIO.OUT, initial=GPIO.LOW)
        GPIO.setup(int(self.pins['FRIDGE_SWITCH']), GPIO.OUT, initial=GPIO.LOW)
        GPIO.setup(int(self.pins['RADIO_SWITCH']), GPIO.OUT, initial=GPIO.LOW)

    def switch(self, switch, on=True):
        io_pin = int(self.pins[switch])
        if on:
            GPIO.output(io_pin, GPIO.HIGH)
        else:
            GPIO.output(io_pin, GPIO.LOW)

    def get_statistics(self):
        stats = dict()

        stats[self.measurement_names['STAT_BATTERY_VOLT']] = random.randint(1220, 1360)/100
        stats[self.measurement_names['STAT_BATTERY_AMP']] = random.randint(200, 350)/100
        stats[self.measurement_names['STAT_SOLAR_VOLT']] = random.randint(2500, 3500)/100
        stats[self.measurement_names['STAT_BATTERY_VOLT']] = random.randint(300, 500)/100
        # stats[measurement_names['TEMPERATURE_FRIDGE']] = random.randint(40, 70)/10
        # stats[measurement_names['TEMPERATURE_INSIDE']] = random.randint(250, 280)/10
        logging.debug(f"returning stats to BTController: {stats}")
        return stats

    def get_switch_status(self):
        return {switch: self.switch_is_on(switch) for switch in self.pins.keys()}

    def switch_is_on(self, switch):
        io_pin = int(self.pins[switch])
        if GPIO.input(io_pin) == GPIO.HIGH:
            return True
        return False
