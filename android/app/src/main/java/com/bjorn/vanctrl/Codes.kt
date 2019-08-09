package com.bjorn.vanctrl

enum class RaspiCodes(val code: Int) {

    SWITCH_FRONT_LIGHT_ON(1),
    SWITCH_FRONT_LIGHT_OFF(2),
    SWITCH_FRONT_LIGHT_TOGGLE(3),
    SWITCH_BACK_LIGHT_ON(4),
    SWITCH_BACK_LIGHT_OFF(5),
    SWITCH_BACK_LIGHT_TOGGLE(6),
    SWITCH_FRIDGE_ON(7),
    SWITCH_FRIDGE_OFF(8),
    SWITCH_FRIDGE_TOGGLE(9),
    SWITCH_RADIO_ON(10),
    SWITCH_RADIO_OFF(11),
    SWITCH_RADIO_TOGGLE(12),

    SEND_STATISTICS_START(50),
    SEND_STATISTICS_STOP(51),
    SEND_SWITCH_STATUS(52),

    PFX_STATISTICS(70),
    PFX_SWITCH_STATUS(71),

    STAT_BATTERY_VOLT(100),
    STAT_BATTERY_AMP(101),
    STAT_SOLAR_VOLT(102),
    STAT_SOLAR_AMP(103),

    FRONT_LIGHT_SWITCH(120),
    BACK_LIGHT_SWITCH(121),
    FRIDGE_SWITCH(122),
    RADIO_SWITCH(123),

    UNKNOWN(1000);



    companion object {
        private val map = RaspiCodes.values().associateBy(RaspiCodes::code)
        fun fromCode(type: Int) = map[type]?: UNKNOWN
    }

}