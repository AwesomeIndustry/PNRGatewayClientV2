package dev.altavision.pnrgatewayclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.nio.charset.StandardCharsets;

public class PDUReceiver extends BroadcastReceiver {
    private SharedPreferences mPrefs = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Runs whenever a data SMS PDU is received. On some carriers (i.e. AT&T), the REG-RESP message is sent as
        //  a data SMS (PDU) instead of a regular SMS message.

        Log.d("PDU_RCVR", "Got data SMS!!");

        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Bundle bundle = intent.getExtras();
        SmsMessage recMsg = null;
        byte[] data = null;
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            Log.d("PDU_RCVR","Got received PDUs: "+pdus.toString());

            //Loops through each received SMS
            for (int i = 0; i < pdus.length; i++) {
                recMsg = SmsMessage.createFromPdu((byte[]) pdus[i]);

                if (mPrefs.getString("gateway_address","").trim().equals("")) {
                    Toast.makeText(context, "Error: Please set the gateway address in Settings", Toast.LENGTH_SHORT).show();
                    continue;
                }

                if (!recMsg.getOriginatingAddress().equals(mPrefs.getString("gateway_address", "<not set>"))) {
                    continue;
                }

                //messageBody will include the REG-RESP text--i.e.
                //  REG-RESP?v=3;r=72325403;n=+11234567890;s=CA21C50C645469B25F4B65C38A7DCEC56592E038F39489F35C7CD6972D
                String messageBody = recMsg.getMessageBody();

                //On some carriers, the PDU received is not readable by the default Android functions.
                //This results in the message body being null despite receiving the data
                if (messageBody == null) {
                    messageBody = new String((byte[]) pdus[i], StandardCharsets.US_ASCII);
                    int startIndex = messageBody.indexOf("REG-RESP");
                    messageBody = messageBody.substring(startIndex);
                }

                //Hands the REG-RESP message off to the SMSReceiver to notify the user
                SMSReceiver.processResponseMessage(messageBody, context);

            }
        }
    }
}
