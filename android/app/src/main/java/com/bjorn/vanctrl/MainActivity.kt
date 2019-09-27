package com.bjorn.vanctrl

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bjorn.vanctrl.Fragments.SettingsFragment
import com.bjorn.vanctrl.Fragments.SwitchesFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity(),
    SwitchesFragment.OnSwitchChangedListener,
    SettingsFragment.OnBluetoothButtonClickedListener {

    val PI_MAC_ADDR: String = "B8:27:EB:C8:56:C7"
    val PI_BT_NAME: String = "raspberrypi"
    val PI_UUID: UUID = UUID.fromString("1e0ca4ea-299d-4335-93eb-27fcfe7fa849")

//    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
//    private lateinit var piBtDevice: BluetoothDevice
    private lateinit var rasPi: BluetoothService

    private lateinit var navController: NavController
    private lateinit var viewModel: VanViewModel
//    private lateinit var rasPi: RasPi

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

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        viewModel = ViewModelProviders.of(this)[VanViewModel::class.java]
        viewModel.initalizeLiveData()

        rasPi = BluetoothService(PI_UUID, this, MessageProcessor(viewModel))

//        rasPi = RasPi(btService)

        setObservers()

        //TODO: REMOVE - only test
//        viewModel.setSwitchStatus(false, false, false, false)

        viewModel.setActiveFragment(R.id.overviewFragment)
    }

    override fun onPause() {
        super.onPause()
        mIsInForeground = false
//        btService.closeConnection()
    }

    override fun onResume() {
        super.onResume()
        mIsInForeground = true
//        try {btService.openConnection()}
//        catch (e: BluetoothException){ Toast.makeText(this, "Closing Socket failed", Toast.LENGTH_LONG).show()}
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
            delay(500)
            rasPi.sendCommand(RaspiCodes.CMD_SEND_DATA, RaspiCodes.DATA_SWITCH_STATUS)
        }

    }


    override fun onConnectBtButtonClick() {
        //TODO why isnt this working

//        findViewById<ProgressBar>(R.id.progressBarSettings)?.apply{ visibility = View.VISIBLE }
        rasPi.tryConnection(PI_MAC_ADDR, PI_BT_NAME)
//        findViewById<ProgressBar>(R.id.progressBarSettings)?.apply{ visibility = View.GONE }
//        if (rasPi.isConnected().value == true) rasPi.setIsConnectedTest(false) else rasPi.setIsConnectedTest(true)


    }

    private fun setObservers() {
        viewModel.getPowerStats().observe(this, Observer<Map<Settings, Map<String, Float>>>{ measurements -> setPowerMeasurementsToUI(measurements) })
        viewModel.getSwitchStatus().observe(this, Observer<Map<Settings, Boolean>>{ status -> setButtonImages(status)})
        viewModel.getFragmentTitle().observe(this, Observer<Int> {fragmentId -> onFragmentChange(fragmentId)})

        rasPi.isConnected().observe(this, Observer<Boolean>{isConnected -> setConnectionBanner(isConnected)})

    }

    private fun setButtonImages(status: Map<Settings, Boolean>) {
        val kitchenlightButton = findViewById<ImageButton>(R.id.kitchenlightButton)
        if (status[Settings.FRONT_LIGHT_SWITCH] == true){
            kitchenlightButton?.setImageResource(R.drawable.ic_kitchenlight_on)
        } else kitchenlightButton?.setImageResource(R.drawable.ic_kitchenlight_off)

        val bedlightButton = findViewById<ImageButton>(R.id.bedlightButton)
        if (status[Settings.BACK_LIGHT_SWITCH] == true){
            bedlightButton?.setImageResource(R.drawable.ic_bedlight_on)
        } else bedlightButton?.setImageResource(R.drawable.ic_bedlight_off)

        val fridgeButton = findViewById<ImageButton>(R.id.fridgeButton)
        if (status[Settings.FRIDGE_SWITCH] == true){
            fridgeButton?.setImageResource(R.drawable.ic_fridge_on)
        } else fridgeButton?.setImageResource(R.drawable.ic_fridge_off)

        val radioButton = findViewById<ImageButton>(R.id.radioButton)
        if (status[Settings.RADIO_SWITCH] == true){
            radioButton?.setImageResource(R.drawable.ic_radio_on)
        } else radioButton?.setImageResource(R.drawable.ic_radio_off)
    }

    private fun setPowerMeasurementsToUI(measurements: Map<Settings, Map<String, Float>>) {
        var amp = measurements[Settings.BATTERY_LOAD]?.get("A")
        var volt = measurements[Settings.BATTERY_LOAD]?.get("V")
        var uiText = "%.2f V".format(volt)
        findViewById<TextView>(R.id.overviewBatteryVoltageView)?.apply {
            text = uiText
        }

        uiText = "%.2f A".format(amp)
        findViewById<TextView>(R.id.overviewBatteryAmpView)?.apply {
            text = uiText
        }
        var power = amp?.times(volt?: 0f)
        uiText = "%.2f W".format((power))
        findViewById<TextView>(R.id.overviewBatteryPowerView)?.apply {
            text = uiText
        }

        amp = measurements[Settings.MPPT_CHARGE]?.get("A")
        volt = measurements[Settings.MPPT_CHARGE]?.get("V")
        uiText = "%.2f V".format(volt)
        findViewById<TextView>(R.id.overviewSolarVoltageView)?.apply {
            text = uiText
        }
        uiText = "%.2f A".format(amp)
        findViewById<TextView>(R.id.overviewSolarAmpView)?.apply {
            text = uiText
        }
        power = amp?.times(volt?: 0f)
        uiText = "%.2f W".format(power)
        findViewById<TextView>(R.id.overviewSolarPowerView)?.apply {
            text = uiText
        }

    }

    private fun setConnectionBanner(isConnected: Boolean) {
        if (isConnected) {
            println("SET CONNECTION BANNER RECEIVED IS_CONNECTED")
            findViewById<TextView>(R.id.noBtConnectionView)?.apply{
                visibility = View.GONE
            }
        } else {
            println("SET CONNECTION BANNER RECEIVED IS_NOT_CONNECTED")
            findViewById<TextView>(R.id.noBtConnectionView)?.apply{
                visibility = View.VISIBLE
            }
        }
    }

    private fun launchUpdateStatistics() {
        GlobalScope.launch {
            while (viewModel.getFragmentTitle().value == R.id.overviewFragment){
                rasPi.sendCommand(RaspiCodes.CMD_SEND_DATA, RaspiCodes.DATA_POWER_MEASUREMENTS)
                rasPi.sendCommand(RaspiCodes.CMD_SEND_DATA, RaspiCodes.DATA_TEMPERATURE_MEASUREMENTS)
                delay(1500)
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
        }
    }

}
