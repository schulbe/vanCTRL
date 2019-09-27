package com.bjorn.vanctrl


enum class Settings(val code: String) {

    //Should be set within App lateron
    FRONT_LIGHT_SWITCH("S1"),
    BACK_LIGHT_SWITCH("S2"),
    FRIDGE_SWITCH("S3"),
    RADIO_SWITCH("S4"),

    BATTERY_LOAD("I1"),
    MPPT_CHARGE("I2"),
    MPPT_LOAD("I3"),

    UNKNOWN("U");



    companion object {
        private val map = Settings.values().associateBy(Settings::code)
        fun fromCode(code: String) = map[code]?: UNKNOWN
    }

}