package com.taximechanic;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

public class BaseActivity extends AppCompatActivity implements WebResponse,
        View.OnClickListener {

    private ProgressDialog pd;

    public BaseActivity() {
        super();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide();
    }

    protected boolean createProgressDialog(int title, int text) {
        if (pd != null) {
            hideProgressDialog();
        }
        if (pd == null) {
            pd = new ProgressDialog(this);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setTitle(getString(title));
            pd.setMessage(getString(text));
            pd.setIndeterminate(true);
            pd.setCanceledOnTouchOutside(false);
            pd.show();
            return true;
        }
        return false;
    }

    protected void hideProgressDialog() {
        if (pd != null) {
            pd.dismiss();
            pd = null;
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void webResponse(int code, int webResponse, String s) {
        hideProgressDialog();
    }
}
