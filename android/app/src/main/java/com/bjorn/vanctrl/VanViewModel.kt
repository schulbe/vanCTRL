package com.bjorn.vanctrl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VanViewModel : ViewModel() {
    private val statistics = MutableLiveData<Map<RaspiCodes, Float>>()
    private val activeFragment = MutableLiveData<Int>()
    private val btConnected = MutableLiveData<Boolean>()
    private val switchStatus = MutableLiveData<Map<RaspiCodes, Boolean>>()

//    fun setStatistics(vBattery: Float, aBattery: Float, aSolar: Float) {
//        statistics.postValue(mapOf(
//            RaspiCodes.STAT_BATTERY_VOLT to vBattery,
//            RaspiCodes.STAT_BATTERY_AMP to aBattery,
//            RaspiCodes.STAT_SOLAR_AMP to aSolar
//        ))
//    }

//    fun setSwitchStatus(bedLight:Boolean, kitchenLight:Boolean, fridge:Boolean, radio:Boolean) {
//        switchStatus.postValue(mapOf(
//            RaspiCodes.BACK_LIGHT_SWITCH to bedLight,
//            RaspiCodes.FRONT_LIGHT_SWITCH to kitchenLight,
//            RaspiCodes.FRIDGE_SWITCH to fridge,
//            RaspiCodes.RADIO_SWITCH to radio
//        ))
//    }

    fun toggleSwitchStatus(what: RaspiCodes) {
        val current = switchStatus.value!!.toMutableMap()
        current[what] = !(current[what]?: true)
        switchStatus.postValue(current.toMap())
    }

    fun setSwitchStatus(status: Map<RaspiCodes, Boolean>) {
        switchStatus.postValue(status)
    }

    fun getSwitchStatus(): LiveData<Map<RaspiCodes, Boolean>>{
        return switchStatus
    }

    fun setStatistics(measurements: Map<RaspiCodes, Float>) {
        statistics.postValue(measurements)
    }

    fun getStatistics(): LiveData<Map<RaspiCodes, Float>>{
        return statistics
    }

    fun setActiveFragment(fragmentId:Int) {
        activeFragment.postValue(fragmentId)
    }

    fun getFragmentTitle(): LiveData<Int> {
        return activeFragment
    }

    fun getBtConnected(): LiveData<Boolean> {
        return btConnected
    }

    fun setBtConnected(connected: Boolean) {
        btConnected.postValue(connected)
    }
}