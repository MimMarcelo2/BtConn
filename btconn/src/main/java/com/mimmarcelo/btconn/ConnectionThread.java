/**
 * File name: ConnectionThread
 * Sets connection as a bluetooth server or client
 */
package com.mimmarcelo.btconn;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.io.IOException;
import java.util.UUID;

final class ConnectionThread extends Thread {

    /* ** Constants ** */

    private static final String APP = "btconn";

    /* ** Private attributes ** */

    private BluetoothListener bluetoothListener;
    private UUID uuid;
    private String macAddress;

    /* ** Constructors ** */

    /**
     * Creates a connection as a Bluetooth client and register the observer
     *
     * @param uuid
     * @param bluetoothListener
     * @param macAddress
     */
    public ConnectionThread(UUID uuid, BluetoothListener bluetoothListener, String macAddress) {
        this.uuid = uuid;
        this.macAddress = macAddress;
        this.bluetoothListener = bluetoothListener;
        BluetoothSocket bluetoothSocket = connect(uuid, macAddress);
        Intent intent = new Intent();
        int resultCode = Activity.RESULT_CANCELED;
        if(bluetoothSocket != null) {
            ConnectedThread conn = new ConnectedThread(bluetoothListener, bluetoothSocket);
            intent.putExtra(BluetoothListener.EXTRA_CONNECTION, conn);
            resultCode = Activity.RESULT_OK;
        }
        bluetoothListener.onActivityResult(BluetoothListener.DEVICE_CONNECTED, resultCode, intent);
    }

    /**
     * Creates a connection as a Bluetooth server and register the observer
     *
     * @param uuid
     * @param bluetoothListener
     */
    public ConnectionThread(UUID uuid, BluetoothListener bluetoothListener){
        this(uuid, bluetoothListener, APP);
    }
    /* ** Public methods ** */

    @Override
    public void run() {
        Intent intent = new Intent();
        BluetoothSocket bluetoothSocket;
        try {
            // Connect as a server
            if(macAddress.equals(APP)){
                BluetoothServerSocket serverSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(APP, uuid);
                bluetoothSocket = serverSocket.accept();
                serverSocket.close();

            }
            else { // Connect as a client
                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress);
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
            }

            ConnectedThread conn = new ConnectedThread(bluetoothListener, bluetoothSocket);
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
} // end ConnectionThread class
