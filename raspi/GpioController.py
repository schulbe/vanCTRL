import RPi.GPIO as GPIO # Import Raspberry Pi GPIO library
import Adafruit_ADS1x15

import random
import logging
from contextlib import suppress


class GpioController:
    ads_1 = None
    ads_2 = None
    mcp = None
    ads_gain = 16


    def __init__(self, pin_numbers, measurement_names, adc_addresses, measurement_mapping, external_wiring_config):

        self.pins = pin_numbers
        self.measurement_names = measurement_names
        self.measurement_mapping = measurement_mapping

        self.power_measurement_mapping = {
            input: {
                'name': external_wiring_config.get('POWER_MEASUREMENTS', input),
                'a_per_bit': 4.096/self.ads_gain/(2**15)
                             /(external_wiring_config.getint('POWER_MEASUREMENTS', f'{input}_SHUNT_MV')/1000)
                             *external_wiring_config.getint('POWER_MEASUREMENTS', f'{input}_SHUNT_A')
            } for input in ['IN_1', 'IN_2', 'IN_3']
        }

        with suppress(Exception):
            self.ads_1 = Adafruit_ADS1x15.ADS1115(address=int(adc_addresses['ADS_1'], 16))
            self.ads_2 = Adafruit_ADS1x15.ADS1115(address=int(adc_addresses['ADS_2'], 16))

        GPIO.setwarnings(False)  # Ignore warning for now
        GPIO.setmode(GPIO.BOARD)  # Use physical pin numbering

        for assignment, pin_number in self.pins:
            if assignment.startswith('SWITCH_'):
                GPIO.setup(int(pin_number), GPIO.OUT, initial=GPIO.LOW)


    def switch(self, switch, on=True):
        io_pin = int(self.pins[switch])
        if on:
            GPIO.output(io_pin, GPIO.HIGH)
        else:
            GPIO.output(io_pin, GPIO.LOW)

    def get_statistics(self):

        stats = dict()

        for input in ['IN_1', 'IN_2', 'IN_3']:
            U, I = self.get_power_measurements(input)

            stats[self.measurement_names[f'STAT_VOLT_{self.power_measurement_mapping[input]["name"]}']] = U
            stats[self.measurement_names[f'STAT_AMP_{self.power_measurement_mapping[input]["name"]}']] = I


        # stats[measurement_names['TEMPERATURE_FRIDGE']] = random.randint(40, 70)/10
        # stats[measurement_names['TEMPERATURE_INSIDE']] = random.randint(250, 280)/10

        return stats

    def get_power_measurements(self, input):
        adc_name, channel = self.measurement_mapping[f'{input}_POSITIVE']
        U = self._read_adc(adc_name, channel)

        adc_name_high, channel_high = self.measurement_mapping[f'{input}_NEGATIVE_HIGH']
        adc_name_low, channel_low = self.measurement_mapping[f'{input}_NEGATIVE_LOW']
        if adc_name_low != adc_name_high:
            raise TypeError('Cant read difference if adcs are not the same')


        I = self._read_adc(adc_name_low, channel_high, channel_low) * self.power_measurement_mapping[input]['a_per_bit']

        return U, I


    def _read_adc(self, name, difference=False, gain=16, *channels):
            if difference and len(channels)!=2:
                raise TypeError('Wrong number of channels supplied for difference=True')

            if name.startswith('ADS_'):
                if name == 'ADS_1':
                    ADS = self.ads_1
                elif name == 'ADS_2':
                    ADS = self.ads_2
                else:
                    raise TypeError()
                if difference:
                    if channels == (0,1):
                        fac = 1
                        num = 0
                    elif channels == (1,0):
                        fac = -1
                        num = 0
                    elif channels == (2,3):
                        fac = 1
                        num = 3
                    elif channels == (3,2):
                        fac = -1
                        num = 3
                    else:
                        raise TypeError(f'Channelset {channels} unknown')

                    return ADS.read_adc_difference(num, gain=gain) * fac

                else:
                    return (ADS.read_adc(c, gain=gain) for c in channels)

            elif name == 'MCP':
                #todo
                return 0

    def get_switch_status(self):
        return {switch: self.switch_is_on(switch) for switch in self.pins.keys() if switch.startswith('SWITCH_')}

    def switch_is_on(self, switch):
        io_pin = int(self.pins[switch])
        if GPIO.input(io_pin) == GPIO.HIGH:
            return True
        return False
