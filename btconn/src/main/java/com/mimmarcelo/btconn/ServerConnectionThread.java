/**
 * File name: ServerConnectionThread
 * Sets connection as a bluetooth server
 */
package com.mimmarcelo.btconn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

class ServerConnectionThread extends ConnectionThread {
    protected ServerConnectionThread(BluetoothListener bluetoothListener) {
        super(bluetoothListener);
    }

    /**
     * Establish a connection as a Bluuetooth server
     * @return BluetoothSocket with connection established
     */
    @Override
    protected BluetoothSocket connect() {
        try {
            BluetoothServerSocket serverSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(APP, java.util.UUID.fromString(UUID));
            BluetoothSocket bluetoothSocket = serverSocket.accept();
            serverSocket.close();

            return bluetoothSocket;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
