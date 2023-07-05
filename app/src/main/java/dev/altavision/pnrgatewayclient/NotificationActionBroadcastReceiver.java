package dev.altavision.pnrgatewayclient;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

public class NotificationActionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent.getAction().equals("COPY_TO_CLIPBOARD")) {
            String textToCopy = intent.getStringExtra("textToCopy");
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("REG-RESP Message", textToCopy);
            clipboard.setPrimaryClip(clip);
        }
    }
}