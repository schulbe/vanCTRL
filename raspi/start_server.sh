# necessary so Bluetoothprofile is loaded before starting script at startup from "crontab -e"
sleep 5

# Also this needs to be run as sudo (for radio controller)
pigpiod

# Start regular server
/home/pi/.local/share/virtualenvs/raspi-o3thuDf6/bin/python /home/pi/repos/vancontrol/raspi/src/server.py &

