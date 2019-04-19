package com.mimmarcelo.btconntest;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.TextView;

import com.mimmarcelo.btconn.BluetoothListener;
import com.mimmarcelo.btconn.BluetoothManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BluetoothListener {

    //Constants
    private final int TURN_ON = 1;
    private final int TURN_DISCOVERABLE = 2;
    private final int ASK_PERMISSION = 3;

    private BluetoothManager bluetoothManager;
    private TextView txtStatus;
    private AppCompatEditText edtMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothManager = BluetoothManager.getBluetoothManager(this);

        txtStatus = findViewById(R.id.txtStatus);
        edtMessage = findViewById(R.id.edtMessage);

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

        if(bluetoothManager.getBluetoothAdapter().isEnabled()){
            setStatus("Bluetooth on");
        }
        else{
            setStatus("Bluetooth off");
        }
    }

    private void setStatus(String message){
        txtStatus.setText(message);
    }

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
                break;
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
                if(bluetoothManager.getDevice() != null){
                    setStatus(bluetoothManager.getDevice().getName()+":"+bluetoothManager.getDevice().getAddress()+" connected");
                }
                else {
                    setStatus("None device connected");
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case TURN_ON://It is important when the user does not enable
                if(resultCode == RESULT_CANCELED){
                    setStatus("Bluetooth off");
                }
                break;
            case TURN_DISCOVERABLE://It is important when the user does not enable
                if(resultCode == RESULT_CANCELED){
                    if(bluetoothManager.getBluetoothAdapter().isEnabled()){
                        setStatus("Bluetooth on");
                    }
                    else{
                        setStatus("Bluetooth off");
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ASK_PERMISSION: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {//Permission denied
                    setStatus("Permission denied");
                } else {
                    //if enabled, it is possible keep with the process
                    setStatus("Permission enabled");
                    bluetoothManager.searchForOpenService(ASK_PERMISSION);
                }
                break;
            }
        }
    }

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
                    BluetoothDevice device = bluetoothManager.getDevice();
                    setStatus("Device: " + device.getName() + " connected");
                    break;
                case BluetoothListener.STATUS_PERMISSION_REQUIRED:
                    setStatus("Fine location permission required");
                    break;
                case BluetoothListener.STATUS_SEARCHING_FOR_SERVICES:
                    setStatus("Searching for services");
                    break;
            }
        }
        else if(intent.hasExtra(BluetoothListener.EXTRA_MESSAGE)){
            setStatus(intent.getStringExtra(BluetoothListener.EXTRA_MESSAGE));
        }
    }
}
