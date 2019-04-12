/**
 * File name: ConnectionBluetooth
 * Defines the procedures to establish a bluetooth connection
 */
package com.mimmarcelo.btconn;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

abstract class ConnectionThread extends Thread {

    //General data to the application
    protected final String APP = "BtCpnn";
    protected final String UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private BluetoothListener bluetoothListener;//Observer pattern
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice device;
    private InputStream input;
    private OutputStream output;
    private boolean running;

    protected ConnectionThread(BluetoothListener bluetoothListener){
        this.bluetoothListener = bluetoothListener;
        this.bluetoothSocket = null;
        this.device = null;
        this.input = null;
        this.output = null;
        this.running = false;
    }

    /**
     * Ask for the right connection type
     * Receive all bluetooth messages
     */
    @Override
    public void run() {
        bluetoothSocket = connect();
        if (bluetoothSocket != null){
            running = true;
            device = bluetoothSocket.getRemoteDevice();

            connectionLoop();
            cancel();
        }
    }

    protected BluetoothDevice getDevice(){
        if(running){
            return device;
        }
        else{
            return null;
        }
    }

    /**
     * Loop to read the Bluetooth input
     */
    private void connectionLoop(){
        try {
            input = bluetoothSocket.getInputStream();
            output = bluetoothSocket.getOutputStream();

            Intent intent = new Intent();
            byte[] buffer = new byte[1024];
            int bytes;
            while (running) {
                bytes = input.read(buffer);
                intent.putExtra(BluetoothListener.EXTRA_MESSAGE, sanitizeString(Arrays.copyOfRange(buffer, 0, bytes)));
                bluetoothListener.messageReceived(intent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert and clear the messages received by Bluetooth
     * @param input Bytes received
     * @return
     */
    private String sanitizeString(byte[] input) {
        String m = new String(input);
        if(m.endsWith("\n")) m = m.substring(0, m.length()-1);
        return m;
    }

    /**
     * Prepare and send message by Bluetooth
     * @param intent
     */
    protected void sendMessage(Intent intent){
        if(intent.hasExtra(BluetoothListener.EXTRA_MESSAGE)){
            String message = intent.getStringExtra(BluetoothListener.EXTRA_MESSAGE);

            if (message.endsWith("\n")) message += "\n";

            byte[] buffer = message.getBytes();

            if(output != null){
                try {
                    output.write(buffer);
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Finish all class objects
     */
    private void cancel(){
        if (input != null) {
            try {
                input.close();
            } catch (Exception e) {
            }
            input = null;
        }

        if (output != null) {
            try {
                output.close();
            } catch (Exception e) {
            }
            output = null;
        }

        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (Exception e) {
            }
            bluetoothSocket = null;
        }

        running = false;
    }

    /**
     * Method to be override with the right way to connect
     * @return BluetoothSocket with connection established
     */
    protected abstract BluetoothSocket connect();

}
