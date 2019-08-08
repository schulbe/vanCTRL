package com.bjorn.vanctrl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VanViewModel : ViewModel() {
    private val statistics = MutableLiveData<Map<RaspiCodes, Float>>()
    private val activeFragment = MutableLiveData<Int>()
    private val btConnected = MutableLiveData<Boolean>()
    private val switchStatus = MutableLiveData<Map<String, Boolean>>()

//    fun setStatistics(vBattery: Float, aBattery: Float, aSolar: Float) {
//        statistics.postValue(mapOf(
//            RaspiCodes.STAT_BATTERY_VOLT to vBattery,
//            RaspiCodes.STAT_BATTERY_AMP to aBattery,
//            RaspiCodes.STAT_SOLAR_AMP to aSolar
//        ))
//    }

    fun setSwitchStatus(bedLight:Boolean, kitchenLight:Boolean, fridge:Boolean, radio:Boolean) {
        switchStatus.postValue(mapOf(
            "bedLight" to bedLight,
            "kitchenLight" to kitchenLight,
            "fridge" to fridge,
            "radio" to radio
        ))
    }

    fun toggleSwitchStatus(what: String) {
        val current = switchStatus.value!!.toMutableMap()
        current[what] = !(current[what]?: true)
        switchStatus.postValue(current.toMap())
    }

    fun getSwitchStatus(): LiveData<Map<String, Boolean>>{
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