/**
 * File name: ServerConnectionThread
 * Sets connection as a bluetooth server
 */
package com.mimmarcelo.btconn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

class ServerConnectionThread extends ConnectionThread {

    /* ** Constructors ** */

    protected ServerConnectionThread(UUID uuid, BluetoothListener bluetoothListener) {
        super(uuid, bluetoothListener);
    }

    /* ** Protected methods ** */

    /**
     * Establishes a connection as a Bluuetooth server
     *
     * @return BluetoothSocket with connection established
     */
    @Override
    protected BluetoothSocket connect() {
        try {
            BluetoothServerSocket serverSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(APP, uuid);
            BluetoothSocket bluetoothSocket = serverSocket.accept();
            serverSocket.close();

            return bluetoothSocket;
        } // end try clause
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    } // end connect method
} // end ClientConnectionThread class