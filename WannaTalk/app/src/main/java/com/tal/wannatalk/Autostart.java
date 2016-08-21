package com.tal.wannatalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Autostart extends BroadcastReceiver
{
    public void onReceive(Context arg0, Intent arg1)
    {
        Intent intent = new Intent(arg0,BgService.class);
        arg0.startService(intent);
        Log.i("Autostart", "started");
    }
}