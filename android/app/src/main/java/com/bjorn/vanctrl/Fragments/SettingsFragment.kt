package com.bjorn.vanctrl.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.appcompat.widget.AppCompatButton
import com.bjorn.vanctrl.R


class SettingsFragment : Fragment() {

    private lateinit var callback: OnBluetoothButtonClickedListener


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnBluetoothButtonClickedListener) {
            callback = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonConnectBt = getView()?.findViewById<AppCompatButton>(R.id.buttonConnectBt)
        buttonConnectBt?.setOnClickListener {_ -> callback.connectBluetoothDevice()}

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    interface OnBluetoothButtonClickedListener {
        fun connectBluetoothDevice()
    }
}
