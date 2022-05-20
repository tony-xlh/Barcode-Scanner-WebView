package com.dynamsoft.webviewdemo;
public interface ScanCallback {
    public void onScanned(String result);
}

class ScanHandler implements ScanCallback {
    @Override
    public void onScanned(String result) {

    }
}