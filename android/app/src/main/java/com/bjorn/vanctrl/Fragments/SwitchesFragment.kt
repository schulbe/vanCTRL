package com.bjorn.vanctrl.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.bjorn.vanctrl.R


class SwitchesFragment : Fragment() {

    private lateinit var callback: OnSwitchChangedListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnSwitchChangedListener) {
            callback = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val frontSwitch = getView()?.findViewById<Switch>(R.id.frontLightSwitch)
        frontSwitch?.setOnCheckedChangeListener {_, isChecked -> callback.switch("FRONT_LIGHT", isChecked)}

        val backSwitch = getView()?.findViewById<Switch>(R.id.backLightSwitch)
        backSwitch?.setOnCheckedChangeListener {_, isChecked -> callback.switch("BACK_LIGHT", isChecked)}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_switches, container, false)
    }

    interface OnSwitchChangedListener {
        fun switch(what: String, on:Boolean)
    }

}
