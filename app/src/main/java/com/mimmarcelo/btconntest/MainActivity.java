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
import com.mimmarcelo.btconn.ConnectedThread;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BluetoothListener {

    /* ** Private attributes ** */

    /**
     * Responsible to manage the Bluetooth connections
     * <p>
     *     It works like a broadcast that receives changes from Bluetooth behavior
     *     and sends responses to {@link BluetoothListener#onActivityResult(int, int, Intent)}
     * </p>
     */
    private BluetoothManager bluetoothManager;

    //Layout components
    private TextView txtStatus;
    private AppCompatEditText edtMessage;

    /* ** Public methods ** */

    /**
     * Defines the behavior of each click button
     *
     * @param v view correspondent clicked button
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
                for(ConnectedThread conn: bluetoothManager.getConnections()) {
                    deviceMsg += conn.getDevice().getName() + ":" + conn.getDevice().getAddress() + " connected\n";
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
     * Receivers the answers from others called activities and, in this case, receives the calls
     * from btconn library.
     * <p>
     *     In the BtConn this happens with all following requestCodes:
     *     <ul>
     *         <li>{@link BluetoothListener#TURN_BLUETOOTH_ON}</li>
     *         <li>{@link BluetoothListener#TURN_BLUETOOTH_OFF}</li>
     *         <li>{@link BluetoothListener#TURN_DISCOVERABLE_ON}</li>
     *         <li>{@link BluetoothListener#TURN_DISCOVERABLE_OFF}</li>
     *         <li>{@link BluetoothListener#TURN_SEARCHING_ON}</li>
     *         <li>{@link BluetoothListener#DEVICE_FOUND}</li>
     *         <li>{@link BluetoothListener#DEVICE_SELECTED}</li>
     *         <li>{@link BluetoothListener#DEVICE_CONNECTED}</li>
     *         <li>{@link BluetoothListener#DEVICE_DISCONNECTED}</li>
     *         <li>{@link BluetoothListener#CLOSE_CONNECTION}</li>
     *         <li>{@link BluetoothListener#MESSAGE_RECEIVED}</li>
     *     </ul>
     * </p>
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
                    if(data.hasExtra(BluetoothListener.EXTRA_CONNECTION)) {
                        ConnectedThread d = (ConnectedThread)data.getSerializableExtra(BluetoothListener.EXTRA_CONNECTION);
                        setStatus("Connected with: " + d.getDevice().getName() + ":" + d.getDevice().getAddress());
                    }
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
//                    if(data.hasExtra(BluetoothListener.EXTRA_CONNECTION)){
//                        ConnectedThread conn = (ConnectedThread) data.getSerializableExtra(BluetoothListener.EXTRA_CONNECTION);
//                        conn.sendMessage("Message delivered!");
//                    }
                }
                break;
        } // end switch requestCode
    } // end sendError method

    /**
     * Receivers the answer from required permission
     * and performs how to work on each result
     *
     * <p>
     *     This method is called by btconn <em>only when it needs</em> to look for open services
     *     <em>and</em> the device version is greater than 6 {@link android.os.Build.VERSION_CODES#M}
     * </p>
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
     * Starts the Activity attrs and behaviours
     *
     * <p>
     *     It is recommended to build the {@link BluetoothManager} at this moment
     * </p>
     * @param savedInstanceState data to reload the Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Starts the BluetoothManager
        bluetoothManager = new BluetoothBuilder(this)
                .setUuid("eca150a0-10c2-4082-a1f4-f36e20f9cbd2")
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
     * Calls the {@link BluetoothManager#destroy()} method
     *
     * <p>
     *     It's necessary to close open connection and unregister the broadcast
     * </p>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothManager.destroy();
    }

    /* ** Private methods ** */

    private void setStatus(String message) {
        txtStatus.setText(message);
    }

} // end MainActivity class
