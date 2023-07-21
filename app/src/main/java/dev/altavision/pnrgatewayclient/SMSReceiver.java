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


        //Sets up the notification channel
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "pnr_response_channel";
        NotificationChannel notificationChannel =
                new NotificationChannel(channelId, "PNR Responses", NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.setDescription("REG-RESP messages received, ready to send back to the iPhone");
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);

        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.cancelAll();



        //Sets up the notification with REG-RESP message data
        Intent copyIntent = new Intent(context, NotificationActionBroadcastReceiver.class);
        copyIntent.setAction("COPY_TO_CLIPBOARD");
        copyIntent.putExtra("textToCopy", messageBody);

        PendingIntent copyPendingIntent = PendingIntent.getBroadcast(context, 0, copyIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_baseline_sim_card_24) // Set the notification icon
                .setContentTitle("REG-RESP Message Received!") // Set the notification title
                .setContentText(messageBody) // Set the notification text
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Set the notification priority
                .addAction(R.drawable.ic_baseline_sim_card_24, "Copy", copyPendingIntent);

        Random rand = new Random();
        int notificationId = rand.nextInt(100000);
        notificationManager.notify(notificationId, builder.build());

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //This runs whenever a regular SMS is received--i.e. to capture the incoming REG-REQ message from the iPhone so
        //  we can notify the user. The user should then paste the REG-RESP contents into the ReceivePNR command
        //  on the iPhone via SSH
        Log.d("SMSRECEIVER", "Received intent!");

        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        SmsMessage[] extractMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

        for (int i = 0; i < extractMessages.length; i++) {

            if (mPrefs.getString("gateway_address","").trim().equals("")) {
                Toast.makeText(context, "Error: Please set the gateway address in Settings", Toast.LENGTH_SHORT).show();
                continue;
            }

            if (extractMessages[i].getOriginatingAddress().equals(mPrefs.getString("gateway_address","none"))) {

                Log.d("SMS_RCVR","Got message from gateway, notifying...");
                processResponseMessage(extractMessages[i].getMessageBody(), context);

            }

        }

    }
}
