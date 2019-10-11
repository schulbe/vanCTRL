# necessary so Bluetoothprofile is loaded before starting script at startup from "crontab -e"

sleep 5

/home/pi/.local/share/virtualenvs/raspi-o3thuDf6/bin/python /home/pi/repos/vancontrol/raspi/src/server.py &

