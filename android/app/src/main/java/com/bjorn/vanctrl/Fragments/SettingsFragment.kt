package com.bjorn.vanctrl.Fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bjorn.vanctrl.R


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener{

    private lateinit var callback: OnSettingChangedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSettingChangedListener) {
            callback = context
        }

    }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val applyButton = findPreference<Preference>(getString(R.string.key_pref_apply_connection_changes))
        applyButton?.setOnPreferenceClickListener {callback.onSynchronizeClicked(); true}
    }

    interface OnSettingChangedListener {
        fun onSettingChanged(prefs: SharedPreferences?, key:String?)
        fun onSynchronizeClicked()
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {

        callback.onSettingChanged(p0, p1)
    }



//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        supportFragmentManager.beginTransaction().replace(android.R.id.content, MyPreferenceFragment()).commit()
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_settings, container, false)
//    }

}
