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

final class BluetoothBroadcast extends BroadcastReceiver {

    //Singleton pattern
    private static BluetoothBroadcast bluetoothBroadcast;

    //Observer pattern
    private BluetoothListener bluetoothListener;
    private int prevScanMode;

    //Singleton pattern
    private BluetoothBroadcast() {
        this.bluetoothListener = null;
        this.prevScanMode = 0;
    }

    /**
     * Creates or returns a BluetoothBroadcast instance (Singleton pattern), and updates the listener param
     * @param bluetoothListener Observer of this class
     * @return BluetoothBroadcast instance
     */
    public static BluetoothBroadcast getBluetoothBroadcast(BluetoothListener bluetoothListener) {
        if (bluetoothBroadcast == null) {
            bluetoothBroadcast = new BluetoothBroadcast();
        }
        bluetoothBroadcast.bluetoothListener = bluetoothListener;
        return bluetoothBroadcast;
    }

    /**
     * Read changes in the BluetoothAdapter
     * @param context
     * @param intent
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
        else if(intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            prevScanMode = BluetoothListener.STATUS_BLUETOOTH_TURNED_ON;
            status = BluetoothListener.STATUS_DEVICE_CONNECTED;
        }//end if ACTION_ACL_CONNECTED

        intent.putExtra(BluetoothListener.EXTRA_STATUS, status);
        bluetoothListener.messageReceived(intent);//Send data to observer
    }
}
