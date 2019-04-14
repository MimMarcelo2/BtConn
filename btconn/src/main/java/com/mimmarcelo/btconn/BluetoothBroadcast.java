/**
 * File name: BluetoothBroadcast
 * It is responsible to read the changes in the BluetoothAdapter
 * Turned on, turned off, discoverable, time out of discoverable, device connected
 */
package com.mimmarcelo.btconn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

final class BluetoothBroadcast extends BroadcastReceiver {

    //Singleton pattern
    private static BluetoothBroadcast bluetoothBroadcast;

    //Observer pattern
    private List<BluetoothListener> bluetoothListeners;
    private int prevScanMode;

    //Singleton pattern
    private BluetoothBroadcast() {
        this.bluetoothListeners = new ArrayList<>();
        this.prevScanMode = 0;
    }

    /**
     * Creates or returns a BluetoothBroadcast instance (Singleton pattern)
     * @return BluetoothBroadcast instance
     */
    public static BluetoothBroadcast getBluetoothBroadcast() {
        if (bluetoothBroadcast == null) {
            bluetoothBroadcast = new BluetoothBroadcast();
        }
        return bluetoothBroadcast;
    }

    /**
     * Observer pattern
     * @param bluetoothListener
     */
    public void registerObserver(BluetoothListener bluetoothListener){
        if(!bluetoothListeners.contains(bluetoothListener)) {
            bluetoothListeners.add(bluetoothListener);
        }
    }

    /**
     * Observer pattern
     * @param bluetoothListener
     */
    public void unregisterObserver(BluetoothListener bluetoothListener){
        if(bluetoothListeners.contains(bluetoothListener)){
            bluetoothListeners.remove(bluetoothListener);
        }
    }

    /**
     * Reads the updates on BluetoothAdapter
     * @param context Context from current activity
     * @param intent Action from BluetoothAdapter
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        int status = BluetoothListener.STATUS_NONE;

        if (intent.getAction().equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
            int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE);

            switch (scanMode) {
                case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                    if (prevScanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        status = BluetoothListener.STATUS_DISCOVERABLE_TURNED_OFF;
                    } else if (prevScanMode != BluetoothListener.STATUS_BLUETOOTH_TURNED_ON){
                        status = BluetoothListener.STATUS_BLUETOOTH_TURNED_ON;
                    }
                    break;
                case BluetoothAdapter.SCAN_MODE_NONE:
                    status = BluetoothListener.STATUS_BLUETOOTH_TURNED_OFF;
                    break;
                case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                    status = BluetoothListener.STATUS_DISCOVERABLE_TURNED_ON;
                    break;
            } //end switch scanMode

            prevScanMode = scanMode;
        } //end if ACTION_SCAN_MODE_CHANGED
        else
            if(intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            prevScanMode = BluetoothListener.STATUS_BLUETOOTH_TURNED_ON;
            status = BluetoothListener.STATUS_NONE;
        }//end if ACTION_ACL_CONNECTED
        else
            if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)){
            status = BluetoothListener.STATUS_DEVICE_FOUND;
        }

        intent.putExtra(BluetoothListener.EXTRA_STATUS, status);

        //Send data to observers
        for (BluetoothListener bluetoothListener: bluetoothListeners) {
            bluetoothListener.messageReceived(intent);
        }
    }

}
