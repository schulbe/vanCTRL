package com.bjorn.vanctrl

import android.widget.ImageButton

class SwitchController (private val activity: MainActivity) {
    fun setButtonImages(status: Map<Settings, Boolean>) {
        val kitchenlightButton = activity.findViewById<ImageButton>(R.id.kitchenlightButton)
        if (status[Settings.FRONT_LIGHT_SWITCH] == true){
            kitchenlightButton?.setImageResource(R.drawable.ic_kitchenlight_on)
        } else kitchenlightButton?.setImageResource(R.drawable.ic_kitchenlight_off)

        val bedlightButton = activity.findViewById<ImageButton>(R.id.bedlightButton)
        if (status[Settings.BACK_LIGHT_SWITCH] == true){
            bedlightButton?.setImageResource(R.drawable.ic_bedlight_on)
        } else bedlightButton?.setImageResource(R.drawable.ic_bedlight_off)

        val fridgeButton = activity.findViewById<ImageButton>(R.id.fridgeButton)
        if (status[Settings.FRIDGE_SWITCH] == true){
            fridgeButton?.setImageResource(R.drawable.ic_fridge_on)
        } else fridgeButton?.setImageResource(R.drawable.ic_fridge_off)

        val radioButton = activity.findViewById<ImageButton>(R.id.radioButton)
        if (status[Settings.RADIO_SWITCH] == true){
            radioButton?.setImageResource(R.drawable.ic_radio_on)
        } else radioButton?.setImageResource(R.drawable.ic_radio_off)
    }


}