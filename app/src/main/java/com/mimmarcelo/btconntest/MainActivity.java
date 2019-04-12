package com.mimmarcelo.btconntest;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.TextView;

import com.mimmarcelo.btconn.BluetoothListener;
import com.mimmarcelo.btconn.BluetoothManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BluetoothListener {

    //Constants
    private final int TURN_ON = 1;
    private final int TURN_DISCOVERABLE = 2;

    private BluetoothManager bluetoothManager;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothManager = BluetoothManager.getBluetoothManager(this);

        txtStatus = findViewById(R.id.txtStatus);

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

        if(bluetoothManager.isActived()){
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
                bluetoothManager.turnOn(TURN_ON);
                setStatus("Asking for permission...");
                break;
            case R.id.btnTurnOff:
                bluetoothManager.turnOff();//Message appears on messageReceived method
                break;
            case R.id.btnSendMessage:
                bluetoothManager.sendMessage("Marcelo Júnior:;Hello World!");
                break;
            case R.id.btnOpenService:
                setStatus("Asking to open service");
                bluetoothManager.askToOpenService(TURN_DISCOVERABLE, 20);
                break;
            case R.id.btnSearchService:
                setStatus("Searching for service");
                break;
            case R.id.btnShowDeviceName:
                setStatus("Device name: " + bluetoothManager.getDeviceName());
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case TURN_ON:
                if(requestCode == RESULT_CANCELED){//It is important when the user does not enable
                    setStatus("Bluetooth off");
                }
                break;
            case TURN_DISCOVERABLE:
                if(resultCode == RESULT_CANCELED){//It is important when the user does not enable
                    if(bluetoothManager.isActived()){
                        setStatus("Bluetooth on");
                    }
                    else{
                        setStatus("Bluetooth off");
                    }
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
                case BluetoothListener.STATUS_DEVICE_CONNECTED:
                    BluetoothDevice device = bluetoothManager.getDevice();
                    setStatus("Device: " + device.getName() + " connected");
                    break;
            }
        }
        else if(intent.hasExtra(BluetoothListener.EXTRA_MESSAGE)){
            setStatus(intent.getStringExtra(BluetoothListener.EXTRA_MESSAGE));
        }
    }
}
