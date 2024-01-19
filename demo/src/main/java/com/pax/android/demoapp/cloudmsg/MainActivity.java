package com.pax.android.demoapp.cloudmsg;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static Handler handler = new Handler();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}
