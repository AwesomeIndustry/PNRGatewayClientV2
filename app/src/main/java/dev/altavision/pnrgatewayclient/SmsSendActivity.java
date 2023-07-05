package dev.altavision.pnrgatewayclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

public class SmsSendActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_send);

        Log.d("SMS_SEND_ACT","Got SMS send activity intent!");

        Intent i = getIntent();
        Log.d("SMS_SEND_ACT", "Got intent: "+i.toString());
        Log.d("SMS_SEND_ACT", "Intent action: "+i.getAction());
        Log.d("SMS_SEND_ACT", "Intent data: "+i.getData());

//        i.
//        Log.d("SMS_SEND_ACT", "Intent extras: "+i.getExtras().);
        Bundle bundle = i.getExtras();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Log.d("SMS_SEND_ACT", key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));

//                SmsManager.RESULT_ERROR_FDN_CHECK_FAILURE
            }
        }

    }
}