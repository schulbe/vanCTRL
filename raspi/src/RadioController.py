import pigpio
import sys


class RadioController:
    # TODO:
    # include start of daemon process in shell script!!!
    # sudo pigpiod

    mark_length_micro = 520
    # mark_length_micro = 536.5
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
        return pulses

    def create_pulses(self, code):
        pulse_start = self._agc()

        # start_bit
        pulse = self._one()

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
        pulse_start.extend(pulse)
        return pulse_start

    def _send_code(self, code):
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

    def on_off(self):
        self._send_code('')

    def vol_up(self):
        self._send_code('')

    def vol_down(self):
        self._send_code('')

    def source(self):
        self._send_code('')

    def rotate_eq(self):
        self._send_code('')

    def toggle_mute(self):
        self._send_code('')

    def right(self):
        self._send_code('')

    def left(self):
        self._send_code('')

    def up(self):
        self._send_code('')

    def down(self):
        self._send_code('')


if __name__ == "__main__":
    rc = RadioController(25)
    t = sys.argv[1]
    print(t)
    if t == 'test':
        rc.test()
    else:
        rc._send_code(str(t))


# https://www.avforums.com/threads/jvc-stalk-adapter-diy.248455/page-2

# TODO: TEST CORRECTNESS
"""
0x04 Vol +
0x05 Vol -
0x08 Source
0x0D Sound
0x0E Mute
0x12 Right
0x13 Left
0x14 Up
0x15 Down

OR 

0x04 Vol+                     01010111101010101
0x05 Vol-                     01111010111101010101       
0x08 Source                   01010101111010101 
0x0D Sound                    01111010111101111010101    
0x0E Mute                     01011110111101111010101     
0x12 Skip fwd or Right        01011110101011110101   
0x13 Skip back or left        01111011110101011110101 
0x14 Skip fwd hold or Up      01010111101011110101  
0x15 Skip back hold or down   01111010111101011110101

OR

00 - Power ON / OFF
04 - Vol +
05 - Vol -
06 - Mute/Unmute
08 - Source
0C - Equalization preset cycle
0D - Mute/Unmute (and Power OFF with "press and hold" - see below)
12 - Seach + / Track + (and Manual Tune + / Fast fwd with "press and hold")
13 - Seach - / Track - (and Manual Tune - / Fast rwd with "press and hold")
14 - Band cycle / Folder +
15 - Program 1-6 cycle / Folder -
16 - Sel cycle (it cycles between Fader/Balance/Loudness/Subwoofer out level/Volume gain for the current source)
"""

