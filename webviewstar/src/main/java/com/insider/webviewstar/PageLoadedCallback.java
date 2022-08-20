package com.insider.webviewstar;

/**
 * The interface that is used for when the page is successfully loaded
 * to the WebView.
 *
 * No failure method was defined in this interface, as the success
 * side is the only side we're interested in right now.
 */
interface PageLoadedCallback {

    /**
     * Triggered when WebView correctly loads the URL associated with it.
     */
    void onPageLoaded();
}