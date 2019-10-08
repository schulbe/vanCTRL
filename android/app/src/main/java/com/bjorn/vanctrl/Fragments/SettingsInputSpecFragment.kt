package com.bjorn.vanctrl.Fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.bjorn.vanctrl.R


class SettingsInputSpecFragment : PreferenceFragmentCompat()
{

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_input_spec)
    }


}
