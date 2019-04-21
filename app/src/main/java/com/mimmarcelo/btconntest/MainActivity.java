/**
 * File name: MainActivity
 * it is an example of use of BtConn Library sources
 */
package com.mimmarcelo.btconntest;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.TextView;

import com.mimmarcelo.btconn.BluetoothListener;
import com.mimmarcelo.btconn.BluetoothManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BluetoothListener {

    /* ** Private constants ** */

    private final int TURN_ON = 1;
    private final int TURN_DISCOVERABLE = 2;
    private final int ASK_PERMISSION = 3;

    /* ** Private attributes ** */

    private BluetoothManager bluetoothManager; // Observer Pattern
    private TextView txtStatus;
    private AppCompatEditText edtMessage;

    /* ** Public methods ** */

    /**
     * Defines the behavior of each click button
     *
     * @param v view correspondent to clicked button
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnTurnOn:
                setStatus("Asking for permission...");
                bluetoothManager.turnOn(TURN_ON);
                break;
            case R.id.btnTurnOff:
                bluetoothManager.turnOff();//Message appears on messageReceived method
                break;
            case R.id.btnSendMessage:
                String msg = edtMessage.getText().toString();
                if(!msg.isEmpty()) {
                    bluetoothManager.sendMessage(msg);
                    edtMessage.setText("");
                }
                break; // end btnSendMessage case
            case R.id.btnOpenService:
                setStatus("Asking to open service");
                bluetoothManager.askToOpenService(TURN_DISCOVERABLE, 20);
                break;
            case R.id.btnSearchService:
                setStatus("Searching for service");
                bluetoothManager.searchForOpenService(ASK_PERMISSION);
                break;
            case R.id.btnShowDeviceName:
                setStatus("Device name: " + bluetoothManager.getBluetoothAdapter().getName());
                break;
            case R.id.btnGetConnected:
                String deviceMsg = "";
                for(BluetoothDevice device: bluetoothManager.getConnectedDevices()) {
                    if (device != null) {
                        deviceMsg += device.getName() + ":" + device.getAddress() + " connected\n";
                    } else {
                        deviceMsg += "empty connection\n";
                    }
                }
                if(deviceMsg.equals("")) deviceMsg = "None device connected";
                setStatus(deviceMsg);
                break; // end btnGetConnected case
        } // end switch v.getId
    } // end onClick method

    /**
     * Receivers the answer from required permission
     * And performs how to work on each result
     *
     * @param requestCode Code to identify when asking some permission
     *                    In BtConn library it happens on searchForOpenService method
     * @param permissions List od permissions required
     * @param grantResults list of status to each permission required
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ASK_PERMISSION:
                // Permission denied
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    setStatus("Permission denied");
                } else {
                    //if enabled, it is possible keep with the process
                    setStatus("Permission enabled");
                    bluetoothManager.searchForOpenService(ASK_PERMISSION);
                }
                break; // end ASK_PERMISSION case

        } // end switch requestCode
    } // end onRequestPermissionResult method

    /**
     * Each message from BluetoothManager will be received through this method
     * Each status from BluetoothBroadcast ou BluetoothManager
     * Each message from any Connection
     *
     * @param intent Data received
     */
    @Override
    public void messageReceived(Intent intent) {
        if(intent.hasExtra(BluetoothListener.EXTRA_STATUS)){
            switch (intent.getIntExtra(BluetoothListener.EXTRA_STATUS, 0)){
                case BluetoothListener.STATUS_BLUETOOTH_TURNED_ON:
                    setStatus("Bluetooth on");
                    break;
                case BluetoothListener.STATUS_BLUETOOTH_TURNED_OFF:
                    setStatus("Bluetooth off");
                    break;
                case BluetoothListener.STATUS_DISCOVERABLE_TURNED_ON:
                    setStatus("Waiting some connection");
                    break;
                case BluetoothListener.STATUS_DISCOVERABLE_TURNED_OFF:
                    setStatus("Time out for connection");
                    break;
                case BluetoothListener.STATUS_DEVICE_SELECTED:
                    if(!intent.hasExtra(BluetoothDevice.EXTRA_DEVICE))//When none device was selected
                        setStatus("None device selected");
                    else
                        setStatus("Trying connect");
                    break;
                case BluetoothListener.STATUS_DEVICE_CONNECTED:
                    setStatus("Device: connected");
                    break;
                case BluetoothListener.STATUS_PERMISSION_REQUIRED:
                    setStatus("Fine location permission required");
                    break;
                case BluetoothListener.STATUS_SEARCHING_FOR_SERVICES:
                    setStatus("Searching for services");
                    break;
                case BluetoothListener.STATUS_CONNECTED_AS_SERVER_CANNOT_BE_A_CLIENT:
                    setStatus("A server connection already exists");
                    break;
                case BluetoothListener.STATUS_CONNECTED_AS_CLIENT_CANNOT_BE_A_SERVER:
                    setStatus("A client connection already exists");
                    break;
            } // end switch EXTRA_STATUS
        } // end if has EXTRA_STATUS
        else if(intent.hasExtra(BluetoothListener.EXTRA_MESSAGE)){
            setStatus(intent.getStringExtra(BluetoothListener.EXTRA_MESSAGE));
        }
    } // end method messageReceived

    /* ** Protected methods ** */

    /**
     * Starts the Activity atributs and behaviours
     *
     * @param savedInstanceState data to reload the Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Starts the BluetoothManager
        bluetoothManager = BluetoothManager.getInstance(this);

        txtStatus = findViewById(R.id.txtStatus);
        edtMessage = findViewById(R.id.edtMessage);

        //Sets buttons behaviours
        AppCompatButton btn = findViewById(R.id.btnTurnOn);
        btn.setOnClickListener(this);

        btn = findViewById(R.id.btnTurnOff);
        btn.setOnClickListener(this);

        btn = findViewById(R.id.btnShowDeviceName);
        btn.setOnClickListener(this);

        btn = findViewById(R.id.btnOpenService);
        btn.setOnClickListener(this);

        btn = findViewById(R.id.btnSearchService);
        btn.setOnClickListener(this);

        btn = findViewById(R.id.btnSendMessage);
        btn.setOnClickListener(this);

        btn = findViewById(R.id.btnGetConnected);
        btn.setOnClickListener(this);

        //Verifies if the Bluetooth is enabled
        if(bluetoothManager.getBluetoothAdapter().isEnabled()){
            setStatus("Bluetooth on");
        }
        else{
            setStatus("Bluetooth off");
        }
    } // end onCreate method

    /**
     * Receivers the answers from others activities called
     * In the BtConn this happens when:
     * - turn Bluetooth on
     * - turn discoverable (open service)
     *
     * @param requestCode Code number to identify the request
     * @param resultCode Status code returned from called activity
     * @param data Data received from called activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case TURN_ON: // It is important when the user does not enable
                if(resultCode == RESULT_CANCELED){
                    setStatus("Bluetooth off");
                }
                break;
            case TURN_DISCOVERABLE: // It is important when the user does not enable
                if(resultCode == RESULT_CANCELED){
                    if(bluetoothManager.getBluetoothAdapter().isEnabled()){
                        setStatus("Bluetooth on");
                    }
                    else{
                        setStatus("Bluetooth off");
                    }
                } // end if resultCode == CANCELED
                break;
        } // end switch requestCode
    } // end onActivityResult method

    /* ** Private methods ** */

    private void setStatus(String message){
        txtStatus.setText(message);
    }
} // end MainActivity class
