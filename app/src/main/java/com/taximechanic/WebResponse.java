package com.taximechanic;

public interface WebResponse {
    int mResponseAuthNick = 1;
    int mResponseAuthPhone = 2;
    int mResponseDriverProfileUpdate = 3;
    int mResponseGeocoder = 4;
    int mResponseDriverOn = 5;
    int mResponseDriverOff = 6;
    int mResponseAuthMechanic = 7;
    int mResponseMechanicUpdate = 8;
    int getmResponseMechanicSaveReport = 9;
    int getQuestions = 10;

    void webResponse(int code, int webResponse, String s);
}
