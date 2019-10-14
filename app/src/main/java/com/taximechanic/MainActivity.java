package com.taximechanic;


import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

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
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tvLogin:
                if (!createProgressDialog(R.string.Empty, R.string.Login)) {
                    return;
                }
                WebQuery wq = new WebQuery(WebQuery.mHostUrlMechanicAuth, WebQuery.mMethodPost, WebResponse.mResponseAuthMechanic);
                wq.mWebResponse = this;
                wq.setParameter("username", etUsername.getText().toString());
                wq.setParameter("password", etPassword.getText().toString());
                wq.setParameter("client_id", "5");
                wq.setParameter("client_secret", WebQuery.mSiteKey);
                wq.setParameter("guard", "system_workers_api");
                wq.request();
        }
    }

    @Override
    public void webResponse(int code, int webResponse, String s) {
        super.webResponse(code, webResponse, s);
    }
}
