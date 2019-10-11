package com.bjorn.vanctrl

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.bjorn.vanctrl.Fragments.SettingsFragment
import com.bjorn.vanctrl.Fragments.SwitchesFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity(),
    SwitchesFragment.OnSwitchChangedListener,
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    SettingsFragment.OnSettingChangedListener
{

    private var piMacAddress: String? = null
    private var piBtName: String? = null
    private lateinit var piUUID: UUID

    private lateinit var rasPi: BluetoothService
    private lateinit var navController: NavController
    private lateinit var viewModel: VanViewModel
    private lateinit var measurementController: MeasurementController
    private lateinit var switchController: SwitchController

    private val handler: Handler = Handler()

    private var mIsInForeground: Boolean = true

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_overview -> {
                viewModel.setActiveFragment(R.id.overviewFragment)
                navController.navigate(R.id.overviewFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_switches -> {
                viewModel.setActiveFragment(R.id.switchesFragment)
                navController.navigate(R.id.switchesFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                viewModel.setActiveFragment(R.id.settingsFragment)
                navController.navigate(R.id.settingsFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_radio -> {
                viewModel.setActiveFragment(R.id.radioFragment)
                navController.navigate(R.id.radioFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        measurementController = MeasurementController(this)
        switchController = SwitchController(this)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        viewModel = ViewModelProviders.of(this)[VanViewModel::class.java]
        viewModel.initalizeLiveData()

        piMacAddress = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_pref_mac_address), "")
        piBtName = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_pref_raspi_name), "")
        piUUID = UUID.fromString(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_pref_uuid), ""))

        rasPi = BluetoothService(this.piUUID,this, MessageProcessor(viewModel))

        setObservers()

        viewModel.setActiveFragment(R.id.overviewFragment)

    }

    override fun onPause() {
        super.onPause()
        mIsInForeground = false
    }

    override fun onResume() {
        super.onResume()
        mIsInForeground = true
    }

    override fun onSwitchClicked(switchId: Int) {
        when(switchId) {
            R.id.kitchenlightButton -> {rasPi.sendCommand(RaspiCodes.CMD_SWITCH_TOGGLE, RaspiCodes.fromSetting(Settings.FRONT_LIGHT_SWITCH))
                                        viewModel.toggleSwitchStatus(Settings.FRONT_LIGHT_SWITCH) }
            R.id.bedlightButton -> {rasPi.sendCommand(RaspiCodes.CMD_SWITCH_TOGGLE, RaspiCodes.fromSetting(Settings.BACK_LIGHT_SWITCH))
                                    viewModel.toggleSwitchStatus(Settings.BACK_LIGHT_SWITCH)}
            R.id.fridgeButton -> {rasPi.sendCommand(RaspiCodes.CMD_SWITCH_TOGGLE, RaspiCodes.fromSetting(Settings.FRIDGE_SWITCH))
                                  viewModel.toggleSwitchStatus(Settings.FRIDGE_SWITCH)}
            R.id.radioButton -> {rasPi.sendCommand(RaspiCodes.CMD_SWITCH_TOGGLE, RaspiCodes.fromSetting(Settings.RADIO_SWITCH))
                                 viewModel.toggleSwitchStatus(Settings.RADIO_SWITCH) }
        }
//        // TODO: Trigger return on switch status change on serverside
        GlobalScope.launch{
            delay(300)
            rasPi.sendCommand(RaspiCodes.CMD_SEND_DATA, RaspiCodes.DATA_SWITCH_STATUS)
        }

    }

    private fun onConnectBtButtonClick() {
        val setVisible = Runnable { findViewById<FrameLayout>(R.id.workingOverlay)?.apply{ visibility = View.VISIBLE } }
        val setInvisible = Runnable { findViewById<FrameLayout>(R.id.workingOverlay)?.apply{ visibility = View.GONE } }

        GlobalScope.launch {
            handler.post(setVisible)
            rasPi.tryConnection(piMacAddress, piBtName)
            handler.post(setInvisible)
        }
    }

    private fun setObservers() {
        viewModel.getPowerStats().observe(this, Observer<Map<Settings, Map<String, Float>>>{ measurements -> measurementController.processRealtimePowerMeasurements(measurements) })
        viewModel.getSwitchStatus().observe(this, Observer<Map<Settings, Boolean>>{ status -> switchController.setButtonImages(status)})
        viewModel.getFragmentTitle().observe(this, Observer<Int> {fragmentId -> onFragmentChange(fragmentId)})
        viewModel.getTemperatures().observe(this, Observer<Map<Settings, Float>> { temperatures -> measurementController.processRealtimeTemperatureMeasurements(temperatures)})

        rasPi.isConnected().observe(this, Observer<Boolean>{isConnected -> setConnectionBanner(isConnected)})

        findViewById<ImageButton>(R.id.btConnectButton)?.setOnClickListener{ _ -> onConnectBtButtonClick()}

    }

    private fun setConnectionBanner(isConnected: Boolean) {
        if (isConnected) {
            findViewById<ImageButton>(R.id.btConnectButton)?.apply{
                visibility = View.GONE
            }
        } else {
            findViewById<ImageButton>(R.id.btConnectButton)?.apply{
                visibility = View.VISIBLE
            }
        }
    }

    private fun launchUpdateStatistics() {
        GlobalScope.launch {
            while (viewModel.getFragmentTitle().value == R.id.overviewFragment) {
                rasPi.sendCommand(RaspiCodes.CMD_SEND_DATA, RaspiCodes.DATA_POWER_MEASUREMENTS)
                delay(2000)
            }
        }
        GlobalScope.launch {
            while (viewModel.getFragmentTitle().value == R.id.overviewFragment) {
                rasPi.sendCommand(RaspiCodes.CMD_SEND_DATA, RaspiCodes.DATA_TEMPERATURE_MEASUREMENTS)
                delay(5000)
            }

        }
    }

    private fun onFragmentChange(fragmentId: Int) {
        when (fragmentId) {
            R.id.overviewFragment -> {
                launchUpdateStatistics()
            }
            R.id.switchesFragment -> {
                rasPi.sendCommand(RaspiCodes.CMD_SEND_DATA, RaspiCodes.DATA_SWITCH_STATUS)
            }
            R.id.radioFragment -> {
            }
            R.id.settingsFragment -> {
            }
            R.id.settingsInputSpecFragment -> {
            }
        }
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        // Instantiate the new Fragment
        // TODO: Also non hardcoded way?
        val args = pref.extras
        var id = R.id.settingsFragment
        when (pref.fragment) {
            "com.bjorn.vanctrl.Fragments.SettingsInputSpecFragment" -> {
                id=R.id.settingsInputSpecFragment
            }
            "com.bjorn.vanctrl.Fragments.SettingsOverviewFragment" -> {
                id=R.id.settingsOverviewFragment
            }
        }

        viewModel.setActiveFragment(id)
        navController.navigate(id)

        return true
    }

    override fun onSettingChanged(prefs: SharedPreferences?, key: String?) {
        GlobalScope.launch {
            when {
                key?.contains("in1") ?: false -> {
                    if (prefs?.getBoolean(getString(R.string.key_pref_connected_in1), false) == true) {
                        rasPi.sendData(
                            RaspiCodes.DATA_INPUT_SPECS,
                            listOf(
                                RaspiCodes.INPUT_1.code,
                                prefs.getString(getString(R.string.key_pref_shunt_a_in1), "") ?: "",
                                prefs.getString(getString(R.string.key_pref_shunt_mv_in1), "") ?: "",
                                prefs.getString(getString(R.string.key_pref_max_v_in1), "") ?: ""
                            )
                        )
                    }
                }
                key?.contains("in2") ?: false -> {
                    if (prefs?.getBoolean(getString(R.string.key_pref_connected_in2), false) == true) {
                        rasPi.sendData(
                            RaspiCodes.DATA_INPUT_SPECS,
                            listOf(
                                RaspiCodes.INPUT_2.code,
                                prefs.getString(getString(R.string.key_pref_shunt_a_in2), "") ?: "",
                                prefs.getString(getString(R.string.key_pref_shunt_mv_in2), "") ?: "",
                                prefs.getString(getString(R.string.key_pref_max_v_in2), "") ?: ""
                            )
                        )
                    }
                }
                key?.contains("in3") ?: false -> {
                    if (prefs?.getBoolean(getString(R.string.key_pref_connected_in3), false) == true) {
                        rasPi.sendData(
                            RaspiCodes.DATA_INPUT_SPECS,
                            listOf(
                                RaspiCodes.INPUT_3.code,
                                prefs.getString(getString(R.string.key_pref_shunt_a_in3), "") ?: "",
                                prefs.getString(getString(R.string.key_pref_shunt_mv_in3), "") ?: "",
                                prefs.getString(getString(R.string.key_pref_max_v_in3), "") ?: ""
                            )
                        )
                    }
                }
                key?.contains("in4") ?: false -> {
                    if (prefs?.getBoolean(getString(R.string.key_pref_connected_in4), false) == true) {
                        rasPi.sendData(
                            RaspiCodes.DATA_INPUT_SPECS,
                            listOf(
                                RaspiCodes.INPUT_4.code,
                                prefs.getString(getString(R.string.key_pref_id_in4), "") ?: ""
                            )
                        )
                    }
                }
                key?.contains("in5") ?: false -> {
                    if (prefs?.getBoolean(getString(R.string.key_pref_connected_in4), false) == true) {
                        rasPi.sendData(
                            RaspiCodes.DATA_INPUT_SPECS,
                            listOf(
                                RaspiCodes.INPUT_5.code,
                                prefs.getString(getString(R.string.key_pref_id_in4), "") ?: ""
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onSynchronizeClicked() {
        finish()
        startActivity(intent)
    }
}
