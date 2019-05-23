/**
 * File name: ConnectionBluetooth
 * Defines the procedures to establish a bluetooth connection
 *
 * *******************************
 * It is necessary implement Parcelable instead Serializable
 */
package com.mimmarcelo.btconn;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class ConnectedThread extends Thread implements BluetoothItem, Serializable {

    /* ** Private attributes ** */

    private BluetoothListener bluetoothListener; // Observer pattern
    private BluetoothSocket bluetoothSocket;
    private InputStream input;
    private OutputStream output;
    private boolean running;

    /* ** Constructors ** */

    /**
     * Initialize the class attributes
     *
     * @param bluetoothListener observer pattern
     */
    protected ConnectedThread(BluetoothListener bluetoothListener, BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
        this.bluetoothListener = bluetoothListener;
        try {
            this.input = bluetoothSocket.getInputStream();
            this.output = bluetoothSocket.getOutputStream();
            Log.i("ConnectedThread", "Connection created");
        } catch (IOException e) {
            Log.i("ConnectedThread", e.getMessage());
        }
        this.running = false;
    } // end constructor ConnectedThread

    /* ** Public methods ** */

    public boolean isRunning() {
        return running;
    }

    /**
     * Verifies and returns, if exist, the connected device
     *
     * @return connected device
     */
    public BluetoothDevice getDevice() {
        Log.i("ConnectedThread", "device required");
        return bluetoothSocket.getRemoteDevice();
    } // end getDevice method

    @Override
    public String getText() {
        return bluetoothSocket.getRemoteDevice().getName();
    }

    @Override
    public Object getInstance() {
        return this;
    }

    /**
     * Process parallel to UI to prevent the UI process freezes
     * Ask for the right connection type
     */
    @Override
    public void run() {
        if (bluetoothSocket != null) {
            running = true;
            try {
                connectionLoop(); // Receive all bluetooth messages
                cancel();
            } catch (IOException e) {
                //Nothing to do
            } catch (Exception e){
                e.printStackTrace();
            }
            finally {
                running = false;
            }
        } // end if bluetoothSocket != null
    } // end run method

    /* ** Protected methods ** */

    /**
     * Prepares and sends the message by Bluetooth
     *
     * @param intent data with the message
     */
    private void sendMessage(Intent intent) {
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

    public void sendMessage(String message){
        Intent intent = new Intent();
        intent.putExtra(BluetoothListener.EXTRA_MESSAGE, message);
        sendMessage(intent);
    }

    /**
     * Fineshes all class objects
     *
     * @throws IOException when some object is closed unexpectedly
     */
    public void cancel() throws IOException {
        running = false;
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
            intent.putExtra(BluetoothListener.EXTRA_CONNECTION, this);
            bluetoothListener.onActivityResult(BluetoothListener.MESSAGE_RECEIVED, Activity.RESULT_OK, intent);
        }
    } // end connectionLoop method
}
