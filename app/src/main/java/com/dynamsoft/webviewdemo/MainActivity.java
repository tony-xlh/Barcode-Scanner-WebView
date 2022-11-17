package com.dynamsoft.webviewdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebViewAssetLoader;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  {
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    private WebView webView;
    private TextView textView;
    private Boolean pageFinished = false;
    private Context ctx;
    final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;
        webView = findViewById(R.id.webView);
        textView = findViewById(R.id.resultTextView);

        if (hasCameraPermission() == false) {
            requestPermission();
        }

        loadWebViewSettings();

        webView.loadUrl("https://appassets.androidplatform.net/assets/scanner.html");
        //webView.loadUrl("file:android_asset/scanner.html");

        Button scanBarcodesButton = findViewById(R.id.scanBarcodesButton);
        scanBarcodesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pageFinished) {
                    webView.evaluateJavascript("javascript:isCameraOpened()", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Log.d("DBR","camera opened?: "+value);
                            if (value.endsWith("\"yes\"")) {
                                Log.d("DBR","resume scan");
                                resumeScan();
                            }else{
                                Log.d("DBR","start scan");
                                startScan();
                            }
                        }
                    });

                    webView.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(ctx,"The web page has not been loaded.",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void startScan(){
        webView.evaluateJavascript("javascript:startScan()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
            }
        });
    }

    private void pauseScan(){
        webView.evaluateJavascript("javascript:pauseScan()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
            }
        });
    }

    private void resumeScan(){
        webView.evaluateJavascript("javascript:resumeScan()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
            }
        });
    }

    private void stopScan(){
        webView.evaluateJavascript("javascript:stopScan()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
            }
        });
    }


    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
    }

    private void loadWebViewSettings(){
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        // Enable remote debugging via chrome://inspect
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                pageFinished = true;
            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                           SslError error) {
                handler.proceed();
            }
            @Override
            @RequiresApi(21)
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            @SuppressWarnings("deprecation") // for API < 21
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return assetLoader.shouldInterceptRequest(Uri.parse(url));
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        request.grant(request.getResources());
                    }
                });
            }
        });
        webView.addJavascriptInterface(new JSInterface(new ScanHandler (){
            @Override
            public void onScanned(String result){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.setVisibility(View.INVISIBLE);
                        textView.setText(result);
                    }
                });
            }
        }), "AndroidFunction");
    }

    @Override
    public void onBackPressed() {
        pauseScan();
        webView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView.getVisibility() == View.VISIBLE) {
          startScan();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }
}