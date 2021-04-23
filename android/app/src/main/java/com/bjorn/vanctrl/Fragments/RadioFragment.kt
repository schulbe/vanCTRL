package com.bjorn.vanctrl.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.bjorn.vanctrl.R


class RadioFragment : Fragment() {

    private lateinit var callback: OnRadioButtonClickedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RadioFragment.OnRadioButtonClickedListener) {
            callback = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editText = getView()?.findViewById<EditText>(R.id.editTextRadio)

        getView()?.findViewById<Button>(R.id.buttonRadio)
            ?.setOnClickListener {
                callback.onRadioButtonClicked(editText?.text.toString()) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_radio, container, false)
    }

    interface OnRadioButtonClickedListener {
        fun onRadioButtonClicked(code: String)
    }

}
