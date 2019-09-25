package com.bjorn.vanctrl

enum class RaspiCodes(val code: String) {

    COMMAND_FLAG("C")

    CMD_SWITCH_ON("C1"),
    CMD_SWITCH_OFF("C2"),
    CMD_SWITCH_TOGGLE("C3"),
    CMD_SEND_DATA("C4")

    DATA_FLAG("D")

    DATA_POWER_MEASUREMENTS("D1")
    DATA_TEMPERATURE_MEASUREMENTS("D2")
    DATA_SWITCH_STATUS("D3")


//    SWITCH_FRONT_LIGHT_ON(1),
//    SWITCH_FRONT_LIGHT_OFF(2),
//    SWITCH_FRONT_LIGHT_TOGGLE(3),
//    SWITCH_BACK_LIGHT_ON(4),
//    SWITCH_BACK_LIGHT_OFF(5),
//    SWITCH_BACK_LIGHT_TOGGLE(6),
//    SWITCH_FRIDGE_ON(7),
//    SWITCH_FRIDGE_OFF(8),
//    SWITCH_FRIDGE_TOGGLE(9),
//    SWITCH_RADIO_ON(10),
//    SWITCH_RADIO_OFF(11),
//    SWITCH_RADIO_TOGGLE(12),

//    SEND_STATISTICS_START(50),
//    SEND_STATISTICS_STOP(51),
//    SEND_SWITCH_STATUS(52),
//    SEND_STATISTICS(53),
//
//    PFX_STATISTICS(70),
//    PFX_SWITCH_STATUS(71),
//
//    STAT_BATTERY_VOLT(100),
//    STAT_BATTERY_AMP(101),
//    STAT_SOLAR_VOLT(102),
//    STAT_SOLAR_AMP(103),

    SWITCH_1("S1"),
    SWITCH_2("S2"),
    SWITCH_3("S3"),
    SWITCH_4("S4"),
    SWITCH_5("S5"),
    SWITCH_6("S6"),
    SWITCH_7("S7"),
    SWITCH_8("S8"),

    INPUT_1("I1")
    INPUT_2("I2")
    INPUT_3("I3")

    UNKNOWN(1000);



    companion object {
        private val map = RaspiCodes.values().associateBy(RaspiCodes::code)
        fun fromCode(type: Int) = map[type]?: UNKNOWN
    }

}