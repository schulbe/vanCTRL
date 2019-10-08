package com.bjorn.vanctrl

enum class RaspiCodes(val code: String) {

    COMMAND_FLAG("C"),

    CMD_SWITCH_ON("C1"),
    CMD_SWITCH_OFF("C2"),
    CMD_SWITCH_TOGGLE("C3"),
    CMD_SEND_DATA("C4"),

    DATA_FLAG("D"),

    DATA_POWER_MEASUREMENTS("D1"),
    DATA_TEMPERATURE_MEASUREMENTS("D2"),
    DATA_SWITCH_STATUS("D3"),
    DATA_INPUT_SPECS("D4"),

    SWITCH_1("S1"),
    SWITCH_2("S2"),
    SWITCH_3("S3"),
    SWITCH_4("S4"),
    SWITCH_5("S5"),
    SWITCH_6("S6"),
    SWITCH_7("S7"),
    SWITCH_8("S8"),

    INPUT_1("I1"),
    INPUT_2("I2"),
    INPUT_3("I3"),
    INPUT_4("I4"),
    INPUT_5("I5"),


    UNKNOWN("U");



    companion object {
        private val map = values().associateBy(RaspiCodes::code)
        fun fromCode(type: String) = map[type]?: UNKNOWN
        fun fromSetting(set: Settings) = fromCode(set.code)
    }
}