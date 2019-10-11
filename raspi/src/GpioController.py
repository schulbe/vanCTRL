import RPi.GPIO as GPIO # Import Raspberry Pi GPIO library
import Adafruit_ADS1x15

import logging
import regex as re
import time

logger = logging.getLogger(__name__)
logger.handlers = [logging.NullHandler()]
logger.propagate = 0

class GpioController:
    ads_1 = None
    ads_2 = None
    ads_3 = None

    gain_factor_mapping = {
        0.256: 16,
        0.512: 8,
        1.024: 4,
        2.048: 2,
        4.096: 1,
        6.144: 2/3
    }

    power_measurement_mapping = dict()

    def __init__(self, config):

        self.config = config
        # self.pins = pin_numbers
        self.pins = dict([(v, k) for k, v in config.items('GPIOS')])
        # self.measurement_names = dict(config.items('MEASUREMENT_NAMES'))
        self.measurement_mapping = {k: v.split(':') for k, v in config.items('MEASUREMENT_MAPPINGS')}

        self.power_inputs = [inp for inp, typ in config.items('INPUT_TYPES') if typ == 'POWER']
        self.temperature_inputs = [inp for inp, typ in config.items('INPUT_TYPES') if typ == 'TEMPERATURE']

        self.initialize_power_measurement_mapping(config, self.power_inputs)
        self.temp_measurement_mapping = {inp: {'id': config.get('INPUT_SPECS', f'{inp}_SENSOR_ID')} for inp in self.temperature_inputs}

        self.ads_1 = Adafruit_ADS1x15.ADS1115(address=int(config.get('ADC_ADDRESSES', 'ADS_1'), 16))
        self.ads_2 = Adafruit_ADS1x15.ADS1115(address=int(config.get('ADC_ADDRESSES', 'ADS_2'), 16))
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
            logger.debug(f'Switching Switch {switch} on pin {io_pin} on')
        else:
            GPIO.output(io_pin, GPIO.LOW)
            logger.debug(f'Switching Switch {switch} on pin {io_pin} off')

    def get_power_measurements(self, inp):
        adc_name_pos, channel_pos = self.power_measurement_mapping[inp]['addr_positive']
        adc_name_pre_shunt, channel_pre_shunt = self.power_measurement_mapping[inp]['addr_pre_shunt']
        adc_name_ref, channel_ref = self.power_measurement_mapping[inp]['addr_negative_ref']
        if adc_name_pos != adc_name_ref or adc_name_pre_shunt != adc_name_ref:
            raise TypeError('Cant read difference if adcs are not the same')
        Us = list()
        Is = list()

        for i in range(3):
            Us.append((self._read_adc(adc_name_ref, int(channel_pos),
                                      channel_ref=int(channel_ref),
                                      gain=self.power_measurement_mapping[inp]['v_gain'])
                       * self.power_measurement_mapping[inp]['v_per_bit']))
            Is.append((self._read_adc(adc_name_ref, int(channel_pre_shunt),
                                      channel_ref=int(channel_ref),
                                      gain=self.power_measurement_mapping[inp]['a_gain'])
                       * self.power_measurement_mapping[inp]['a_per_bit']))
            time.sleep(0.1)

        U = sum(Us)/3

        I = sum(Is)/3

        logger.debug(f"INPUT: {inp} // I: {I} // U: {U} ")

        return I, U

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
                return dif

            else:
                return ADS.read_adc(channel, gain=gain)

    def get_switch_status(self):
        return {switch: self.switch_is_on(switch) for switch in self.pins.keys() if switch.startswith('SWITCH_')}

    def switch_is_on(self, switch):
        io_pin = int(self.pins[switch])
        if GPIO.input(io_pin) == GPIO.HIGH:
            return True
        return False

    def update_power_measurement_mapping_entry(self, inp, a_shunt, mv_shunt, max_volt):
        def v_per_bit(gain):
            return 4.096/gain/(2**15)

        a_shunt = int(a_shunt)
        max_volt = int(max_volt)
        v_shunt = int(mv_shunt) / 1000
        factor = self.config.getint('MEASUREMENT_MAPPINGS', f'{inp}_VOLT_DIV_FACTOR')
        positive = self.config.get('MEASUREMENT_MAPPINGS', f'{inp}_POSITIVE').split(':')
        pre_shunt = self.config.get('MEASUREMENT_MAPPINGS', f'{inp}_PRE_SHUNT').split(':')
        negative_ref = self.config.get('MEASUREMENT_MAPPINGS', f'{inp}_NEGATIVE_REF').split(':')

        a_gain = self.get_gain(v_shunt)
        v_gain = self.get_gain(max_volt/factor)

        self.power_measurement_mapping[inp] = {
            'a_per_bit': v_per_bit(a_gain) / v_shunt * a_shunt,
            'v_per_bit': v_per_bit(v_gain) * factor,
            'a_gain': a_gain,
            'v_gain': v_gain,
            'addr_positive': positive,
            'addr_pre_shunt': pre_shunt,
            'addr_negative_ref': negative_ref,
        }

    def initialize_power_measurement_mapping(self, config, power_inputs):
        for inp in power_inputs:
            self.update_power_measurement_mapping_entry(
                inp,
                config.getint('INPUT_SPECS', f'{inp}_SHUNT_A'),
                config.getint('INPUT_SPECS', f'{inp}_SHUNT_MV'),
                config.getint('INPUT_SPECS', f'{inp}_MAX_VOLT'))

    def get_gain(self, max_volt):
        possible_gains = [(k, v) for k, v in self.gain_factor_mapping.items() if k >= max_volt]
        try:
            _, gain = sorted(possible_gains)[0]
        except IndexError:
            # TODO produce Errors!
            return 2/3

        return gain

    def get_temperature_measurement(self, inp):
        name = self.temp_measurement_mapping[inp]['id']
        path = f'/sys/bus/w1/devices/{name}/w1_slave'

        with open(path, 'r') as f:
            line = f.readline()
            if re.match(r"([0-9a-f]{2} ){9}: crc=[0-9a-f]{2} YES", line):
                line = f.readline()
                m = re.match(r"([0-9a-f]{2} ){9}t=([+-]?[0-9]+)", line)
                if m:
                    value = float(m.group(2)) / 1000.0

        return value
