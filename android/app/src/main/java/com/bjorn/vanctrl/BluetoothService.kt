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
import androidx.preference.PreferenceManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


private const val TAG = "MY_APP_DEBUG_TAG"



class BluetoothService(
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

    fun sendData(data_type: RaspiCodes, details:List<String>) {
        write(messageProcessor.createDataMessage(data_type, details))
    }

    fun tryConnection(deviceMac: String?, deviceDisplayName: String?, deviceUUID: UUID) {
        try {
            initiateBluetoothConnection(deviceMac, deviceDisplayName, deviceUUID)
        } catch (e: Exception) {
            val txt = "Error in inital Connection Process: $e"
            println(txt)
            activity.runOnUiThread { Toast.makeText(activity, txt, Toast.LENGTH_LONG).show() }
            return
        }

        try {
            if (bluetoothAdapter?.isEnabled?: false) {
                openConnection() }
        } catch (e: Exception) {
            val txt = "Could not connect to Pi..."
            println(txt)
            activity.runOnUiThread { Toast.makeText(activity, txt, Toast.LENGTH_LONG).show() }
            return
        }

        setIsConnected()

        try {
            openReader()
        } catch (e: Exception) {
            val txt = "Error in openReader(): $e"
            println(txt)
            activity.runOnUiThread { Toast.makeText(activity, txt, Toast.LENGTH_LONG).show()}
            return
        }
    }

    private fun initiateBluetoothConnection(deviceMac: String?, deviceDisplayName: String?, deviceUUID: UUID) {
        if (bluetoothAdapter == null) {
            throw BluetoothException("No Bluetooth Adapter Found in Device")
        }
        else if (deviceMac == null || deviceDisplayName == null) {
            throw BluetoothException("No Device MAC or DisplayName found. Please go to Settings")
        }
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(activity, enableBtIntent, REQUEST_ENABLE_BT, null)
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        pairedDevices?.forEach { device ->

            if (device.name == deviceDisplayName && device.address == deviceMac) {
                piBtDevice = device
            }
        }

        if (mmSocket == null) {
            mmSocket = piBtDevice.createRfcommSocketToServiceRecord(deviceUUID)
            println("MMSOCKET: $mmSocket")
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
            }
        }
    }

    private fun write(message: String) {
        if (isConnected.value == true) {
            GlobalScope.launch {
                try {
                    ConnectedThread().write(message.toByteArray())
                } catch (e: Exception) {
                    val txt = "Error in Writing Process (msg= $message): $e"
                    println(txt)
//                Toast.makeText(activity, txt, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setIsConnected() {
        isConnected.postValue( mmSocket?.isConnected?: false)

    }

    fun isConnected(): LiveData<Boolean> {
        return isConnected
    }

    private fun closeConnection() {
        try {
            mmSocket?.close()
            mmSocket = null
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

    fun createDataMessage(data_type:RaspiCodes, details: List<String>):String {
        return "\u0002${RaspiCodes.DATA_FLAG.code}\u0003${data_type.code}\u0003${details.joinToString("\u0004")}\u0002"
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
        println("Process Data Message: $type")
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
        println("PROCESSING POWER MEAS: $details")
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



