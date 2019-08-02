package com.bjorn.vanctrl

class RasPi {

    fun getPowerMeasurements(): Map<String, Float> {

        val vBattery = (0..20).random().toFloat()
        val aBattery = (0..20).random().toFloat()
        val aSolar = (0..20).random().toFloat()

        return mapOf(
            "vBat" to vBattery,
            "aBat" to aBattery,
            "aSol" to aSolar
        )

    }
}
