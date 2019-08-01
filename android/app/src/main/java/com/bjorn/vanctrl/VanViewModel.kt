package com.bjorn.vanctrl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VanViewModel : ViewModel() {
    private val powerMeasurements = MutableLiveData<Map<String, Float>>()
    private val fragmentTitle = MutableLiveData<String>()


    fun setPowerStatistics(vBattery: Float, aBattery: Float, aSolar: Float) {
        powerMeasurements.postValue(mapOf<String, Float>(
            "vBat" to vBattery,
            "aBat" to aBattery,
            "aSol" to aSolar
        ))
    }

    fun setPowerStatistics(measurements: Map<String, Float>) {
        powerMeasurements.postValue(measurements)
    }

    fun getPowerMeasurements(): LiveData<Map<String, Float>>{
        return powerMeasurements
    }

    fun setFragmentTitle(title:String) {
        fragmentTitle.postValue(title)
    }

    fun getFragmentTitle(): LiveData<String> {
        return fragmentTitle
    }

}