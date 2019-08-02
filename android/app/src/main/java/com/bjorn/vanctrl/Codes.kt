package com.bjorn.vanctrl

enum class RaspiCommands(val value: Int) {

    SWITCH_FRONT_LIGHT_ON(1),
    SWITCH_FRONT_LIGHT_OFF(2),
    SWITCH_BACK_LIGHT_ON(3),
    SWITCH_BACK_LIGHT_OFF(4),
    GET_POWER_MEASUREMENTS(5),
    CLOSE_CONNECTION(100)
}