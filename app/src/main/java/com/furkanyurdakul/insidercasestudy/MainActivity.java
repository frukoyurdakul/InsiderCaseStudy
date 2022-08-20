package com.furkanyurdakul.insidercasestudy;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.insider.webviewstar.WebViewStarSDK;

public class MainActivity extends AppCompatActivity {

    private WebViewStarSDK sdk;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sdk = WebViewStarSDK.createInstance(findViewById(R.id.webView));

        findViewById(R.id.addBigStarButton).setOnClickListener(v -> sdk.addBigStar());
        findViewById(R.id.addSmallStarButton).setOnClickListener(v -> sdk.addSmallStar());
        findViewById(R.id.resetButton).setOnClickListener(v -> sdk.reset());
    }
}