# vancontrol
## REMEMBER:
sudo crontab -e 

## Config Files

## Communication Interface
### General
Every Message sent starts with an indicator of what the message contains (in numerical Code):
- COMMAND_FLAG: Followed by further instructions of what to do 
- DATA_FLAG: Followed by Information

### COMMAND
A Full command looks like this:

\u0002COMMAND_FLAG\u0003COMMAND\u003[{details if needed}]\u0002

#### COMMAND = CMD_SWITCH_ON
details=SWITCH_NUMBER (get from config screen later, now from config file)
SWITCH_NUMBER is between 1 and 8

#### COMMAND = CMD_SWITCH_OFF
details=SWITCH_NUMBER (get from config screen later, now from config file)
SWITCH_NUMBER is between 1 and 8

#### COMMAND = CMD_SWITCH_TOGGLE
details=SWITCH_NUMBER (get from config screen later, now from config file)
SWITCH_NUMBER is between 1 and 8

#### COMMAND = CMD_SEND_DATA
details=DATA_TYPE
DATA_TYPE is one of (POWER_MEASUREMENT, TEMPERATURE_MEASUREMENT, SWITCH_STATUS)

### DATA
\u0002DATA_FLAG\u0003DATA_TYPE\u0003DATA\u0002

#### DATA_TYPE = POWER_MEASUREMENT
DATA = IN_1_AMPERE\u0004IN_1_VOLT\u0004IN_2_AMPERE\u0004IN_2_VOLT\u0004IN_3_AMPERE\u0004IN_3_VOLT\u0004

#### DATA_TYPE = TEMPERATURE_MEASUREMENT

#### DATA_TYPE = SWITCH_STATUS
DATA = STATUS_SWITCH_1\u0004STATUS_SWITCH_2\u0004....\u0004STATUS_SWITCH_8

### Read Measurements