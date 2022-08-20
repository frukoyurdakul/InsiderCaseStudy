package com.insider.webviewstar;

import android.webkit.WebView;

import androidx.core.view.ViewCompat;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

class WebViewFunctionManager {

    private final Queue<Runnable> afterInitializeQueue = new LinkedBlockingQueue<>(10);
    private final WebView webView;

    private boolean isInitialized = false;

    WebViewFunctionManager(WebView webView) {
        this.webView = webView;
    }

    void initialize(String javascript) {
        runTask(() -> {
            webView.evaluateJavascript(javascript, null);
            isInitialized = true;
            Runnable task;
            while ((task = afterInitializeQueue.poll()) != null) {
                task.run();
            }
        });
    }

    void call(String functionName) {
        runAfterInitialize(() -> webView.evaluateJavascript(functionName + "();", null));
    }

    void call(String functionName, String argument) {
        runAfterInitialize(() -> webView.evaluateJavascript(functionName + "('" + argument + "');", null));
    }

    private void runAfterInitialize(Runnable runnable) {
        if (isInitialized)
            runTask(runnable);
        else
            afterInitializeQueue.offer(runnable);
    }

    private void runTask(Runnable runnable) {
        webView.post(() -> {
            if (ViewCompat.isAttachedToWindow(webView))
                runnable.run();
        });
    }
}