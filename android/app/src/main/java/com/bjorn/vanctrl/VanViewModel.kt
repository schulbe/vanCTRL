package com.bjorn.vanctrl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VanViewModel : ViewModel() {
    private val powerStats = MutableLiveData<Map<Settings, Map<String, Float>>>()
    private val activeFragment = MutableLiveData<Int>()
    private val btConnected = MutableLiveData<Boolean>()
    private val switchStatus = MutableLiveData<Map<Settings, Boolean>>()
    private val temperatures = MutableLiveData<Map<Settings, Float>>()

    fun initalizeLiveData() {
        switchStatus.postValue(mapOf(
            Settings.FRONT_LIGHT_SWITCH to false,
            Settings.BACK_LIGHT_SWITCH to false,
            Settings.FRIDGE_SWITCH to false,
            Settings.RADIO_SWITCH to false
        ))

    }

    fun toggleSwitchStatus(what: Settings) {
        val current = switchStatus.value!!.toMutableMap()
        current[what] = !(current[what]?: true)
        switchStatus.postValue(current.toMap())
    }

    fun setSwitchStatus(status: Map<Settings, Boolean>) {
        switchStatus.postValue(status)
    }

    fun getSwitchStatus(): LiveData<Map<Settings, Boolean>>{
        return switchStatus
    }

    fun setPowerStats(measurements: Map<Settings, Map<String, Float>>) {
        powerStats.postValue(measurements)
    }

    fun getPowerStats(): LiveData<Map<Settings, Map<String, Float>>> {
        return powerStats
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

    fun setTemperatures(temps: Map<Settings, Float>) {
        temperatures.postValue(temps)
    }

    fun getTemperatures(): LiveData<Map<Settings, Float>>{
        return temperatures
    }
}