package com.taximechanic;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseActivity {

    EditText etUsername;
    EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imgIntro = findViewById(R.id.imgAnim);
        imgIntro.setBackgroundResource(R.drawable.intro_anim);
        ((AnimationDrawable)imgIntro.getBackground()).start();
        findViewById(R.id.tvLogin).setOnClickListener(this);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    alertDialog(R.string.Error, R.string.GrandExternalStoragePermission).setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    });
                    return;
                }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tvLogin:
                if (!createProgressDialog(R.string.Empty, R.string.Login)) {
                    return;
                }
                WebQuery wq = new WebQuery(WebQuery.mHostUrlMechanicAuth, WebQuery.HttpMethod.POST, WebResponse.mResponseAuthMechanic, this);
                wq.setParameter("username", etUsername.getText().toString());
                wq.setParameter("password", etPassword.getText().toString());
                wq.request();
        }
    }

    @Override
    public void webResponse(int code, int webResponse, String s) {
        super.webResponse(code, webResponse, s);
        if (webResponse != 200) {
            if (s == null) {
                s = "";
            }
            alertDialog(getString(R.string.Error), s);
            return;
        }
        switch (code) {
            case mResponseAuthMechanic:
                parseAuthentication(s);
                break;
        }
    }

    public void parseAuthentication(String s) {
        try {
            JSONObject jo = new JSONObject(s);
            JSONObject jmechanic = jo.getJSONObject("worker");
            Config.setInt("mech_id", jmechanic.getInt("id"));
            Config.setString("mech_fname", jmechanic.getString("name"));
            Config.setString("mech_lname", jmechanic.getString("surname"));
            Config.setString("mech_email", jmechanic.getString("email"));
            JSONObject jbearer = jo.getJSONObject("bearer");
            Config.setBearerKey(jbearer.getString("token"));
            Intent intent = new Intent(this, WorkActivity.class);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
            alertDialog(getString(R.string.Error), e.getMessage());
        }
    }
}
