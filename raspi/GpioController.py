import RPi.GPIO as GPIO # Import Raspberry Pi GPIO library
import Adafruit_ADS1x15

import random
import logging
from contextlib import suppress


class GpioController:
    ads_1 = None
    ads_2 = None
    ads_3 = None
    mcp = None
    ads_gain_amp = 16
    ads_gain_v = 1

    def __init__(self, config):

        # self.pins = pin_numbers
        self.pins = dict([(v, k) for k, v in config.items('GPIOS')])
        # self.measurement_names = dict(config.items('MEASUREMENT_NAMES'))
        self.measurement_mapping = {k: v.split(':') for k, v in config.items('MEASUREMENT_MAPPINGS')}

        self.power_measurement_mapping = {
            inp: {
                'name': config.get('POWER_MEASUREMENTS', inp),
                'a_per_bit': 4.096/self.ads_gain_amp/(2**15)
                             /(config.getint('POWER_MEASUREMENTS', f'{inp}_SHUNT_MV')/1000)
                             *config.getint('POWER_MEASUREMENTS', f'{inp}_SHUNT_A'),
                'v_per_bit': 4.096/self.ads_gain_v/(2**15)
            } for inp in ['IN_1', 'IN_2', 'IN_3']
        }

        with suppress(Exception):
            self.ads_1 = Adafruit_ADS1x15.ADS1115(address=int(config.get('ADC_ADDRESSES', 'ADS_1'), 16))
        with suppress(Exception):
            self.ads_2 = Adafruit_ADS1x15.ADS1115(address=int(config.get('ADC_ADDRESSES', 'ADS_2'), 16))
        with suppress(Exception):
            self.ads_3 = Adafruit_ADS1x15.ADS1115(address=int(config.get('ADC_ADDRESSES', 'ADS_3'), 16))
        GPIO.setwarnings(False)  # Ignore warning for now
        GPIO.setmode(GPIO.BOARD)  # Use physical pin numbering

        for assignment, pin_number in self.pins.items():
            if assignment.startswith('SWITCH_'):
                GPIO.setup(int(pin_number), GPIO.OUT, initial=GPIO.LOW)

    def switch(self, switch, on=True):
        io_pin = int(self.pins[switch])
        if on:
            GPIO.output(io_pin, GPIO.HIGH)
            logging.debug(f'Switching Switch {switch} on pin {io_pin} on')
        else:
            GPIO.output(io_pin, GPIO.LOW)
            logging.debug(f'Switching Switch {switch} on pin {io_pin} off')

    def get_power_measurements(self, input):
        adc_name_pos, channel_pos = self.measurement_mapping[f'{input}_POSITIVE']
        adc_name_pre_shunt, channel_pre_shunt = self.measurement_mapping[f'{input}_PRE_SHUNT']
        adc_name_ref, channel_ref = self.measurement_mapping[f'{input}_NEGATIVE_REF']
        if adc_name_pos != adc_name_ref or adc_name_pre_shunt != adc_name_ref:
            raise TypeError('Cant read difference if adcs are not the same')

        U = self._read_adc(adc_name_ref, int(channel_pos), channel_ref=int(channel_ref), gain=self.ads_gain_v) \
            * self.power_measurement_mapping[input]['v_per_bit']
        # todo only debug reasons
        I = self._read_adc(adc_name_ref, int(channel_pre_shunt), channel_ref=int(channel_ref), gain=self.ads_gain_amp) \
            * self.power_measurement_mapping[input]['a_per_bit']
        logging.debug(f"I: {I} // U: {U}")

        return U, I

    def _read_adc(self, name, channel, channel_ref=None, gain=16):
        def sign(perm, base):
            if perm == base:
                return 1
            elif perm == (base[1], base[0]):
                return -1

        def map_channel(c, cref):
            channels = (c, cref)
            if set(channels) == {0, 1}:
                return 0, sign(channels, (0, 1))
            elif set(channels) == {0, 3}:
                return 1, sign(channels, (0, 3))
            elif set(channels) == {1, 3}:
                return 2, sign(channels, (1, 3))
            elif set(channels) == {2, 3}:
                return 3, sign(channels, (2, 3))

        if name.startswith('ADS_'):
            if name == 'ADS_1':
                ADS = self.ads_1
            elif name == 'ADS_2':
                ADS = self.ads_2
            elif name == 'ADS_3':
                ADS = self.ads_3
            else:
                raise TypeError()
            if channel_ref is not None:
                dif_channel, factor = map_channel(channel, channel_ref)
                dif = ADS.read_adc_difference(dif_channel, gain=gain) * factor
                logging.debug(f'Read difference Ratio {dif_channel}: {dif}')
                return dif

            else:
                return ADS.read_adc(channel, gain=gain)

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
