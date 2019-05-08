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

import com.mimmarcelo.btconn.BluetoothBuilder;
import com.mimmarcelo.btconn.BluetoothListener;
import com.mimmarcelo.btconn.BluetoothManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BluetoothListener {

    /* ** Private constants ** */

//    private final int TURN_ON = 1;
//    private final int TURN_DISCOVERABLE = 2;
//    private final int ASK_PERMISSION = 3;

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
        switch (v.getId()) {
            case R.id.btnTurnOn:
                setStatus("Asking for permission...");
                bluetoothManager.turnBluetoothOn();
                break;
            case R.id.btnTurnOff:
                bluetoothManager.turnBluetoothOff();//Message appears on sendError method
                break;
            case R.id.btnShowDeviceName:
                setStatus("Device name: " + bluetoothManager.getBluetoothAdapter().getName());
                break;
            case R.id.btnOpenService:
                setStatus("Asking to open service");
                bluetoothManager.turnDiscoverableOn(15);
                break;
            case R.id.btnSearchService:
                setStatus("Searching for service");
                bluetoothManager.searchForServices();
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
            case R.id.btnCloseConnection:
                bluetoothManager.selectConnectionToClose();
                break;
            case R.id.btnSendMessage:
                String msg = edtMessage.getText().toString();
                if(!msg.isEmpty()) {
                    bluetoothManager.sendMessage(msg);
                    edtMessage.setText("");
                }
                break; // end btnSendMessage case
        } // end switch v.getId
    } // end onClick method

    /**
     * Receivers the answer from required permission
     * And performs how to work on each result
     *
     * @param requestCode  Code to identify when asking some permission
     *                     In BtConn library it happens on searchForServices method
     * @param permissions  List od permissions required
     * @param grantResults list of status to each permission required
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case BluetoothListener.PERMISSION_REQUIRED:
                // Permission denied
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    setStatus("Permission denied");
                } else {
                    //if enabled, it is possible keep with the process
                    setStatus("Permission enabled");
                    bluetoothManager.searchForServices();
                }
                break; // end ASK_PERMISSION case

        } // end switch requestCode
    } // end onRequestPermissionResult method

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
        bluetoothManager = new BluetoothBuilder(this)
                .setUuid("00001101-0000-1000-8000-00805F9B34FB")
                .build();

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

        btn = findViewById(R.id.btnGetConnected);
        btn.setOnClickListener(this);

        btn = findViewById(R.id.btnCloseConnection);
        btn.setOnClickListener(this);

        btn = findViewById(R.id.btnSendMessage);
        btn.setOnClickListener(this);

        //Verifies if the Bluetooth is enabled
        if (bluetoothManager.getBluetoothAdapter().isEnabled()) {
            setStatus("Bluetooth on");
        } else {
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
     * @param resultCode  Status code returned from called activity
     * @param data        Data received from called activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BluetoothListener.TURN_BLUETOOTH_ON:
                if (resultCode == RESULT_OK) {
                    setStatus("Bluetooth on");
                } else if (resultCode == RESULT_CANCELED) {
                    setStatus("Turn bluetooth on canceled by user");
                } else if (resultCode == BluetoothListener.BLUETOOTH_ALREADY_ON) {
                    setStatus("Bluetooth already on");
                } else {
                    setStatus("Status not analyzed: " + data.getIntExtra(BluetoothListener.EXTRA_STATUS, 0));
                }
                break;
            case BluetoothListener.TURN_BLUETOOTH_OFF:
                if (resultCode == RESULT_OK) {
                    setStatus("Bluetooth off");
                } else {
                    setStatus("Bluetooth already off");
                }
                break;
            case BluetoothListener.TURN_DISCOVERABLE_ON:
                if (resultCode == RESULT_OK) {
                    setStatus("Waiting some connection...");
                } else if (resultCode == RESULT_CANCELED) {
                    setStatus("Turn discoverable on canceled by user");
                } else if (resultCode == CONNECTED_AS_CLIENT_CANNOT_BE_A_SERVER) {
                    setStatus("There is a connection as a client, cannot be a server");
                }
                break;
            case BluetoothListener.TURN_DISCOVERABLE_OFF:
                setStatus("Discoverable finished...");
                break;
            case BluetoothListener.TURN_SEARCHING_ON:
                if (resultCode == RESULT_OK) {
                    setStatus("Searching for available services");
                } else if (resultCode == CONNECTED_AS_SERVER_CANNOT_BE_A_CLIENT) {
                    setStatus("There is a connection as a server, cannot be a client");
                } else if (resultCode == PERMISSION_REQUIRED) {
                    setStatus("To search for services, it is necessary enable ACCESS_FINE_LOCATION permission");
                }
                break;
            case BluetoothListener.DEVICE_SELECTED:
                if (resultCode == RESULT_OK) {
                    setStatus("Trying establishes the connection...");
                } else if (resultCode == RESULT_CANCELED) {
                    setStatus("Searching canceled by user");
                }
                break;
            case BluetoothListener.DEVICE_CONNECTED:
                if (resultCode == RESULT_OK) {
                    BluetoothDevice d = data.getParcelableExtra(BluetoothListener.EXTRA_DEVICE);
                    setStatus("Connected with: " + d.getName() + ":" + d.getAddress());
                } else {
                    setStatus("It was not possible establishes the connection");
                }
                break;
            case BluetoothListener.DEVICE_DISCONNECTED:
                if (resultCode == RESULT_OK) {
                    BluetoothDevice d = data.getParcelableExtra(BluetoothListener.EXTRA_DEVICE);
                    setStatus("Device: " + d.getName() + ":" + d.getAddress() + " disconnected");
                }
                break;
            case BluetoothListener.CLOSE_CONNECTION:
                if(resultCode == RESULT_OK){
                    setStatus("closing connection...");
                }
                else if(resultCode == RESULT_CANCELED){
                    setStatus("Close connection canceled by user");
                }
                else if(resultCode == BluetoothListener.NO_CONNECTIONS){
                    setStatus("No connections activated");
                }
                break;
            case BluetoothListener.MESSAGE_RECEIVED:
                if(data.hasExtra(BluetoothListener.EXTRA_MESSAGE)) {
                    setStatus(data.getStringExtra(BluetoothListener.EXTRA_MESSAGE));
                }
                break;
        } // end switch requestCode
    } // end sendError method

    /* ** Private methods ** */

    private void setStatus(String message) {
        txtStatus.setText(message);
    }
} // end MainActivity class
