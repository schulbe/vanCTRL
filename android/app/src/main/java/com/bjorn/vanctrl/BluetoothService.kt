package com.bjorn.vanctrl

import android.app.Activity
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

    fun sendCommand(cmd: RaspiCodes) {
        write(messageProcessor.createCommandMessage(cmd))
    }

    fun tryConnection(deviceMac: String, deviceDisplayName: String, timeout: Int = 10) {
        try {
            println("INITIALIZE")
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
            println("TRYING TO WRITE $bytes")
            mmOutStream.write(bytes)
        }

    }
}

class BluetoothException(message: String) : Exception(message)



class MessageProcessor(private val viewModel: VanViewModel) {
    fun createCommandMessage(cmd:RaspiCodes): String {
        if cmd.name.startsWitch("SWITCH_") {
            var split = cmd.name.split('_')
            var switch = split.slice((1..s.size-2)).joinToString(separator="_")
            var action = split[split.size-1]
            return "\u0002${action}\u0003${switch}\u0002"
        }


        return "\u0002${cmd.code}\u0002"
    }

    fun processReceivedMessage(msg: String) {

        if (!(msg.startsWith("\u0002") and msg.endsWith("\u0002"))) {
//            Toast.makeText(activity, "Received message without propper start or end byte", Toast.LENGTH_LONG).show()
        }

        val message = msg.removeSurrounding("\u0002")

        if (message.startsWith(RaspiCodes.PFX_STATISTICS.code.toString())) {
            processReceivedStatistics(message.removePrefix(RaspiCodes.PFX_STATISTICS.code.toString()))
        }
        else if (message.startsWith(RaspiCodes.PFX_SWITCH_STATUS.code.toString())) {
            processReceivedSwitchStatus(message.removePrefix(RaspiCodes.PFX_SWITCH_STATUS.code.toString()))
        }
    }

    private fun processReceivedSwitchStatus(message: String) {
        val statistics = mutableMapOf<RaspiCodes, Boolean>()
        message.split("|").forEach{
                val s = it.split(":")
                if (s.size == 2) {
                    statistics[RaspiCodes.fromCode(s[0].toInt())] = (s[1] == "1")
                }
            }

        viewModel.setSwitchStatus(statistics.toMap())
    }

    private fun processReceivedStatistics(message: String) {

        val statistics = mutableMapOf<RaspiCodes, Float>()
        message.split("|").forEach{
                val s = it.split(":")
                if (s.size == 2) {
                    statistics[RaspiCodes.fromCode(s[0].toInt())] = s[1].toFloat()
            }
        }

        viewModel.setStatistics(statistics.toMap())

    }
}



