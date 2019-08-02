package com.bjorn.vanctrl

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bjorn.vanctrl.bluetoothBjorn.BTHandler
import com.bjorn.vanctrl.bluetoothBjorn.BluetoothService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {
    val REQUEST_ENABLE_BT: Int = 17

    val PI_MAC_ADDR: String = "B8:27:EB:C8:56:C7"
    val PI_BT_NAME: String = "raspberrypi"
    val PI_UUID: UUID = UUID.fromString("1e0ca4ea-299d-4335-93eb-27fcfe7fa848")

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var piBtDevice: BluetoothDevice
    private lateinit var navController: NavController
    private lateinit var viewModel: VanViewModel
    private lateinit var rasPi: RasPi

    private var mIsInForeground: Boolean = true

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_battery -> {
                navController.navigate(R.id.batteryFragment)
                viewModel.setFragmentTitle("batteryFragment")
                launchPowerMeasurementUpdateRoutine()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_switches -> {
                navController.navigate(R.id.switchesFragment)
                viewModel.setFragmentTitle("switchesFragment")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_fridge -> {
                navController.navigate(R.id.fridgeFragment)
                viewModel.setFragmentTitle("fridgeFragment")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_radio -> {
                navController.navigate(R.id.radioFragment)
                viewModel.setFragmentTitle("radioFragment")
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

        // BLUETOOTH TEST
        setUpBluetooth()
        val btHandler = BTHandler()
        val btService = BluetoothService(btHandler)

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address

            if (deviceName == PI_BT_NAME && deviceHardwareAddress == PI_MAC_ADDR) {
                piBtDevice = device
                println("YEAH IM FUCKING CONNECTED")
            }
        }


        val mmSocket: BluetoothSocket = piBtDevice.createRfcommSocketToServiceRecord(PI_UUID)
        mmSocket.connect()


        btService.setUp(mmSocket)
        Thread.sleep(1000)
//        btService.sendMessage("Hallo hallo?")
        btService.waitForMessages()

        // BLUETOOTH TEST END

        rasPi = RasPi()

        viewModel.setFragmentTitle("batteryFragment")
        launchPowerMeasurementUpdateRoutine()

    }

    override fun onPause() {
        super.onPause()
        mIsInForeground = false
    }

    override fun onResume() {
        super.onResume()
        mIsInForeground = true
        launchPowerMeasurementUpdateRoutine()
    }

    private fun setUpBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth", Toast.LENGTH_SHORT).show();
            println("NO BLUETOOTH ADAPTER FOUND")
            // Device doesn't support Bluetooth
        }
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    private fun connectToDevice() {

    }

    private fun sendMessage(message: String) {
        
    }

    private fun testBT() {

    }

//    private fun getDeviceFromPaired(): String {
//        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
//        pairedDevices?.forEach { device ->
//            val deviceName = device.name
//            val deviceHardwareAddress = device.address // MAC address
//        }
//
//        return "abc"
//    }

    private fun setPowerMeasurementsToUI(measurements: Map<String, Float>) {
        val formattedVoltage = "%.2f V".format(measurements["vBat"])
        findViewById<TextView>(R.id.batteryVoltageView)?.apply {
            text = formattedVoltage
        }
    }

    private fun launchPowerMeasurementUpdateRoutine() {
        GlobalScope.launch {
            delay(1000)
            while (viewModel.getFragmentTitle().value == "batteryFragment" && mIsInForeground) {
                val measurements = rasPi.getPowerMeasurements()
                viewModel.setPowerStatistics(measurements)
                delay(1000)
            }
        }
    }




}
