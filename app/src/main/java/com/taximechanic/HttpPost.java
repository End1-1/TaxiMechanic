package com.taximechanic;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpPost extends AsyncTask<Void, Void, Integer> {

    public WebResponse mListener;
    public int mWebResponseCode;
    public String mResponseText;
    public int mRequestCode;
    private URL mUrl;
    private Map<String, String> mParameters = new LinkedHashMap<>();
    private Map<String, String> mHeaders = new LinkedHashMap<>();
    private Map<String, List<String>> mFiles = new LinkedHashMap<>();

    public HttpPost(String url, int requestCode) {
        mRequestCode = requestCode;
        try {
            mUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void setParameter(String key, String value) {
        mParameters.put(key, value);
    }

    public void setHeader(String key, String value) {
        mHeaders.put(key, value);
    }

    public void setFile(String key, String value) {
        if (!mFiles.containsKey(key)) {
            mFiles.put(key, new LinkedList<String>());
        }
        mFiles.get(key).add(value);
    }

    public void post() {
        execute();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        String boundary = "jdjd77d749aqlpo4ksasdvoi947871d--";
        try {
            HttpsURLConnection conn = (HttpsURLConnection) mUrl.openConnection();
            conn.setConnectTimeout(4000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Accept", "application/json");
            for (Map.Entry<String, String> e: mHeaders.entrySet()) {
                conn.setRequestProperty(e.getKey(), e.getValue());
            }
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            for (Map.Entry<String, String> e: mParameters.entrySet()) {
                os.writeBytes("--" + boundary + "\r\n");
                os.writeBytes("Content-Disposition: form-data; name=\"" + e.getKey() + "\r\n");
                os.writeBytes("\r\n");
                os.writeBytes(e.getValue());
                os.writeBytes("\r\n");
                os.writeBytes("--" + boundary + "--" + "\r\n");
            }
            for (Map.Entry<String, List<String>> e: mFiles.entrySet()) {
                for (String fn: e.getValue()) {
                    String[] fileName = fn.split("/");
                    String formFileName = fileName[fileName.length - 1];
                    os.writeBytes("--" + boundary + "\r\n");
                    os.writeBytes("Content-Disposition: form-data; name=\"" + e.getKey() + "\"; filename=\"" + formFileName + "\"" + "\r\n");
                    //os.writeBytes("Content-Type: image/jpeg" + "\r\n");
                    os.writeBytes("Content-Transfer-Encoding: binary" + "\r\n");
                    os.writeBytes("\r\n");
                    File file = new File(fn);
                    FileInputStream fileInputStream = new FileInputStream(file);
                    int bytesAvailable = fileInputStream.available();
                    int bufferSize = Math.min(bytesAvailable, 1048576);
                    byte[] buffer = new byte[bufferSize];
                    int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        os.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, 1048576);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                    os.writeBytes("\r\n");
                    os.writeBytes("--" + boundary + "--" + "\r\n");
                }
            }
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
            mResponseText = sb.toString();
            Log.d("HTTP POST", String.format("%d -> %s", mRequestCode, mResponseText));
        } catch (IOException e) {
            e.printStackTrace();
            mWebResponseCode = 0;
            mResponseText = e.getMessage();
        }
        return mWebResponseCode;
    }

    @Override
    protected void onPostExecute(Integer aVoid) {
        super.onPostExecute(aVoid);
        if (mListener != null) {
            mListener.webResponse(mRequestCode, aVoid, mResponseText);
        }
    }
}
