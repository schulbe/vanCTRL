package com.bjorn.vanctrl.bluetooth.request

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.bjorn.vanctrl.bluetooth.IBluetoothEventListener
import java.io.IOException
import java.util.*

private const val BASE_UUID = "00000000-0000-1000-8000-00805F9B34FB"

class ConnectionRequest(private val context : Context, private val eventListener: IBluetoothEventListener) : IBluetoothRequest {
    private var connectionThread : ConnectionThread? = null

    fun conntect(device: BluetoothDevice) {
        eventListener.onConnecting()
        connectionThread = ConnectionThread(device)
        { isSuccess -> eventListener.onConnected(isSuccess)}
        connectionThread?.start()
    }

    fun stopConnect() {
        if (connectionThread != null)
            connectionThread?.cancel()
    }

    override fun cleanup() {
        stopConnect()
    }


    private class ConnectionThread(private val device : BluetoothDevice,
                                   private val onComplete: (isSuccess : Boolean) -> Unit) : Thread() {

        private var bluetoothAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        private var bluetoothSocket : BluetoothSocket? = createSocket();

        private fun createSocket() : BluetoothSocket? {
            var socket : BluetoothSocket? = null;

            try {
                val uuid = if (device.uuids.size > 0)
                    device.uuids[0].uuid
                else UUID.fromString(BASE_UUID);

                socket = device.createRfcommSocketToServiceRecord(uuid)
            }
            catch (e : IOException) {}

            return socket;
        }

        override fun run() {
            super.run()

            bluetoothAdapter.cancelDiscovery()
            var isSuccess = false

            try {
                if (bluetoothSocket != null) {
                    bluetoothSocket?.connect()
                    isSuccess = true
                }

            }
            catch (e: Exception) { }

            onComplete(isSuccess)
        }

        fun cancel() {
            if (bluetoothSocket != null)
                bluetoothSocket?.close()
        }
    }


}