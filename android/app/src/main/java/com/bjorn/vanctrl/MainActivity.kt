package com.bjorn.vanctrl

import android.os.Bundle
import android.view.View
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
    val REQUEST_ENABLE_BT: Int = 17

    val PI_MAC_ADDR: String = "B8:27:EB:C8:56:C7"
    val PI_BT_NAME: String = "raspberrypi"
    val PI_UUID: UUID = UUID.fromString("1e0ca4ea-299d-4335-93eb-27fcfe7fa848")

//    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
//    private lateinit var piBtDevice: BluetoothDevice
    private lateinit var btService: BluetoothService

    private lateinit var navController: NavController
    private lateinit var viewModel: VanViewModel
    private lateinit var rasPi: RasPi

    private var mIsInForeground: Boolean = true

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_overview -> {
                viewModel.setFragmentTitle("overviewFragment")
                launchPowerMeasurementUpdateRoutine()
                navController.navigate(R.id.overviewFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_switches -> {
                viewModel.setFragmentTitle("switchesFragment")
                navController.navigate(R.id.switchesFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                viewModel.setFragmentTitle("fridgeFragment")
                navController.navigate(R.id.settingsFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_radio -> {
                viewModel.setFragmentTitle("radioFragment")
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
        viewModel.getPowerMeasurements().observe(this, Observer<Map<String,Float>>{ measurements -> setPowerMeasurementsToUI(measurements) })

        btService = BluetoothService(PI_UUID, this, RaspiMessageProcessor(viewModel))
        btService.isConnected().observe(this, Observer<Boolean>{isConnected -> setConnectionBanner(isConnected)})

        rasPi = RasPi(btService)

//        viewModel.setFragmentTitle("batteryFragment")
        launchPowerMeasurementUpdateRoutine()

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
        launchPowerMeasurementUpdateRoutine()
    }

//    override fun onStart() {
//        super.onStart()
//        try {
//            btService.openConnection()
//            btService.openReader()
//        } catch (e: BluetoothException){ Toast.makeText(this, "Socket openening and reading failed", Toast.LENGTH_LONG).show()}
//    }
//
//    override fun onStop() {
//        super.onStop()
//        rasPi.sendCommand(RaspiCommands.CLOSE_CONNECTION)
//        btService.closeConnection()
//    }

    override fun switch(what: String, on:Boolean) {
        when(what) {
            "FRONT_LIGHT" -> when(on) {
                true  -> rasPi.sendCommand(RaspiCommands.SWITCH_FRONT_LIGHT_ON)
                false -> rasPi.sendCommand(RaspiCommands.SWITCH_FRONT_LIGHT_OFF)
            }
            "BACK_LIGHT" -> when(on) {
                true  -> rasPi.sendCommand(RaspiCommands.SWITCH_BACK_LIGHT_ON)
                false -> rasPi.sendCommand(RaspiCommands.SWITCH_BACK_LIGHT_OFF)
            }
        }
    }

    override fun connectBluetoothDevice() {
        btService.initiateBluetoothConnection(PI_MAC_ADDR, PI_BT_NAME)
        btService.openConnection()
        btService.openReader()
    }

    private fun setPowerMeasurementsToUI(measurements: Map<String, Float>) {
        val formattedVoltage = "%.2f V".format(measurements["vBat"])
        findViewById<TextView>(R.id.batteryVoltageView)?.apply {
            text = formattedVoltage
        }
    }

    private fun setConnectionBanner(isConnected: Boolean) {
        if (isConnected) {
            findViewById<TextView>(R.id.noBtConnectionView)?.apply{
                visibility = View.INVISIBLE
            }
        } else {
            findViewById<TextView>(R.id.noBtConnectionView)?.apply{
                visibility = View.VISIBLE
            }
        }
    }

    private fun launchPowerMeasurementUpdateRoutine() {
//        GlobalScope.launch {
//            delay(1000)
//            while (viewModel.getFragmentTitle().value == "overviewFragment" && mIsInForeground) {
//                val measurements = rasPi.getPowerMeasurements()
//                viewModel.setPowerStatistics(measurements)
//                delay(1000)
//            }
//            btService.o
//        }
    }

}
