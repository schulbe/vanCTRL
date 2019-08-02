package com.bjorn.vanctrl.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.bjorn.vanctrl.bluetooth.request.*

class BluetoothConnectionService(val context: Context) {

    private var eventListener : IBluetoothEventListener = EmptyBluetoothEventListener()
    private val enableRequest = EnableRequest(context, eventListener)
    private val discoverRequest = DiscoverRequest(context, eventListener)
    private val pairRequest = PairRequest(context, eventListener)
    private val connectionRequest = ConnectionRequest(context, eventListener)
    private val audioConnectionRequest = AudioConnectionRequest(context, eventListener)

    fun setBluetoothEventListener(listener: IBluetoothEventListener) {
        eventListener = listener
    }

    fun enableBluetoothAdapter() {
        enableRequest.enableluetooth()
    }

    fun disableBluetoothAdapter() {
        enableRequest.disableBluetooth()
    }

    fun discoverDevices() {
        discoverRequest.discover()
    }

    fun pairDevice(device : BluetoothDevice) {
        pairRequest.pair(device)
    }

    fun connectDevice(device: BluetoothDevice) {
        connectionRequest.conntect(device)
    }

    fun stopConnectDevice() {
        connectionRequest.stopConnect()
    }

    fun connectAudioDevice(device : BluetoothDevice) {
        audioConnectionRequest.connect(device)
    }

    fun cleanUp() {
        enableRequest.cleanup()
        discoverRequest.cleanup()
        pairRequest.cleanup()
    }
}