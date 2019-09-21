import RPi.GPIO as GPIO # Import Raspberry Pi GPIO library
import Adafruit_ADS1x15

import random
import logging


class GpioController:
    def __init__(self, pin_numbers, measurement_names, s2i_addresses):

        self.pins = pin_numbers
        self.measurement_names = measurement_names
        try:
            self.ads_measurements = ADS(shunt_mv=50, shunt_a=100, address=int(s2i_addresses['ADS_MEASUREMENT'], 16))
        except:
            self.ads_measurements = None

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
        #todo: remove when fixed
        if self.ads_measurements is not None:
            bat_amp = self.ads_measurements.get_current()
        else:
            bat_amp = 1.23

        stats = dict()

        stats[self.measurement_names['STAT_BATTERY_VOLT']] = random.randint(12202, 13602)/1000
        stats[self.measurement_names['STAT_BATTERY_AMP']] = bat_amp
        stats[self.measurement_names['STAT_SOLAR_VOLT']] = random.randint(2500, 3500)/100
        stats[self.measurement_names['STAT_BATTERY_VOLT']] = random.randint(300, 500)/100
        # stats[measurement_names['TEMPERATURE_FRIDGE']] = random.randint(40, 70)/10
        # stats[measurement_names['TEMPERATURE_INSIDE']] = random.randint(250, 280)/10
        return stats

    def get_switch_status(self):
        return {switch: self.switch_is_on(switch) for switch in self.pins.keys()}

    def switch_is_on(self, switch):
        io_pin = int(self.pins[switch])
        if GPIO.input(io_pin) == GPIO.HIGH:
            return True
        return False

class ADS(Adafruit_ADS1x15):
    GAIN = 16
    VOLT_PER_BIT = 4.096/GAIN/(2**15)

    def __init__(self, shunt_mv=50, shunt_a=100, *args, **kwargs):
        super(ADS).__init__(*args, **kwargs)
        self.a_per_bit = self.VOLT_PER_BIT * (shunt_mv/1000)*shunt_a

    def get_current(self):
        return self.read_adc_difference(0, gain=self.GAIN) * self.a_per_bit