package com.bjorn.vanctrl.bluetoothBjorn

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


private const val TAG = "MY_APP_DEBUG_TAG"

// Defines several constants used when transmitting messages between the
// service and the UI.
const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2
// ... (Add other message types here as needed.)



class NEWBluetoothService(
    private val CONFIG_UUID: UUID

)

    fun initiateBluetoothConnection(deviceMac: String, deviceName:String) {

    }


class BluetoothService(
    // handler that gets info from Bluetooth service
    private val handler: Handler

) {
    private lateinit var mmSocket: BluetoothSocket

    fun setUp(socket: BluetoothSocket) {
        mmSocket = socket

        println("Set Socket...")
    }

    fun waitForMessages() {
        println("WAITN")
        ConnectedThread(mmSocket).run()
    }

    fun sendMessage(message: String) {
        ConnectedThread(mmSocket).write(message.toByteArray())
    }


    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                println("WEEEEEL")

                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }
                println(String(mmBuffer.sliceArray(0 until numBytes)))
                // Send the obtained bytes to the UI activity.
                val readMsg = handler.obtainMessage(
                    MESSAGE_READ, numBytes, -1,
                    mmBuffer)

                readMsg.sendToTarget()
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
                return
            }

//            // Share the sent message with the UI activity.
//            val writtenMsg = handler.obtainMessage(
//                MESSAGE_WRITE, -1, -1, mmBuffer)
//            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }
}


class BTHandler: Handler() {
    override fun handleMessage(msg: Message) {
        when(msg.what) {
            MESSAGE_READ -> {
                println("RECEIVED THAT SHIT")
                val readBuf = msg.obj
                println(readBuf.toString())
            }
        }
    }
}

