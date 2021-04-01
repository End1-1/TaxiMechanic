package com.taximechanic;

import android.os.Handler;
import android.os.Looper;

import org.conscrypt.Conscrypt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.ByteString;

import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

public class WebQuery {

    public enum  HttpMethod {
        GET,
        POST,
        PUT
    }

    public static final String mHostUrl = "https://newyellowtaxi.com";
    public static final String mHostUrlAuthNick = mHostUrl + "/api/driver/auth";
    public static final String mHostUrlDriverReady = mHostUrl + "/api/driver/order-ready";
    public static final String mHostUrlAuthPhone = mHostUrl + "/api/driver/auth/phone";
    public static final String mHostUrlDriverUpdateProfile = mHostUrl + "/api/driver/profile/update/3";
    public static final String mHostUrlMechanicAuth = mHostUrl + "/api/worker/auth";
    public static final String mHostUrlMechanicQuestions = mHostUrl + "/api/worker/questions";
    public static final String mHostUrlMechanicUpdate = mHostUrl + "/api/worker/worker-info";
    public static final String mHostUrlMechanicReport = mHostUrl + "/api/worker/report";
    //public static String mGeocoderApiKey = "45b3aaf3-6d70-459d-980e-30269585db64"; //real
    public static String mGeocoderApiKey = "101f3900-d4f7-45fc-8796-62e0f5db942f"; //fake
    // mSiteKey stored in oauth_clients, NewYellowTaxi Password Grant Client
    public static final String mSiteKey = "fhMTzPCAQJT2pHXAyuMq1UltFWfJ9AtbeWcNPbdc";


    private String mUrl;
    private HttpMethod mMethod;
    private int mResponseCode;
    private WebResponse mWebResponse;
    private Map<String, String> mHeader;
    private Map<String, String> mParameters;
    private String mData;
    private int mWebResponseCode;
    private String mOutputData;
    private Map<String, List<String>> mFiles;

    public WebQuery(String url, HttpMethod method, int responseCode, WebResponse r) {
        mData = "";
        mOutputData = "";
        mWebResponseCode = 0;
        mHeader = new HashMap<>();
        mParameters = new HashMap<>();
        mFiles = new LinkedHashMap<>();
        mUrl = url;
        mMethod = method;
        mResponseCode = responseCode;
        mWebResponse = r;

        setHeader("Authorization", "Bearer " + Config.mBearerKey);
        setHeader("Accept", "application/json");
    }

    public WebQuery setHeader(String key, String value) {
        mHeader.put(key, value);
        return this;
    }

    public WebQuery setParameter(String key, String value) {
        mParameters.put(key, value);
        return this;
    }

    public WebQuery setFile(String key, String value) {
        if (!mFiles.containsKey(key)) {
            mFiles.put(key, new LinkedList<>());
        }
        mFiles.get(key).add(value);
        return this;
    }

    private RequestBody getBody() {
        String boundary = "jdjd77d749aqlpo4ksasdvoi947871d--";

        MultipartBody.Builder form = new MultipartBody.Builder();
        form.setType(MultipartBody.FORM);
        for (Map.Entry<String, String> e: mParameters.entrySet()) {
            form.addFormDataPart(e.getKey(), e.getValue());
        }
       for (Map.Entry<String, List<String>> e: mFiles.entrySet()) {
            for (String fn: e.getValue()) {
                String[] fileName = fn.split("/");
                String formFileName = fileName[fileName.length - 1];
                form.addFormDataPart(e.getKey(), formFileName, RequestBody.create(MediaType.parse("image/jpeg"), new File(fn)));
            }
        }
        RequestBody rb = form.build();
        return rb;
    }

    public void request() {
        Security.insertProviderAt(Conscrypt.newProvider(), 1);
        OkHttpClient httpClient = getUnsafeOkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url(mUrl);
        for (Map.Entry<String, String> e: mHeader.entrySet()) {
            builder.addHeader(e.getKey(), e.getValue());
        }
        switch (mMethod) {
            case GET:
                builder.get();
                break;
            case POST:
                builder.post(getBody());
                break;
        }
        Thread thread = new Thread(() -> {
            try {
                System.out.println(mUrl);
//                if (mMethod == HttpMethod.POST) {
//                    final Buffer buffer = new Buffer();
//                    getBody().writeTo(buffer);
//                    System.out.println(buffer.readUtf8());
//                }
                Response response = httpClient.newCall(builder.build()).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + Integer.toString(response.code()) + " " + response.body().string());
                }
                mWebResponseCode = 200;
                mOutputData = response.body().string();
                System.out.println(mOutputData);
            }
            catch (Exception e) {
                mOutputData = e.getMessage();
                mWebResponseCode = 500;
                System.out.println(mResponseCode);
                e.printStackTrace();
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (mWebResponse != null) {
                        mWebResponse.webResponse(mResponseCode, mWebResponseCode, mOutputData);
                    }
                }
            });
        });
        thread.start();
    }

    public static WebQuery create(String url, HttpMethod method, int responseCode, WebResponse r) {
        return new WebQuery(url, method, responseCode, r);
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance(SSL);
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
