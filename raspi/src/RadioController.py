import pigpio
import sys


class RadioController:
    mark_length_micro = 536.5
    address = "1110001"
    mute_1 = "0111000"

    def __init__(self, pin):
        self.pin = pin
        self.pi = pigpio.pi()
        self.pi.set_mode(self.pin, pigpio.OUTPUT)

    def _agc(self):
        return [pigpio.pulse(1<<self.pin, 0, 16*self.mark_length_micro), pigpio.pulse(0, 1<<self.pin, 8*self.mark_length_micro)]

    def _one(self):
        return [pigpio.pulse(1<<self.pin, 0, self.mark_length_micro), pigpio.pulse(0, 1<<self.pin, 3*self.mark_length_micro)]

    def _zero(self):
        return [pigpio.pulse(1<<self.pin, 0, self.mark_length_micro), pigpio.pulse(0, 1<<self.pin, self.mark_length_micro)]

    def _pause(self):
        return [pigpio.pulse(1<<self.pin, 0, self.mark_length_micro), pigpio.pulse(0, 1<<self.pin, 40*self.mark_length_micro)]

    def _parse_code(self, code):
        pulses = []
        for num in code:
            if num == "1":
                pulses.extend(self._one())
            elif num == "0":
                pulses.extend(self._zero())
            else:
                raise TypeError(f"Encountered code '{num}'. Only 0 and 1 are allowed codes.")

    def create_pulses(self, code):
        pulse = self._agc()

        # start_bit
        pulse.extend(self._one())

        # adress
        pulse.extend(self._parse_code(self.address))

        # code
        pulse.extend(self._parse_code(code))

        # stop bit
        pulse.extend(self._one())

        # pause
        pulse.extend(self._pause())

        # repeat 3 time
        pulse.extend(2*pulse)

        return pulse

    def send_code(self, code):
        pulses = self.create_pulses(str(code))

        self.pi.wave_add_generic(pulses)

        wid = self.pi.wave_create()

        self.pi.wave_send_once(wid)

    def test(self):
        pulse = [pigpio.pulse(1<<self.pin, 0, 1000000), pigpio.pulse(0, 1<<self.pin, 1000000)]
        pulse.extend(5*pulse)

        self.pi.wave_add_generic(pulse)

        wid = self.pi.wave_create()

        self.pi.wave_send_once(wid)


if __name__ == "__main__":
    rc = RadioController(22)
    t = sys.argv[1]
    if t == 'test':
        rc.test()




