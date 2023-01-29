package com.cwdt.junnan.nopassword_vivo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int REQUEST_VPN = 1;


    private LinearLayout white_l;
    private LinearLayout open_l;
    private ImageView open_img;
    private TextView open_text;
    private TextView upgrade_text;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        white_l = findViewById(R.id.white_l);
        open_l = findViewById(R.id.open_l);

        open_img = findViewById(R.id.open_img);
        open_text = findViewById(R.id.open_text);
        upgrade_text = findViewById(R.id.upgrade_text);


        white_l.setOnClickListener(onClickListener);
        open_l.setOnClickListener(onClickListener);
        upgrade_text.setOnClickListener(onClickListener);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = prefs.getBoolean("enabled", false);
        prefs.registerOnSharedPreferenceChangeListener(this);
        open_img.setImageResource(enabled ? R.drawable.stop : R.drawable.open);
        open_text.setText(enabled ? "停止" : "开启");

    }


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.white_l:
                    IntentWrapper.whiteListMatters(MainActivity.this, "拒绝密码的持续运行");
                    break;
                case R.id.open_l:
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    boolean enabled = prefs.getBoolean("enabled", false);
                    if (enabled) {
                        prefs.edit().putBoolean("enabled", false).apply();
                        NetFilterService.stop(MainActivity.this);
                    } else {
                        Intent prepare = VpnService.prepare(MainActivity.this);
                        if (prepare == null) {
                            onActivityResult(REQUEST_VPN, RESULT_OK, null);
                        } else {
                            try {
                                startActivityForResult(prepare, REQUEST_VPN);
                            } catch (Throwable ex) {
                                onActivityResult(REQUEST_VPN, RESULT_CANCELED, null);
                            }
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String name) {
        if ("enabled".equals(name)) {
            boolean enabled = prefs.getBoolean(name, false);
            open_img.setImageResource(enabled ? R.drawable.stop : R.drawable.open);
            open_text.setText(enabled ? "停止" : "开启");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VPN) {
            prefs.edit().putBoolean("enabled", resultCode == RESULT_OK).apply();

            if (resultCode == RESULT_OK)
                NetFilterService.start(this);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

}
