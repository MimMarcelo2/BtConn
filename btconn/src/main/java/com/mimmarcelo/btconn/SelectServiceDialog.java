/**
 * File name: SelectServiceDialog
 * Defines how the popup of open services should works
 */
package com.mimmarcelo.btconn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

final class SelectServiceDialog implements BluetoothListener {

    /* ** Private static attributes ** */

    //Singleton pattern
    private static SelectServiceDialog selectServiceDialog;

    /* ** Private attributes ** */

    private BluetoothListener listener; // Observer pattern
    private AlertDialog alertDialog;
    private List<BluetoothDevice> bluetoothDevices;
    private ArrayAdapter<BluetoothDevice> adapter;
    private int selectedService;

    /* ** Constructors ** */

    /**
     * Build the customized AlertDialog
     *
     * @param context  Context from BluetoothManager
     * @param listener Observer that receivers the selected data
     */
    private SelectServiceDialog(Context context, final BluetoothListener listener) {
        AlertDialog.Builder selectService = new AlertDialog.Builder(context);
        selectService.setTitle("Select service");

        this.listener = listener;
        bluetoothDevices = new ArrayList<>();
        adapter = new DeviceListAdapter(context, bluetoothDevices);

        // Defines Dialog layout
        selectService.setSingleChoiceItems(adapter, 1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedService = which;
            }
        });

        // Defines events to CANCEL and CONFIRM
        selectService.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closeDialog(false);
            }
        });
        selectService.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closeDialog(true);
            }
        });

        alertDialog = selectService.create();
    } // end constructor SelectServiceDialog

    /* ** Public methods ** */

    /**
     * Receives data from BluetoothBroadcast when a device is found
     *
     * @param intent Data received
     */
    @Override
    public void messageReceived(Intent intent) {
        if (intent.hasExtra(BluetoothListener.EXTRA_STATUS)) {
            if (intent.getIntExtra(BluetoothListener.EXTRA_STATUS, 0) == BluetoothListener.STATUS_DEVICE_FOUND) {
                if (intent.hasExtra(BluetoothDevice.EXTRA_DEVICE)) {
                    bluetoothDevices.add((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                    adapter.notifyDataSetChanged();
                }
            }
        } // end if has EXTRA_STATUS
    } // end messageReceived method

    /* ** Protected static methods ** */

    /**
     * Creates and/or returns a SelectServiceDialog instance (Singleton pattern)
     *
     * @param context  Context from BluetoothManager
     * @param listener Observer that receivers the selected data
     * @return SelectedServiceDialog instance
     */
    protected static SelectServiceDialog getInstance(Context context, final BluetoothListener listener) {
        if (selectServiceDialog == null) {
            selectServiceDialog = new SelectServiceDialog(context, listener);
        }
        return selectServiceDialog;
    }

    /* ** Protected methods ** */

    /**
     * Cleans the list params
     * Starts the discovery of BluetoothAdapter
     * Shows the AlertDialog
     */
    protected void show() {
        bluetoothDevices.clear();
        adapter.clear();
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
        alertDialog.show();
    }

    /* ** Private methods ** */

    /**
     * Send a response to BluetoothManager (listener)
     *
     * @param confirmed If it was clicled on "CONFIRM" button (true) or on "CANCEL" button (false)
     */
    private void closeDialog(boolean confirmed) {
        Intent intent = new Intent();
        intent.putExtra(BluetoothListener.EXTRA_STATUS, BluetoothListener.STATUS_DEVICE_SELECTED);
        if (selectedService >= 0 && confirmed && adapter.getItem(selectedService) != null) {
            intent.putExtra(BluetoothDevice.EXTRA_DEVICE, adapter.getItem(selectedService));
        }
        listener.messageReceived(intent);
    } // end closeDialog method
} // end SelectServiceDialog class
