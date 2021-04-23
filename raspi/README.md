# RASPI

This folder contains the source code for the raspi controller to work. 

## Overview of Files and Folders
* **src/**: Contains the actual source code in Python (should mostly be understandable by looking at the comments)
Basically *server.py* executes an infinite loop and creates instances of *BluetoothCOntroller* (for communication),
*GpioController* (for switching and reading of PIN levels) and *RadioController* (for interacting with the car stereo, experimental).

* **config.ini**: Contains information about the hardware (GPIO Pins) used by the RasPi for what purpose.
Since a specific switch (for example) will turn on when the Power on the connected GPIO Pin is set to HIGH.
Also Power measurement units need to be accessed via a specific hardware address that is defined by the way the components
are soldered. Lastly this file stores the Codes used for Bluetooth communication.
* **Pipfile**: Pip-packages to be installed on the raspberry

* **start_server.sh**: Main entrypoint that gets called by the crontab when the raspi starts.