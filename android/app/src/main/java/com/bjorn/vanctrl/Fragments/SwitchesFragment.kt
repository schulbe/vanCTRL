package com.bjorn.vanctrl.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.bjorn.vanctrl.R


class SwitchesFragment : Fragment() {

    private lateinit var callback: OnSwitchChangedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSwitchChangedListener) {
            callback = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getView()?.findViewById<ImageButton>(R.id.bedlightButton)?.setOnClickListener {callback.onSwitchClicked(R.id.bedlightButton)}
        getView()?.findViewById<ImageButton>(R.id.kitchenlightButton)?.setOnClickListener {callback.onSwitchClicked(R.id.kitchenlightButton)}
        getView()?.findViewById<ImageButton>(R.id.fridgeButton)?.setOnClickListener {callback.onSwitchClicked(R.id.fridgeButton)}
        getView()?.findViewById<ImageButton>(R.id.radioButton)?.setOnClickListener {callback.onSwitchClicked(R.id.radioButton)}

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_switches, container, false)
    }

    interface OnSwitchChangedListener {
        fun onSwitchClicked(switchId: Int)
    }

}
