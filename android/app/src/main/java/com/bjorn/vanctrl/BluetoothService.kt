package com.bjorn.vanctrl

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


private const val TAG = "MY_APP_DEBUG_TAG"



class BluetoothService(
    private val CONFIG_UUID: UUID,
    private val activity: MainActivity,
    private val messageProcessor: MessageProcessor
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var mmSocket: BluetoothSocket? = null
    private val isConnected = MutableLiveData<Boolean>()

    private lateinit var piBtDevice: BluetoothDevice

    private val REQUEST_ENABLE_BT = 17

    fun sendCommand(cmd: RaspiCodes, details:RaspiCodes) {
        write(messageProcessor.createCommandMessage(cmd, details))
    }

    fun tryConnection(deviceMac: String, deviceDisplayName: String, timeout: Int = 10) {
        try {
            initiateBluetoothConnection(deviceMac, deviceDisplayName)
        } catch (e: Exception) {
            val txt = "Error in inital Connection Process: $e"
            println(txt)
            Toast.makeText(activity, txt, Toast.LENGTH_LONG).show()
        }

        try {
            println("OPENCONNECTION")
            openConnection()
        } catch (e: Exception) {
            val txt = "Error in openConnection(): $e"
            println(txt)
            Toast.makeText(activity, txt, Toast.LENGTH_LONG).show()
        }

        // TODO REINCLUDE!
        setIsConnected()

        try {
            openReader()
        } catch (e: Exception) {
            val txt = "Error in openReader(): $e"
            println(txt)
            Toast.makeText(activity, txt, Toast.LENGTH_LONG).show()
        }
    }

    private fun initiateBluetoothConnection(deviceMac: String, deviceDisplayName: String) {
        if (bluetoothAdapter == null) {
//            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth", Toast.LENGTH_SHORT).show();
            throw BluetoothException("No Bluetooth Adapter Found in Device")
            // Device doesn't support Bluetooth
        }
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(activity, enableBtIntent, REQUEST_ENABLE_BT, null)
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address

            if (deviceName == deviceDisplayName && deviceHardwareAddress == deviceMac) {
                piBtDevice = device
            }
        }

        if (mmSocket == null) {
            mmSocket = piBtDevice.createRfcommSocketToServiceRecord(CONFIG_UUID)
            println("MMSOCKET:")
            println(mmSocket)
        }

    }

    private fun openConnection() {
        try {
            mmSocket?.connect()
        } catch (e: Exception) {
            if (isConnected().value == true) {
                println("openConnection() was invoked although connection is already open")
            } else {
                throw e
            }
        }
    }

    private fun openReader() {
        GlobalScope.launch{
            try {
                ConnectedThread().read()
            } catch (e: Exception) {
                val txt = "Error in Reading Process: $e"
                println(txt)
//                Toast.makeText(activity, txt, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun write(message: String) {
        GlobalScope.launch {
            try {
                ConnectedThread().write(message.toByteArray())
            } catch (e: Exception) {
                val txt = "Error in Writing Process (msg= $message): $e"
                println(txt)
//                Toast.makeText(activity, txt, Toast.LENGTH_LONG).show()
            }}
    }

    private fun setIsConnected() {
        isConnected.postValue( mmSocket?.isConnected?: false)

    }

    // TODO: REMOVE
//    fun setIsConnectedTest(isconnected: Boolean) {
//        isConnected.postValue( isconnected)
//    }

    fun isConnected(): LiveData<Boolean> {
        return isConnected
    }

    private fun closeConnection() {
        try {
            mmSocket?.close()
        } catch (e: IOException) {
//            Toast.makeText(activity, "IOException in closeConnection()", Toast.LENGTH_LONG).show()
        }
    }

    private inner class ConnectedThread {

        private val mmInStream: InputStream? = mmSocket?.inputStream
        private val mmOutStream: OutputStream? = mmSocket?.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        fun read() {
            if (mmInStream == null) {
//                Toast.makeText(activity, "Could not write data because no output stream is available", Toast.LENGTH_LONG).show()
                return
            }

            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
//                    Toast.makeText(activity, "Inputstream was diconnected", Toast.LENGTH_LONG).show()
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }
                if (numBytes == 0) {
//                    Toast.makeText(activity, "Received Message of zero length", Toast.LENGTH_LONG).show()
                    continue
                }
                // Send the obtained bytes to the UI activity.
                val receivedString = String(mmBuffer.sliceArray(0 until numBytes))
                messageProcessor.processReceivedMessage(receivedString)

            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            if (mmOutStream == null) {
                throw BluetoothException("mmOutStream is not available")
            }
            mmOutStream.write(bytes)
        }

    }
}

class BluetoothException(message: String) : Exception(message)



class MessageProcessor(private val viewModel: VanViewModel) {
    fun createCommandMessage(cmd:RaspiCodes, details: RaspiCodes): String {
        return "\u0002${RaspiCodes.COMMAND_FLAG.code}\u0003${cmd.code}\u0003${details.code}\u0002"
    }

    fun processReceivedMessage(msg: String) {

        if (!(msg.startsWith("\u0002") and msg.endsWith("\u0002"))) {
//            Toast.makeText(activity, "Received message without propper start or end byte", Toast.LENGTH_LONG).show()
        }

        val message = msg.removeSurrounding("\u0002")
        val (flag, type, details) = message.split("\u0003")

        when (RaspiCodes.fromCode(flag)) {
            RaspiCodes.COMMAND_FLAG -> {
                processCommandMessage(RaspiCodes.fromCode(type), details)

            }
            RaspiCodes.DATA_FLAG -> {
                processDataMessage(RaspiCodes.fromCode(type), details)
            }
            else -> {println("Unknown Sequence as Flag received: $flag")}
        }
    }

    private fun processCommandMessage(type: RaspiCodes, details:String) {

    }

    private fun processDataMessage(type:RaspiCodes, details:String) {
        val detailsSplit = details.split("\u0004")
        when (type) {
            RaspiCodes.DATA_POWER_MEASUREMENTS -> {
                processPowerMeasurements(detailsSplit)
            }
            RaspiCodes.DATA_TEMPERATURE_MEASUREMENTS -> {
                processTemperatureMeasurements(detailsSplit)
            }
            RaspiCodes.DATA_SWITCH_STATUS -> {
                processSwitchStatus(detailsSplit)
            }
            else -> {println("Unknown Sequence as Data Type received: $type")}
        }
    }

//    private fun processReceivedSwitchStatus(message: String) {
//        val statistics = mutableMapOf<RaspiCodes, Boolean>()
//        message.split("|").forEach{
//                val s = it.split(":")
//                if (s.size == 2) {
//                    statistics[RaspiCodes.fromCode(s[0])] = (s[1] == "1")
//                }
//            }
//
//        viewModel.setSwitchStatus(statistics.toMap())
//    }

    private fun processSwitchStatus(details: List<String>) {
        val status = mutableMapOf<Settings, Boolean>()

        status[Settings.fromCode("S1")] = (details[0]=="1")
        status[Settings.fromCode("S2")] = (details[1]=="1")
        status[Settings.fromCode("S3")] = (details[2]=="1")
        status[Settings.fromCode("S4")] = (details[3]=="1")
        status[Settings.fromCode("S5")] = (details[4]=="1")
        status[Settings.fromCode("S6")] = (details[5]=="1")
        status[Settings.fromCode("S7")] = (details[6]=="1")
        status[Settings.fromCode("S8")] = (details[7]=="1")

        viewModel.setSwitchStatus(status.toMap())
    }

    private fun processPowerMeasurements(details: List<String>) {

        val statistics = mutableMapOf<Settings, Map<String, Float>>()
        val meas = mutableMapOf<String, Float>()

        meas["A"] = details[0].toFloat()
        meas["V"] = details[1].toFloat()

        statistics[Settings.fromCode("I1")] = meas.toMap()

        meas["A"] = details[2].toFloat()
        meas["V"] = details[3].toFloat()
        statistics[Settings.fromCode("I2")] = meas.toMap()

        meas["A"] = details[4].toFloat()
        meas["V"] = details[5].toFloat()
        statistics[Settings.fromCode("I3")] = meas.toMap()

        viewModel.setPowerStats(statistics.toMap())
    }

    private fun processTemperatureMeasurements(details: List<String>) {
        println("Process Temperature: $details")
        val temperatures = mutableMapOf<Settings, Float>()

        temperatures[Settings.fromCode("I4")] = details[0].toFloat()
        temperatures[Settings.fromCode("I5")] = details[1].toFloat()

        viewModel.setTemperatures(temperatures.toMap())
    }

//    private fun processReceivedStatistics(message: String) {
//
//        val statistics = mutableMapOf<RaspiCodes, Float>()
//        message.split("|").forEach{
//                val s = it.split(":")
//                if (s.size == 2) {
//                    statistics[RaspiCodes.fromCode(s[0].toInt())] = s[1].toFloat()
//            }
//        }
//
//        viewModel.setPowerStats(statistics.toMap())
//
//    }
}



