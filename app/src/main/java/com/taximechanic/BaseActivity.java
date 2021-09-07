package com.taximechanic;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

    public AlertDialog alertDialog(int title, int message) {
        String strTitle = "";
        if (title > 0) {
            strTitle = getString(title);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(message))
                .setTitle(strTitle);
        builder.setCancelable(false);
        builder.setNeutralButton(R.string.OK, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public AlertDialog alertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(title);
        builder.setCancelable(false);
        builder.setNeutralButton(R.string.OK, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void webResponse(int code, int webResponse, String s) {
        hideProgressDialog();
    }
}
