package com.bjorn.vanctrl

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bjorn.vanctrl.Fragments.SettingsFragment
import com.bjorn.vanctrl.Fragments.SwitchesFragment
import kotlinx.coroutines.delay
import java.util.*

class MainActivity : AppCompatActivity(),
    SwitchesFragment.OnSwitchChangedListener,
    SettingsFragment.OnBluetoothButtonClickedListener {

    val PI_MAC_ADDR: String = "B8:27:EB:C8:56:C7"
    val PI_BT_NAME: String = "raspberrypi"
    val PI_UUID: UUID = UUID.fromString("1e0ca4ea-299d-4335-93eb-27fcfe7fa848")

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
            R.id.kitchenlightButton -> {rasPi.sendCommand(RaspiCodes.SWITCH_FRONT_LIGHT_TOGGLE)
                                        viewModel.toggleSwitchStatus(RaspiCodes.FRONT_LIGHT_SWITCH) }
            R.id.bedlightButton -> {rasPi.sendCommand(RaspiCodes.SWITCH_BACK_LIGHT_TOGGLE)
                                    viewModel.toggleSwitchStatus(RaspiCodes.BACK_LIGHT_SWITCH)}
            R.id.fridgeButton -> {rasPi.sendCommand(RaspiCodes.SWITCH_FRIDGE_TOGGLE)
                                  viewModel.toggleSwitchStatus(RaspiCodes.FRIDGE_SWITCH)}
            R.id.radioButton -> {rasPi.sendCommand(RaspiCodes.SWITCH_RADIO_TOGGLE)
                                 viewModel.toggleSwitchStatus(RaspiCodes.RADIO_SWITCH) }
        }

        rasPi.sendCommand(RaspiCodes.SEND_SWITCH_STATUS)

    }


    override fun onConnectBtButtonClick() {
        //TODO why isnt this working

        findViewById<ProgressBar>(R.id.progressBarSettings)?.apply{ visibility = View.VISIBLE }
        Thread.sleep(500)
        rasPi.tryConnection(PI_MAC_ADDR, PI_BT_NAME)
        findViewById<ProgressBar>(R.id.progressBarSettings)?.apply{ visibility = View.GONE }
//        if (rasPi.isConnected().value == true) rasPi.setIsConnectedTest(false) else rasPi.setIsConnectedTest(true)


    }

    private fun setObservers() {
        viewModel.getStatistics().observe(this, Observer<Map<RaspiCodes,Float>>{ measurements -> setPowerMeasurementsToUI(measurements) })
        viewModel.getSwitchStatus().observe(this, Observer<Map<RaspiCodes, Boolean>>{ status -> setButtonImages(status)})
        viewModel.getFragmentTitle().observe(this, Observer<Int> {fragmentId -> onFragmentChange(fragmentId)})

        rasPi.isConnected().observe(this, Observer<Boolean>{isConnected -> setConnectionBanner(isConnected)})

    }

    private fun setButtonImages(status: Map<RaspiCodes, Boolean>) {
        val kitchenlightButton = findViewById<ImageButton>(R.id.kitchenlightButton)
        if (status[RaspiCodes.FRONT_LIGHT_SWITCH] == true){
            kitchenlightButton?.setImageResource(R.drawable.ic_kitchenlight_on)
        } else kitchenlightButton?.setImageResource(R.drawable.ic_kitchenlight_off)

        val bedlightButton = findViewById<ImageButton>(R.id.bedlightButton)
        if (status[RaspiCodes.BACK_LIGHT_SWITCH] == true){
            bedlightButton?.setImageResource(R.drawable.ic_bedlight_on)
        } else bedlightButton?.setImageResource(R.drawable.ic_bedlight_off)

        val fridgeButton = findViewById<ImageButton>(R.id.fridgeButton)
        if (status[RaspiCodes.FRIDGE_SWITCH] == true){
            fridgeButton?.setImageResource(R.drawable.ic_fridge_on)
        } else fridgeButton?.setImageResource(R.drawable.ic_fridge_off)

        val radioButton = findViewById<ImageButton>(R.id.radioButton)
        if (status[RaspiCodes.RADIO_SWITCH] == true){
            radioButton?.setImageResource(R.drawable.ic_radio_on)
        } else radioButton?.setImageResource(R.drawable.ic_radio_off)
    }

    private fun setPowerMeasurementsToUI(measurements: Map<RaspiCodes, Float>) {
        val formattedVoltage = "%.2f V".format(measurements[RaspiCodes.STAT_BATTERY_VOLT])
        findViewById<TextView>(R.id.batteryVoltageView)?.apply {
            text = formattedVoltage
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

    private fun onFragmentChange(fragmentId: Int) {
        when (fragmentId) {
            R.id.overviewFragment -> {
                rasPi.sendCommand(RaspiCodes.SEND_STATISTICS_START)
            }
            R.id.switchesFragment -> {
                rasPi.sendCommand(RaspiCodes.SEND_STATISTICS_STOP)
                rasPi.sendCommand(RaspiCodes.SEND_SWITCH_STATUS)
            }
            R.id.radioFragment -> {
                rasPi.sendCommand(RaspiCodes.SEND_STATISTICS_STOP)
            }
            R.id.settingsFragment -> {
                rasPi.sendCommand(RaspiCodes.SEND_STATISTICS_STOP)
            }
        }
    }

}
