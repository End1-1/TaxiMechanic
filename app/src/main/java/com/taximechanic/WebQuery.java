package com.taximechanic;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class WebQuery extends AsyncTask<Void, Void, Integer> {

    //public static final String mHostUrl = "http://192.168.0.203";
    public static final String mHostUrl = "http://195.191.155.164:29999";
    //public static final String mHostUrl = "http://10.1.0.2";
    public static final String mHostUrlAuthNick = mHostUrl + "/api/driver/auth";
    public static final String mHostUrlDriverReady = mHostUrl + "/api/driver/order-ready";
    public static final String mHostUrlAuthPhone = mHostUrl + "/api/driver/auth/phone";
    public static final String mHostUrlDriverUpdateProfile = mHostUrl + "/api/driver/profile/update/3";
    public static final String mHostUrlMechanicAuth = mHostUrl + "/api/mechanic/login";
    public static final String mHostUrlMechanicUpdate = mHostUrl + "/api/mechanic/mechanic-info";
    public static final String mHostUrlMechanicReport = mHostUrl + "/api/mechanic/report";
    //public static String mGeocoderApiKey = "45b3aaf3-6d70-459d-980e-30269585db64"; //real
    public static String mGeocoderApiKey = "101f3900-d4f7-45fc-8796-62e0f5db942f"; //fake
    // mSiteKey stored in oauth_clients, NewYellowTaxi Password Grant Client
    public static final String mSiteKey = "8saiL6GO2BBdKWpELXcL9yXGodBpZliDFiLSHTwk";

    public WebResponse mWebResponse = null;

    public static final String mMethodGet = "GET";
    public static final String mMethodPost = "POST";
    public static final String mMethodPut = "PUT";

    private String mUrl;
    private String mMethod;
    private String mResult;
    private int mResultCode;
    private int mWebResponseCode;
    private Map<String, String> mParameters = new LinkedHashMap<>();
    private Map<String, String> mHeaders = new LinkedHashMap<>();

    public WebQuery(String url, String method, int resultCode) {
        mUrl = url;
        mMethod = method;
        mResultCode = resultCode;
    }

    public void setParameter(String key, String value) {
        mParameters.put(key, value);
    }

    public void setHeader(String key, String value) {
        mHeaders.put(key, value);
    }

    public void request() {
        execute();
    }

    private void requestGET() {
        try {
            String urlParameters = new String();
            for (Map.Entry<String, String> e: mParameters.entrySet()) {
                if (!urlParameters.isEmpty()) {
                    urlParameters += "&";
                }
                urlParameters += e.getKey() + "=" + e.getValue();
            }
            if (urlParameters.length() > 0) {
                mUrl += "?" + urlParameters;
            }
            URL url = new URL(mUrl);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            mWebResponseCode = con.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String input;
            mResult = new String();
            while ((input = br.readLine()) != null){
                mResult += input;
            }
            br.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            mResult = e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            mResult = e.getMessage();
        }
    }

    private void requestPost() {
        String urlParameters = new String();
        for (Map.Entry<String, String> e: mParameters.entrySet()) {
            if (!urlParameters.isEmpty()) {
                urlParameters += "&";
            }
            urlParameters += e.getKey() + "=" + e.getValue();
        }
        try {
            URL url = new URL(mUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(4000);
            conn.setRequestMethod(mMethod);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            for (Map.Entry<String, String> e: mHeaders.entrySet()) {
                conn.setRequestProperty(e.getKey(), e.getValue());
            }
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes(StandardCharsets.UTF_8).length));
            OutputStream out = conn.getOutputStream();
            out.write(urlParameters.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
            mWebResponseCode = conn.getResponseCode();
            InputStream is = null;
            if (mWebResponseCode < 400) {
                is =  conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            mResult = sb.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            mResult = e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            mResult = e.getMessage();
        }
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        if (mMethod.equals(mMethodGet)) {
            requestGET();
        } else if (mMethod.equals(mMethodPost) || mMethod.equals(mMethodPut)) {
            requestPost();
        }
        return mResultCode;
    }

    @Override
    protected void onPostExecute(Integer aVoid) {
        super.onPostExecute(aVoid);
        if (mWebResponse != null) {
            mWebResponse.webResponse(aVoid, mWebResponseCode, mResult);
        }
    }
}
