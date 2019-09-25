package com.bjorn.vanctrl

import

enum class Settings(val code: String) {

    //Should be set within App lateron
    FRONT_LIGHT_SWITCH("S1"),
    BACK_LIGHT_SWITCH("S2"),
    FRIDGE_SWITCH("S3"),
    RADIO_SWITCH("S4"),

    BATTERY_LOAD_IN("I1")
    MPPT_LOAD_IN("I2")
    MPPT_CHARGE_IN("I3")

    UNKNOWN(1000);



    companion object {
        private val map = RaspiCodes.values().associateBy(RaspiCodes::code)
        fun internalName(type: Int) = map[type]?: UNKNOWN
    }

}