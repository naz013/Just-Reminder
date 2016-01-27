package com.backdoor.moove;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(MainActivity.this, WearService.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
