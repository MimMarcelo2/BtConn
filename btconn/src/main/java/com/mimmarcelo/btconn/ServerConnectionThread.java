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

    /* ** Constructors ** */

    protected ServerConnectionThread(BluetoothListener bluetoothListener) {
        super(bluetoothListener);
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
            BluetoothServerSocket serverSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(APP, java.util.UUID.fromString(UUID));
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