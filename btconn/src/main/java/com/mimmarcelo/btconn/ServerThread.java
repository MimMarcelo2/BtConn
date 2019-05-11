/**
 * File name: ServerThread
 * Sets connection as a bluetooth server
 */
package com.mimmarcelo.btconn;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.io.IOException;
import java.util.UUID;

class ServerThread extends Thread {

    /* ** Constants ** */

    private final String APP = "btconn";

    /* ** Protected final attributes ** */
    private BluetoothListener bluetoothListener;
    private UUID uuid;
    /* ** Constructors ** */

    public ServerThread(UUID uuid, BluetoothListener bluetoothListener) {
        this.uuid = uuid;
        this.bluetoothListener = bluetoothListener;
    }

    /* ** Public methods ** */

    @Override
    public void run() {
        Intent intent = new Intent();
        try {
            BluetoothServerSocket serverSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(APP, uuid);
            BluetoothSocket bluetoothSocket = serverSocket.accept();
            serverSocket.close();

            ConnectionThread conn = new ConnectionThread(bluetoothListener, bluetoothSocket);
            intent.putExtra(BluetoothListener.EXTRA_CONNECTION, conn);
            bluetoothListener.onActivityResult(BluetoothListener.DEVICE_CONNECTED, Activity.RESULT_OK, intent);
        } // end try clause
        catch (IOException e) {
            intent.putExtra(BluetoothListener.EXTRA_MESSAGE, e.getMessage());
            bluetoothListener.onActivityResult(BluetoothListener.DEVICE_CONNECTED, Activity.RESULT_CANCELED, intent);
        }
    }
} // end ClientThread class