package com.bjorn.vanctrl.bluetooth.request

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.bjorn.vanctrl.bluetooth.IBluetoothEventListener
import java.lang.reflect.InvocationTargetException

class AudioConnectionRequest (private val context : Context, private val eventListener: IBluetoothEventListener) : IBluetoothRequest {

    private val bluetoothAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var currentBluetoothDevice : BluetoothDevice? = null;

    init {
        registerReceiver()
    }

    fun connect(device : BluetoothDevice) {
        currentBluetoothDevice = device
        bluetoothAdapter.getProfileProxy(context, profileServiceListener, BluetoothProfile.A2DP)
    }

    private fun registerReceiver() {
        context.registerReceiver(audioBroadcastReceiver, IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED))
    }


    private val profileServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile != BluetoothProfile.A2DP)
                return

            var hasError = false

            if (isAlreadyConnected(proxy!!, currentBluetoothDevice!!)) {
                eventListener.onConnected(true)
            }

            try {
                val connect = BluetoothA2dp::class.java.getDeclaredMethod("connect", BluetoothDevice::class.java)
                connect.invoke(proxy, currentBluetoothDevice)
            } catch (e: IllegalAccessException) {
                e.printStackTrace();
                hasError = true;
            } catch (e : InvocationTargetException) {
                e.printStackTrace();
                hasError = true;
            } catch (e : NoSuchMethodException) {
                e.printStackTrace();
                hasError = true;
            }

            if (hasError)
                eventListener.onConnected(false)
        }

        override fun onServiceDisconnected(p0: Int) {

        }
    }

    private fun isAlreadyConnected(proxy : BluetoothProfile, device: BluetoothDevice) : Boolean {
        return (proxy.getConnectionState(device) == BluetoothA2dp.STATE_CONNECTED)
    }

    private val audioBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (currentBluetoothDevice == null)
                return

            val action = intent?.getAction()
            val state = intent?.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1)

            if (!BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(action))
                return

            if (state == BluetoothA2dp.STATE_CONNECTING)
                eventListener.onConnecting()
            if (state == BluetoothA2dp.STATE_CONNECTED)
                eventListener.onConnected(true)
            if (state == BluetoothA2dp.STATE_DISCONNECTING)
                eventListener.onDisconnecting()
            if (state == BluetoothA2dp.STATE_DISCONNECTED)
                eventListener.onDisconnected()
        }
    }

    override fun cleanup() {
        context.unregisterReceiver(audioBroadcastReceiver)
    }

}