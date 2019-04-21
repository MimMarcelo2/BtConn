/**
 * File name: ClientConnectionThread
 * Sets connection as a bluetooth client
 */
package com.mimmarcelo.btconn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

final class ClientConnectionThread extends ConnectionThread {

    /* ** Private attributes ** */

    private String macAddress;

    /* ** Constructors ** */

    /**
     * Creates a connection as a Bluetooth client and register the observer
     *
     * @param bluetoothListener
     * @param macAddress
     */
    protected ClientConnectionThread(BluetoothListener bluetoothListener, String macAddress) {
        super(bluetoothListener);
        this.macAddress = macAddress;
    }

    /* ** Protected methods ** */

    /**
     * Establishes a connection as a Bluetooth client
     *
     * @return BluetoothSocket with connection established
     */
    @Override
    protected BluetoothSocket connect() {
        try {
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress);
            BluetoothSocket bluetoothSocket = device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));

            bluetoothSocket.connect();
            return bluetoothSocket;
        } // end try clause
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    } // end connect method
} // end ClientConnectionThread class
