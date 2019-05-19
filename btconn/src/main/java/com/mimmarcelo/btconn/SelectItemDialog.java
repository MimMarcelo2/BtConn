/**
 * File name: SelectItemDialog
 * Defines how the popup of open services should works
 */
package com.mimmarcelo.btconn;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

final class SelectItemDialog implements DialogInterface.OnDismissListener {

    /* ** Private attributes ** */

    private BluetoothListener listener; // Observer pattern
    private List<BluetoothItem> bluetoothItems;
    private ArrayAdapter<BluetoothItem> adapter;
    private int requestCode;
    private int resultCode;
    private int selectedItem;

    /* ** Constructors ** */

    protected SelectItemDialog(Context context, BluetoothListener listener, List<BluetoothItem> bluetoothItems, int requestCode){
        this.listener = listener;
        this.bluetoothItems = bluetoothItems;
        this.requestCode = requestCode;
        this.resultCode = Activity.RESULT_CANCELED;
        this.bluetoothItems = bluetoothItems;
        this.adapter = new ItemListAdapter(context, this.bluetoothItems);

        AlertDialog.Builder selectService = new AlertDialog.Builder(context)
                .setTitle("Select service")
                .setOnDismissListener(this)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resultCode = Activity.RESULT_OK;
                    }
                })
                .setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedItem = which;
                    }
                });

        selectService.create().show();
    }
    /**
     * Build the customized AlertDialog
     *
     * @param context  Context from BluetoothManager
     * @param listener Observer that receivers the selected data
     */
    protected SelectItemDialog(Context context, BluetoothListener listener, int requestCode) {
        this(context, listener, new ArrayList<BluetoothItem>(), requestCode);
    } // end constructor SelectItemDialog

    /* ** Public methods ** */

    @Override
    public void onDismiss(DialogInterface dialog) {
        Intent intent = null;
        if (resultCode == Activity.RESULT_OK) {
            if (selectedItem >= 0 && adapter.getItem(selectedItem) != null) {
                intent = new Intent();
                if (adapter.getItem(selectedItem).getInstance() instanceof Serializable)
                    intent.putExtra(BluetoothListener.EXTRA_CONNECTION, (Serializable) adapter.getItem(selectedItem).getInstance());
                else
                    intent.putExtra(BluetoothListener.EXTRA_CONNECTION, (Parcelable) adapter.getItem(selectedItem).getInstance());
            } else {
                resultCode = Activity.RESULT_CANCELED;
            }
        }
        listener.onActivityResult(requestCode, resultCode, intent);
    }

    /* ** Protected methods ** */

    protected void update(BluetoothItem item) {
        bluetoothItems.add(item);
        adapter.notifyDataSetChanged();
    }

} // end SelectItemDialog class
