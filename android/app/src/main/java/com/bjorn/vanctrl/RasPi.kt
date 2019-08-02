package com.bjorn.vanctrl

class RasPi(
    private val btService: BluetoothService
) {

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

    fun sendCommand(cmd: RaspiCommands) {
        val commandMessage = "\u0002${cmd.value}\u0002"
        println("GOT MESSAGE:")
        println(commandMessage)

        if (!btService.isConnected()) {
            btService.openConnection()
        }

        btService.write(commandMessage)
    }
}
