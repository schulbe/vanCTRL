package com.bjorn.vanctrl.bluetoothBjorn

import android.app.PendingIntent.getActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import com.bjorn.vanctrl.MainActivity
import com.bjorn.vanctrl.VanViewModel
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
    private lateinit var piBtDevice: BluetoothDevice
    private lateinit var mmSocket: BluetoothSocket

    private val REQUEST_ENABLE_BT = 17


    fun initiateBluetoothConnection(deviceMac: String, deviceDisplayName: String) {
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
        mmSocket = piBtDevice.createRfcommSocketToServiceRecord(CONFIG_UUID)

    }

    fun openConnection() {
        try {
            mmSocket.connect()
        } catch (e: IOException) {
            println(e)
            Toast.makeText(activity, "Connection via Socket failed", Toast.LENGTH_LONG).show()
            throw BluetoothException("Could not connect via given socket")
        }
    }

    fun openReader() {
        GlobalScope.launch{ConnectedThread().read()}
    }

    fun write(message: String) {
        Thread{ConnectedThread().write(message.toByteArray())}
    }

    fun closeConnection() {
        try {
            mmSocket.close()
        } catch (e: IOException) {
            println(e)

            throw BluetoothException("Could not close the connect socket")
        }
    }

    private inner class ConnectedThread {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        fun read() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }
                // Send the obtained bytes to the UI activity.
                val receivedString = String(mmBuffer.sliceArray(0 until numBytes))
                messageProcessor.process(receivedString)

            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
                return
            }

        }

    }
}

class BluetoothException(message: String) : Exception(message)

open class MessageProcessor() {
    open fun process(message: String){}
}

class RaspiMessageProcessor(private val viewModel: VanViewModel): MessageProcessor() {
    override fun process(message: String) {
        println(message)

        try {
            val voltage = message.toFloat()
            viewModel.setPowerStatistics(voltage, 1f, 1f)
        } catch (e:  NumberFormatException) {println(e)}
    }
}



