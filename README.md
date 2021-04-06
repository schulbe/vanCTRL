# vancontrol
## Purpose of this project
I bought an empty van a few years back and converted it to a camper. Since I was a student, I was dependend on
using as little money as possible to do so. After some years of camping I noticed tow major issues that annoyed me:
1. I never knew how much power was generated by my solar panel or used by my devices and had tehrefore
no idea how much I had left to use in my battery. It is thus hard to plan ahead when camping off the grid.
1. The biggest drain on my battery is my fridge which just knows the setting "very very cold" and "off".
To reduce power consumption significantly it would be good to have some sort of smart device switching it on and off 
dependend on measured temperature (or power left in the battery or alike)

Devices that can do all this do exist but they are not cheap. I therefore decided to build one of my own using a
RaspberryPi that I had lying around.

## General Remarks
* The solution I chose is highly specific and will most likely not be useable "as is" for anyone else,
 but it might present some ideas to get someone with similar intentions started.
* It is a work in progress that I did not have too much time to work on during the past year or so. 
It will probably never be fully finished.
* While I am used to working with Python, I also needed skills in app programming (Kotlin) and
the design and actual soldering of electric circuits which I didn't have. I tought myself as I went along wth this project,
thus I guess there is a lot of room for improvement here - it does work though and I got better step by step.
* To keep it cheap I chose to solder building blocks from cheap components myself instead of buying "out-of-the-box" 
solutions, where it made sense. This CAN be done simpler but I guess not much cheaper. 
The overall cost without the Pi do not exceed an estimated 30€.

## Basic Functionality
* A RaspberryPi (Version 3 for me but could probably be done with a Zero as well) serves as control unit for actual
physical measuring and switching. It is therefore connected to
    * A switchboard of 8 Relais that can be turned on and off individually to control up to 8 12V devices.
    * up to 3 units measuring Voltage drop across so called "shunts". This makes it possible to measure voltage and current across 3 different wires.
    * up to 3 temperature sensors
* An Android Device communicates with the RasPi via Bluetooth and can
    * Receive current and voltage measurements, display them and calculate overall power consumption (or gain from a solar panel)
    * Receive and display Temperature measurements
    * Receive status of switches (on or off) and send commands to actually switch them
    * Enable a user to change display names, shunt sizes or calculations using a settings tab.
    * {in the future} Enable the user to determine custom rules, for example
    "If Temperature1(Inside fridge) rises above 6°C turn on Switch3(Fridge)" and "If Temperature1(Inside fridge) falls below 3°C turn off Switch3(Fridge)"
    * {in the future} Interact with the car radio
    
The interface can be found further down.

## Structure of Project
The Project contains 3 main parts:
* *android* contains the gradle project for the Android App written in Kotlin
* *hardware* contains explanations and pictures for the single building blocks of hardware
* *raspi* contains the Code used for the control station.

## APP UI
At the moment, the app is in german language but by adjusting *vancontrol/android/app/src/main/res/values/strings.xml* translation is simple.

### Home Screen
Opens when starting the app. If Bluetooth switched off, it asks to turn on Bluetooth:

![Turn on Bluetooth?](resources/App%20Screenshots/padded/turn_on_bt.png) 

Then it tries to connect to the RaspberryPi (indicated by the Bluetooth Banner with the loading sphere on Top):

![Connecting to Raspberry Pi](resources/App%20Screenshots/connecting_bt.jpg)

When it is connected, the scrollable main screen shows up to 4 sections: 3 for different power measurements and one for different temperatures.
Names and visibility can be adjusted in the *settings* section.
**IMG**

### Switches Screen
Here you can find Images representing different switches (just 4 at the moment but they are supposed to move to the settings section as well)
When clicking a switch, the icon turns from greyscale to coloured indicating that the representative device is turned on.

![All Switches OFF](resources/App%20Screenshots/switches_off.jpg)
![Two Switches ON](resources/App%20Screenshots/switches_on.jpg)

### Radio Screen
*Experimental* My old JVC Radio uses binary codes sent to it via a pulldown resistor towards their "remote" input.
There is no official documentation but bits and pieces of information can be found in forums. I am in the process of
figuring out the corrct codes to issue Volume up /down, Play, Stop, etc commands. To do so, i am using this input. Will be replaced by actual 
play /stop /vol buttons when I found all the commands

![Send Codes to Radio](resources/App%20Screenshots/radio.jpg)

### Settings Screen
Some Basic settings need for the bluetooth connection to work (for dev purposes) under "Verbindungen"

![Main Settings](resources/App%20Screenshots/basic_settings.jpg)

Adjusting the measurements (visibility, names, specification) on the Home Screen can be done under "Anschlüsse"

![Adjust Power Settings](resources/App%20Screenshots/power_settings.jpg)
![Adjust Temperature Sensor Settings](resources/App%20Screenshots/temp_settings.jpg)

-----------------------------
## Too technical for Readme.md

Raspi should be configured to start serving script on startup. I used `sudo crontab -e` for this purpose.

## Config Files

## Further Reading
[Specification of Bluetooth Messages passed between RasPi and Android App](resources/docs/CommunicationProtocol.md)
[Explanation of Configuration Files](resources/docs/ConfigurationFiles.md)