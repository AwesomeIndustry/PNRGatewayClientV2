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

public class PDUReceiver extends BroadcastReceiver {
//    public static final String GATEWAY_ADDRESS = "22223333"; //Google Fi/TMobile MVNO
//    public static final String IPHONE_NUMBER = "+11234567890";

    private SharedPreferences mPrefs = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Runs whenever a data SMS PDU is received. This will happen when Apple sends us the REG-RESP message
        //  after we've sent them our REG-REQ message
        Log.d("PDU_RCVR", "Got data SMS!!");

        //NOTE: On some carriers, the received SMS might be a regular SMS instead of a data SMS. I'm pretty
        //      sure most carriers transmit the data SMS, but if (during normal registration) you see the
        //      REG-RESP messages displayed in the Messages app on your iPhone, they might be standard SMSes
        //      and won't get picked up here.

//        if (mPrefs == null) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//        }

        Bundle bundle = intent.getExtras();
        String recMsgString = "";
        String fromAddress = "";
        SmsMessage recMsg = null;
        byte[] data = null;
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            Log.d("PDU_RCVR","Got received PDUs: "+pdus.toString());

            //Loops through each received SMS
            for (int i = 0; i < pdus.length; i++) {
                recMsg = SmsMessage.createFromPdu((byte[]) pdus[i]);

                //Logs some information about the PDU that was received (for debugging)
                Log.d("PDU_RCVR", recMsg.toString());
                Log.d("PDU_RCVR", "\tMessage body: "+recMsg.getMessageBody());
                Log.d("PDU_RCVR", "\tMessage class: "+String.valueOf(recMsg.getMessageClass()));
                Log.d("PDU_RCVR", "\tMessage PDU: "+recMsg.getPdu().toString());
                Log.d("PDU_RCVR", "\tDisplay message body: "+recMsg.getDisplayMessageBody());

                Log.d("PDU_RCVR", "\tOriginating address: "+recMsg.getOriginatingAddress());
                Log.d("PDU_RCVR", "\tDisplay originating address: "+recMsg.getDisplayOriginatingAddress());
                Log.d("PDU_RCVR", "\tEmail body: "+recMsg.getEmailBody());
                Log.d("PDU_RCVR", "\tEmail from: "+recMsg.getEmailFrom());

                Log.d("PDU_RCVR", "\tPseudo-subject: "+recMsg.getPseudoSubject());
                Log.d("PDU_RCVR", "\tService center address: "+recMsg.getServiceCenterAddress());
                Log.d("PDU_RCVR", "\tIndex on ICC: "+String.valueOf(recMsg.getIndexOnIcc()));

                Log.d("PDU_RCVR", "\tProtocol identifier: "+String.valueOf(recMsg.getProtocolIdentifier()));
                Log.d("PDU_RCVR", "\tStatus: "+String.valueOf(recMsg.getStatus()));
                Log.d("PDU_RCVR", "\tUser data: "+String.valueOf(recMsg.getUserData()));

                if (mPrefs.getString("gateway_address","none").equals("none")) {
                    Toast.makeText(context, "Error: Please set the gateway address in Settings", Toast.LENGTH_SHORT).show();
                    continue;
                }

//                if (mPrefs.getString("iphone_number","none").equals("none")) {
//                    Toast.makeText(context, "Error: Please set the iPhone number in Settings", Toast.LENGTH_SHORT).show();
//                    continue;
//                }


                if (!recMsg.getOriginatingAddress().equals(mPrefs.getString("gateway_address", "<not set>"))) {
                    continue;
                }

                //messageBody will include the REG-RESP text--i.e.
                //  REG-RESP?v=3;r=72325403;n=+11234567890;s=CA21C50C645469B25F4B65C38A7DCEC56592E038F39489F35C7CD6972D
                String messageBody = recMsg.getMessageBody();

                //TODO: Check if the SMS is coming from the gateway number and not just any random phone number

//                if (messageBody.startsWith("REG-RESP")) {
                Log.d("PDU_RCVR", "*********** Sending data sms back");
                SmsManager smsManager = SmsManager.getDefault();

                //Forwards the REG-RESP message back to the iPhone for it to complete registration
//                smsManager.sendTextMessage(mPrefs.getString("iphone_number", "<not set>"), null, messageBody, null, null);

                //TODO: Send this back to the iPhone via iMessage or something

                SMSReceiver.processResponseMessage(messageBody, context);



                //In theory it's possible to send a data SMS to the iPhone and have it pick up on that, but I couldn't get that to work


            }
        }
    }
}
