package com.dynamsoft.webviewdemo;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class JSInterface {
    Activity activity;

    JSInterface(Activity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void returnResult(String result) {
        Log.d("DBR","js: "+result);
        if (this.activity instanceof JSActivity) {
            JSActivity.closeWithResult(result);
        }
    }

}
