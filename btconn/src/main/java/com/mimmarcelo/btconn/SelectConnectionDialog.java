/**
 * File name: SelectConnectionDialog
 * Defines how the popup of current connection should works
 */
package com.mimmarcelo.btconn;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

final class SelectConnectionDialog {

    /* ** Private static attributes ** */

    //Singleton pattern
    private static SelectConnectionDialog selectConnectionDialog;

    /* ** Private attributes ** */

    private BluetoothListener listener; // Observer pattern
    private AlertDialog alertDialog;
    private List<ConnectionThread> connectionThreads;
    private ArrayAdapter<ConnectionThread> adapter;
    private int selectedService;

    /* ** Constructors ** */

    /**
     * Build the customized AlertDialog
     *
     * @param context  Context from BluetoothManager
     * @param listener Observer that receivers the selected data
     */
    private SelectConnectionDialog(Context context, final BluetoothListener listener) {
        AlertDialog.Builder selectConnection = new AlertDialog.Builder(context);
        selectConnection.setTitle("Select connection");

        this.listener = listener;
        connectionThreads = new ArrayList<>();
        adapter = new ConnectionListAdapter(context, connectionThreads);

        // Defines Dialog layout
        selectConnection.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedService = which;
            }
        });

        // Defines events to CANCEL and CONFIRM
        selectConnection.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closeDialog(Activity.RESULT_CANCELED);
            }
        });
        selectConnection.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closeDialog(Activity.RESULT_OK);
            }
        });

        alertDialog = selectConnection.create();
    } // end constructor SelectServiceDialog

    /* ** Protected static methods ** */

    /**
     * Creates and/or returns a SelectConnectionDialog instance (Singleton pattern)
     *
     * @param context  Context from BluetoothManager
     * @param listener Observer that receivers the selected data
     * @return SelectedConnectionDialog instance
     */
    public static SelectConnectionDialog getInstance(Context context, final BluetoothListener listener) {
        if (selectConnectionDialog == null) {
            selectConnectionDialog = new SelectConnectionDialog(context, listener);
        }
        return selectConnectionDialog;
    }

    /* ** Protected methods ** */

    /**
     * Receivers the current connection list and updates the list adapter to be showed
     *
     * @param connectionThreads Current connection list
     */
    public void show(List<ConnectionThread> connectionThreads) {
        this.connectionThreads.clear();
        this.connectionThreads.addAll(connectionThreads);
        adapter.notifyDataSetChanged();
        alertDialog.show();
    }

    /* ** Private methods ** */

    /**
     * Send a response to BluetoothManager (listener)
     *
     * @param resultCode If it was pushed on "CONFIRM" button (true) or on "CANCEL" button (false)
     */
    private void closeDialog(int resultCode) {
        Intent intent = null;
        if(resultCode == Activity.RESULT_OK) {
            if (selectedService >= 0 && adapter.getItem(selectedService) != null) {
                intent = new Intent();
                intent.putExtra(BluetoothListener.EXTRA_CONNECTION, adapter.getItem(selectedService));
            }
            else {
                resultCode = Activity.RESULT_CANCELED;
            }
        }
        listener.onActivityResult(BluetoothListener.CLOSE_CONNECTION, resultCode, intent);
    } // end closeDialog method
} // end SelectConnectionDialog class
