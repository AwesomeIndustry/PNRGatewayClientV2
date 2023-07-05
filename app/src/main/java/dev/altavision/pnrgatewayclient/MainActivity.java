package dev.altavision.pnrgatewayclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN_ACT";
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

//        requestSmsPermission();





        ((Button) findViewById(R.id.send_req_sms_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

//                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
//                    startActivity(intent);


                    String rawTokenMessage = ((TextView) findViewById(R.id.push_token_field)).getText().toString();
                    //Regex is \<([a-f0-9\ ]+)\>
                    String regex = "\\<([a-f0-9\\ ]+)\\>";

                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(rawTokenMessage);

                    if (matcher.find()) {
                        Log.d(TAG,"Match found at index " + matcher.start());

                        Log.d(TAG,"Match group 1 " + matcher.group(1));
//                        Log.d(TAG,"Match group 2 " + matcher.group(2));

                        String pushTokenCleaned = matcher.group(1).replaceAll("\\s", "").toUpperCase(); //Cleans up the push token so it's in the all-caps, no-spaces format

                        Random random = new Random();
                        long randomNum = (long) (random.nextDouble() * 10000000000L);

                        String smsToSend = "REG-REQ?v=3;t="+pushTokenCleaned+";r="+randomNum;


                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(mPrefs.getString("gateway_address","none"), null, smsToSend, null, null);



                    } else {
                        Log.w(TAG,"Match not found.");
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        ((Button) findViewById(R.id.smsbutton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    //    private void requestSmsPermission() {
////        permission = Manifest.permission.RECEIVE_SMS;
//    }
}