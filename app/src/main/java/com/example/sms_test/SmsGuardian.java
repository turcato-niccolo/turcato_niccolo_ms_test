package com.example.sms_test;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsGuardian extends BroadcastReceiver {

    private static final String TAG = SmsGuardian.class.getSimpleName();
    public static final String pdu_type = "pdus";

    SmsManager sender;

    public SmsGuardian()
    {
        sender = SmsManager.getDefault();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the SMS message.
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        String strMessage = "";
        String format = bundle.getString("format");
        // Retrieve the SMS message received.
        Object[] pdus = (Object[]) bundle.get(pdu_type);
        if (pdus != null) {
            // Check the Android version.
            boolean isVersionM =
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
            // Fill the msgs array.
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                // Check Android version and use appropriate createFromPdu.
                if (isVersionM) {
                    // If Android version M or newer:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    // If Android version L or older:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                // Build the message to show.
                strMessage += "SMS from " + msgs[i].getOriginatingAddress();
                strMessage += " : " + msgs[i].getMessageBody() + "\n";
                // Log and display the SMS message.
                Log.d(TAG, "onReceive: " + strMessage);
                Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    public String Send(String number, String text) {
        //ritorna "1" se c'è stato un errore legato agli argomenti, "0" se l'invio è andato a buon fine
        //per gli altri tipi di eccezione ritorna direttamente la stringa di errore
        String exitCode = "0";

        try {
            sender.sendTextMessage(number, null, text, null, null);
        }
        catch (IllegalArgumentException ex) {

            exitCode = "1";
        }
        catch (Exception e) {
            exitCode = e.getMessage() + " SMS failed, please try again.";

        }

        return exitCode;
    }


}
