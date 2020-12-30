package com.taximechanic;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ProfileActivity extends BaseActivity  {

    private EditText edDriverNick;
    private EditText edDriverName;
    private EditText edDriverLastName;
    private EditText edDriverPhone;
    private EditText edDriverEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        edDriverNick = findViewById(R.id.edDriverNick);
        edDriverName = findViewById(R.id.edDriverName);
        edDriverPhone = findViewById(R.id.edPhoneNumber);
        edDriverEmail = findViewById(R.id.edEmail);
        edDriverLastName = findViewById(R.id.edDriverLastName);
        edDriverNick.setText(Config.getString("mech_nick"));
        edDriverName.setText(Config.getString("mech_fname"));
        edDriverLastName.setText(Config.getString("mech_lname"));
        edDriverPhone.setText(Config.getString("mech_phone"));
        edDriverEmail.setText(Config.getString("mech_email"));
        findViewById(R.id.btnSave).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btnSave:
                WebQuery wq = new WebQuery(WebQuery.mHostUrlMechanicUpdate, WebQuery.mMethodPut, WebResponse.mResponseMechanicUpdate);
                wq.mWebResponse = this;
                wq.setHeader("Authorization", "Bearer " + Config.mBearerKey);
                wq.setParameter("name", edDriverName.getText().toString());
                wq.setParameter("surname", edDriverLastName.getText().toString());
                wq.setParameter("nickname", edDriverNick.getText().toString());
                wq.setParameter("email", edDriverEmail.getText().toString());
                wq.setParameter("phone", edDriverPhone.getText().toString());
                wq.request();
                break;
        }
    }

    @Override
    public void webResponse(int code, int webResponseCode, String s) {
        super.webResponse(code, webResponseCode, s);
        if (webResponseCode == 200) {
            Config.setString("mech_nick", edDriverNick.getText().toString());
            Config.setString("mech_fname", edDriverName.getText().toString());
            Config.setString("mech_lname", edDriverLastName.getText().toString());
            Config.setString("mech_email", edDriverEmail.getText().toString());
            Config.setString("mech_phone", edDriverPhone.getText().toString());
            alertDialog(R.string.Empty, R.string.Saved);
        } else {
            alertDialog(getString(R.string.Empty), getString(R.string.CouldNotSaved) + "\r\n" + s);
        }
    }
}
