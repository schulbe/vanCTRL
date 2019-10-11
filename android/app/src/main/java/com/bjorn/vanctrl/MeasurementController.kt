package com.bjorn.vanctrl

import android.widget.TextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MeasurementController(private val activity: MainActivity) {

    fun processRealtimePowerMeasurements(measurements: Map<Settings, Map<String, Float>>) {
        GlobalScope.launch{
            setRealtimePowerMeasurementsToUi(measurements)
        }
    }

    fun processRealtimeTemperatureMeasurements(temperatures: Map<Settings,Float>) {
        GlobalScope.launch {
            setRealtimeTemperatureMeasurementsToUI(temperatures)
        }
    }

    private fun setRealtimePowerMeasurementsToUi(measurements: Map<Settings, Map<String, Float>>) {
        val overall_amp = measurements[Settings.IN_1]?.get("A")
        val overall_volt = measurements[Settings.IN_1]?.get("V")
        val mppt_solar_amp = measurements[Settings.IN_2]?.get("A")
        val mppt_solar_volt = measurements[Settings.IN_2]?.get("V")
        val mppt_load_amp = measurements[Settings.IN_3]?.get("A")
        val mppt_load_volt = measurements[Settings.IN_3]?.get("V")

        var load_amp = mppt_load_amp?.plus(mppt_solar_amp?:0f)
        load_amp = overall_amp?.minus(load_amp?:0f)
        load_amp = load_amp?.plus(mppt_load_amp?:0f)


        var uiText = "%.2f V".format(mppt_load_volt)
        activity.findViewById<TextView>(R.id.overviewInp1VoltageView)?.apply {
            text = uiText
        }

        uiText = "%.2f A".format(mppt_load_volt)
        activity.findViewById<TextView>(R.id.overviewInp1AmpView)?.apply {
            text = uiText
        }
        var power = load_amp?.times(mppt_load_volt?: 0f)
        uiText = "%.2f W".format((power))
        activity.findViewById<TextView>(R.id.overviewInp1PowerView)?.apply {
            text = uiText
        }

        uiText = "%.2f V".format(mppt_solar_volt)
        activity.findViewById<TextView>(R.id.overviewInp2VoltageView)?.apply {
            text = uiText
        }
        uiText = "%.2f A".format(mppt_solar_amp)
        activity.findViewById<TextView>(R.id.overviewInp2AmpView)?.apply {
            text = uiText
        }
        power = mppt_solar_amp?.times(mppt_solar_volt?: 0f)
        uiText = "%.2f W".format(power)
        activity.findViewById<TextView>(R.id.overviewInp2PowerView)?.apply {
            text = uiText
        }

        uiText = "%.2f V".format(overall_volt)
        activity.findViewById<TextView>(R.id.overviewInp3VoltageView)?.apply {
            text = uiText
        }
        uiText = "%.2f A".format(overall_amp)
        activity.findViewById<TextView>(R.id.overviewInp3AmpView)?.apply {
            text = uiText
        }
        power = overall_amp?.times(overall_volt?: 0f)
        uiText = "%.2f W".format(power)
        activity.findViewById<TextView>(R.id.overviewInp3PowerView)?.apply {
            text = uiText
        }

    }

    private fun setRealtimeTemperatureMeasurementsToUI(temperatures: Map<Settings,Float>) {

        var temp = temperatures[Settings.IN_4]
        var uiText = "%.2f °C".format(temp)
        activity.findViewById<TextView>(R.id.overviewInp4TemperatureView)?.apply {
            text = uiText
        }

        temp = temperatures[Settings.IN_5]
        uiText = "%.2f °C".format(temp)
        activity.findViewById<TextView>(R.id.overviewInp5TemperatureView)?.apply {
            text = uiText
        }
    }
}