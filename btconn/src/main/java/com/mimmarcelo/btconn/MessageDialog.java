package com.mimmarcelo.btconn;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

class MessageDialog implements DialogInterface.OnDismissListener {

    BluetoothListener listener;
    int requestCode;
    int resultCode;

    protected MessageDialog(Context context, BluetoothListener listener, String title, String message, int requestCode) {
        this.listener = listener;
        this.requestCode = requestCode;
        this.resultCode = Activity.RESULT_CANCELED;

        AlertDialog.Builder selectService = getMessageDialog(context, title, message);
        selectService.create().show();
    }

    private AlertDialog.Builder getMessageDialog(Context context, String title, String message){
        return new android.support.v7.app.AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setOnDismissListener(this)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resultCode = Activity.RESULT_OK;
                    }
                });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        listener.onActivityResult(requestCode, resultCode, null);
    }
}
