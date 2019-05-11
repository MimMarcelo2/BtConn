/**
 * File name: ClientThread
 * Sets connection as a bluetooth client
 */
package com.mimmarcelo.btconn;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.io.IOException;
import java.util.UUID;

final class ClientThread extends Thread {

    private BluetoothListener bluetoothListener;
    private UUID uuid;
    private String macAddress;

    /* ** Constructors ** */

    /**
     * Creates a connection as a Bluetooth client and register the observer
     *
     * @param bluetoothListener
     * @param macAddress
     */
    public ClientThread(UUID uuid, BluetoothListener bluetoothListener, String macAddress) {
        this.uuid = uuid;
        this.macAddress = macAddress;
        this.bluetoothListener = bluetoothListener;
        BluetoothSocket bluetoothSocket = connect(uuid, macAddress);
        Intent intent = new Intent();
        int resultCode = Activity.RESULT_CANCELED;
        if(bluetoothSocket != null) {
            ConnectionThread conn = new ConnectionThread(bluetoothListener, bluetoothSocket);
            intent.putExtra(BluetoothListener.EXTRA_CONNECTION, conn);
            resultCode = Activity.RESULT_OK;
        }
        bluetoothListener.onActivityResult(BluetoothListener.DEVICE_CONNECTED, resultCode, intent);
    }

    /* ** Public methods ** */

    @Override
    public void run() {
        Intent intent = new Intent();
        try {
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress);
            BluetoothSocket bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);

            bluetoothSocket.connect();

            ConnectionThread conn = new ConnectionThread(bluetoothListener, bluetoothSocket);
            intent.putExtra(BluetoothListener.EXTRA_CONNECTION, conn);
            bluetoothListener.onActivityResult(BluetoothListener.DEVICE_CONNECTED, Activity.RESULT_OK, intent);
        } // end try clause
        catch (IOException e) {
            intent.putExtra(BluetoothListener.EXTRA_MESSAGE, e.getMessage());
            bluetoothListener.onActivityResult(BluetoothListener.DEVICE_CONNECTED, Activity.RESULT_CANCELED, intent);
        }
    }

    /* ** Protected methods ** */

    /**
     * Establishes a connection as a Bluetooth client
     *
     * @return BluetoothSocket with connection established
     */
//    @Override
    protected BluetoothSocket connect(UUID uuid, String macAddress) {

        return null;
    } // end connect method
} // end ClientThread class
