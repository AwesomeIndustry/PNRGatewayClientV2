package dev.altavision.pnrgatewayclient;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.Random;

public class SMSReceiver extends BroadcastReceiver {

    private SharedPreferences mPrefs = null;

    public static void processResponseMessage(String messageBody, Context context) {
        //Runs when a REG-RESP message is received, either as a regular SMS or a PDU

        // Get a reference to the NotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

// Create a unique channel ID for your notifications (required for Android 8.0+)
        String channelId = "pnr_response_channel";

// Create a NotificationChannel with a unique ID and name (required for Android 8.0+)
        NotificationChannel notificationChannel =
                new NotificationChannel(channelId, "PNR Responses", NotificationManager.IMPORTANCE_HIGH);

// Configure the NotificationChannel's properties (optional)
        notificationChannel.setDescription("REG-RESP messages received, ready to send back to the iPhone");
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);

// Register the NotificationChannel with the system
        notificationManager.createNotificationChannel(notificationChannel);

        notificationManager.cancelAll();



        Intent copyIntent = new Intent(context, NotificationActionBroadcastReceiver.class);
        copyIntent.setAction("COPY_TO_CLIPBOARD");
        copyIntent.putExtra("textToCopy", messageBody);

        PendingIntent copyPendingIntent = PendingIntent.getBroadcast(context, 0, copyIntent, PendingIntent.FLAG_IMMUTABLE);



// Create a NotificationCompat.Builder object
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_baseline_sim_card_24) // Set the notification icon
                .setContentTitle("REG-RESP Message Received!") // Set the notification title
                .setContentText(messageBody) // Set the notification text
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Set the notification priority
                .addAction(R.drawable.ic_baseline_sim_card_24, "Copy", copyPendingIntent);


        Random rand = new Random();

// Create a unique notification ID for your notification
//        int notificationId = (int) (System.currentTimeMillis() / 1000);
        int notificationId = rand.nextInt(100000);

// Build the notification and send it
        notificationManager.notify(notificationId, builder.build());

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //This runs whenever an SMS is received--i.e. to capture the incoming REG-REQ message from the iPhone so
        //  we can forward it on to the gateway address (the Apple registration phone number)
        Log.d("SMSRECEIVER", "Received intent!");

        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        SmsMessage[] extractMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

        for (int i = 0; i < extractMessages.length; i++) {

            //Loops through each message and prints out some basic information for debugging
            Log.d("SMSRECEIVER", extractMessages[i].toString());
            Log.d("SMSRECEIVER", "\tMessage body: "+extractMessages[i].getMessageBody());
            Log.d("SMSRECEIVER", "\tMessage class: "+String.valueOf(extractMessages[i].getMessageClass()));
            Log.d("SMSRECEIVER", "\tMessage PDU: "+extractMessages[i].getPdu().toString());
            Log.d("SMSRECEIVER", "\tDisplay message body: "+extractMessages[i].getDisplayMessageBody());

            Log.d("SMSRECEIVER", "\tOriginating address: "+extractMessages[i].getOriginatingAddress());
            Log.d("SMSRECEIVER", "\tDisplay originating address: "+extractMessages[i].getDisplayOriginatingAddress());
            Log.d("SMSRECEIVER", "\tEmail body: "+extractMessages[i].getEmailBody());
            Log.d("SMSRECEIVER", "\tEmail from: "+extractMessages[i].getEmailFrom());

            Log.d("SMSRECEIVER", "\tPseudo-subject: "+extractMessages[i].getPseudoSubject());
            Log.d("SMSRECEIVER", "\tService center address: "+extractMessages[i].getServiceCenterAddress());
            Log.d("SMSRECEIVER", "\tIndex on ICC: "+String.valueOf(extractMessages[i].getIndexOnIcc()));

            Log.d("SMSRECEIVER", "\tProtocol identifier: "+String.valueOf(extractMessages[i].getProtocolIdentifier()));
            Log.d("SMSRECEIVER", "\tStatus: "+String.valueOf(extractMessages[i].getStatus()));
            Log.d("SMSRECEIVER", "\tUser data: "+String.valueOf(extractMessages[i].getUserData()));

            if (mPrefs.getString("gateway_address","none").equals("none")) {
                Toast.makeText(context, "Error: Please set the gateway address in Settings", Toast.LENGTH_SHORT).show();
                continue;
            }

//            if (mPrefs.getString("iphone_number","none").equals("none")) {
//                Toast.makeText(context, "Error: Please set the iPhone number in Settings", Toast.LENGTH_SHORT).show();
//                continue;
//            }


//            if (extractMessages[i].getOriginatingAddress().equals(mPrefs.getString("iphone_number","none"))) {
//                //Any message coming from the iPhone gets forwarded to the gateway address
//                SmsManager smsManager = SmsManager.getDefault();
//                smsManager.sendTextMessage(mPrefs.getString("gateway_address","none"), null, extractMessages[i].getMessageBody(), null, null);
//
//            }

            Log.d("SMS_RCVR","Checking "+extractMessages[i].getOriginatingAddress()+" against "+mPrefs.getString("gateway_address","none"));

            if (extractMessages[i].getOriginatingAddress().equals(mPrefs.getString("gateway_address","none"))) {

                Log.d("SMS_RCVR","It came from the gateway! Sending back...");

                processResponseMessage(extractMessages[i].getMessageBody(), context);

//                SmsManager smsManager = SmsManager.getDefault();
//                smsManager.sendTextMessage(mPrefs.getString("iphone_number","none"), null, extractMessages[i].getMessageBody(), null, null);
            }

        }

    }
}
