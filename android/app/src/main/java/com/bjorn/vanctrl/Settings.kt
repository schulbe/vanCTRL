package com.bjorn.vanctrl


enum class Settings(val code: String) {

    //Should be set within App lateron
    FRONT_LIGHT_SWITCH("S1"),
    BACK_LIGHT_SWITCH("S2"),
    FRIDGE_SWITCH("S3"),
    RADIO_SWITCH("S4"),

    IN_1("I1"),
    IN_2("I2"),
    IN_3("I3"),

    IN_4("I4"),
    IN_5("I5"),

    UNKNOWN("U");



    companion object {
        private val map = Settings.values().associateBy(Settings::code)
        fun fromCode(code: String) = map[code]?: UNKNOWN
    }
}