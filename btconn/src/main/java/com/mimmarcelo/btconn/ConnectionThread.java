/**
 * File name: ConnectionBluetooth
 * Defines the procedures to establish a bluetooth connection
 *
 * *******************************
 * It is necessary implement Parcelable instead Serializable
 */
package com.mimmarcelo.btconn;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

abstract class ConnectionThread extends Thread implements Serializable {

    /* ** Protected final attributes ** */

    //General data to the application
    protected final String APP = "BtCpnn";
    protected final String UUID = "00001101-0000-1000-8000-00805F9B34FB";

    /* ** Private attributes ** */

    private BluetoothListener bluetoothListener; // Observer pattern
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice device;
    private InputStream input;
    private OutputStream output;
    private boolean running;

    /* ** Constructors ** */

    /**
     * Initialize the class attributes
     *
     * @param bluetoothListener observer pattern
     */
    protected ConnectionThread(BluetoothListener bluetoothListener) {
        this.bluetoothListener = bluetoothListener;
        this.bluetoothSocket = null;
        this.device = null;
        this.input = null;
        this.output = null;
        this.running = false;
    } // end constructor ConnectionThread

    /* ** Public methods ** */

    /**
     * Process parallel to UI to prevent the UI process freeze
     * Ask for the right connection type
     */
    @Override
    public void run() {
        bluetoothSocket = connect();
        if (bluetoothSocket != null) {
            running = true;
            device = bluetoothSocket.getRemoteDevice();
            Log.i("ConnectionThread", "device setted: " + device.getName());

            Intent intent = new Intent();
            intent.putExtra(BluetoothListener.EXTRA_STATUS, BluetoothListener.STATUS_DEVICE_CONNECTED);
            bluetoothListener.messageReceived(intent);//Registers "Connected" message

            try {
                input = bluetoothSocket.getInputStream();
                output = bluetoothSocket.getOutputStream();
                connectionLoop(); // Receive all bluetooth messages
                cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } // end if bluetoothSocket != null
    } // end run method

    /* ** Protected abstract methods ** */

    /**
     * Method to be override with the right way to connect
     *
     * @return BluetoothSocket with connection established
     */
    protected abstract BluetoothSocket connect();

    /* ** Protected methods ** */

    /**
     * Verifies and returns, if exist, the connected device
     *
     * @return connected device
     */
    protected BluetoothDevice getDevice() {
        Log.i("ConnectionThread", "device required");
        if (running) {
            if (device == null) {
                device = bluetoothSocket.getRemoteDevice();
            }
            return device;
        } else {
            return null;
        }
    } // end getDevice method

    /**
     * Prepares and sends the message by Bluetooth
     *
     * @param intent data with the message
     */
    protected void sendMessage(Intent intent) {
        if (intent.hasExtra(BluetoothListener.EXTRA_MESSAGE)) {
            String message = intent.getStringExtra(BluetoothListener.EXTRA_MESSAGE);

            /*
             * "\n" is important for adapter send the message
             * without it the message is added to the older one
             * and it is send only the thread is finished
             */
            if (message.endsWith("\n")) message += "\n";

            byte[] buffer = message.getBytes();

            if (output != null) {
                try {
                    output.write(buffer);
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } // end if output != null
        } // end if has EXTRA_MESSAGE
    } // end sendMessage method

    /**
     * Fineshes all class objects
     *
     * @throws IOException when some object is closed unexpectedly
     */
    protected void cancel() throws IOException {
        if (input != null) {
            input.close();
            input = null;
        }

        if (output != null) {
            output.close();
            output = null;
        }

        if (bluetoothSocket != null) {
            bluetoothSocket.close();
            bluetoothSocket = null;
        }

        running = false;
    } // end cancel method

    /* ** Private methods ** */

    /**
     * Converts and cleans the messages received by Bluetooth
     *
     * @param input Bytes received
     * @return the message as a String
     */
    private String sanitizeString(byte[] input) {
        String m = new String(input);
        if (m.endsWith("\n")) m = m.substring(0, m.length() - 1);
        return m;
    }

    /**
     * Loop to read the Bluetooth input (messages received)
     *
     * @throws IOException when the input is closed unexpectedly
     */
    private void connectionLoop() throws IOException {
        Intent intent = new Intent();
        byte[] buffer = new byte[1024];
        int bytes;

        //Reads and registers all messages received
        while (running) {
            bytes = input.read(buffer);
            intent.putExtra(BluetoothListener.EXTRA_MESSAGE, sanitizeString(Arrays.copyOfRange(buffer, 0, bytes)));
            bluetoothListener.messageReceived(intent);
        }
    } // end connectionLoop method
}
