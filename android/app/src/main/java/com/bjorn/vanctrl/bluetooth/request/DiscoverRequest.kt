package com.bjorn.vanctrl.bluetooth.request

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.bjorn.vanctrl.bluetooth.IBluetoothEventListener
import java.util.*

class DiscoverRequest(private val context : Context, private val eventListener: IBluetoothEventListener) : IBluetoothRequest  {

    private var discoveredDevices:MutableList<BluetoothDevice> = mutableListOf()
    private var bluetoothAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    init {
        registerReceiver()
    }

    fun discover() {
        discoveredDevices = mutableListOf()

        if (bluetoothAdapter.isDiscovering)
            bluetoothAdapter.cancelDiscovery()

        bluetoothAdapter.startDiscovery()
        eventListener.onDiscovering()
    }

    private fun registerReceiver() {
        context.registerReceiver(discoverReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        context.registerReceiver(discoverReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    private fun addDiscoveredDevice(device: BluetoothDevice) {
        if (device.bondState != BluetoothDevice.BOND_BONDED)
            return

        for (device in discoveredDevices) {
            if (device.address.equals(device.address))
                return
        }

        discoveredDevices.add(device)
    }

    private val discoverReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (BluetoothDevice.ACTION_FOUND.equals(intent.action)) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                addDiscoveredDevice(device)
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.action)) {
                eventListener.onDiscovered()
            }
        }
    }

    override fun cleanup() {
        context.unregisterReceiver(discoverReceiver)
    }
}