package com.example.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Patterns;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserRedirectService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();

        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
            {
                AccessibilityNodeInfo parentNodeInfo = event.getSource();
                if (parentNodeInfo == null) {
                    return;
                }
                String packageName = event.getPackageName().toString();
                SupportedBrowserConfig browserConfig = null;
                for (SupportedBrowserConfig supportedConfig : getSupportedBrowsers()) {
                    if (supportedConfig.packageName.equals(packageName)) {
                        browserConfig = supportedConfig;
                    }
                }
                //this is not a supported browser, so exit
                if (browserConfig == null) {
                    return;
                }
                Log.d(TAG, "Browser: " + browserConfig.packageName.split("\\.").getClass());


                String capturedUrl = captureUrl(parentNodeInfo, browserConfig);

                if (capturedUrl == null || !capturedUrl.contains(URL_GOOGLE_SEARCH)) {
                    return;
                }

                String query = extractQuery(capturedUrl);
                if (query != null) {
                    String redirectUrl = URL_ZAREBIN_SEARCH + " " + query;
                    Intent intent = createRedirectIntent(redirectUrl, browserConfig);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
            break;
        }
    }

    private Intent createRedirectIntent(String url, SupportedBrowserConfig browserConfig) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setPackage(browserConfig.packageName);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, browserConfig.packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @Nullable
    private String extractQuery(String url) {
        try {
            Pattern pattern = Pattern.compile("(?<=q=).*?(?=$|&)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group();
            } else {
                return null;
            }
        } catch (IllegalStateException exception) {
            Log.e(TAG, "Pattern failed to match: " + exception.getMessage());
            return null;
        }
    }

    /**
     * Holds predefined supported browsers configs
     */
    private static class SupportedBrowserConfig {
        public String packageName, addressBarId;

        public SupportedBrowserConfig(String packageName, String addressBarId) {
            this.packageName = packageName;
            this.addressBarId = addressBarId;
        }
    }

    @NonNull
    private static List<SupportedBrowserConfig> getSupportedBrowsers() {
        List<SupportedBrowserConfig> browsers = new ArrayList<>();
        // The purpose of the this test project is to run the project on commonly used browsers, feel free to try other browsers, but they are not tested!
        browsers.add(new SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar"));
        browsers.add(new SupportedBrowserConfig("org.mozilla.firefox", "org.mozilla.firefox:id/mozac_browser_toolbar_url_view,url_bar_title"));
//        browsers.add(new SupportedBrowserConfig("com.opera.browser", "com.opera.browser:id/url_field"));
//        browsers.add(new SupportedBrowserConfig("com.opera.mini.native", "com.opera.mini.native:id/url_field"));
//        browsers.add(new SupportedBrowserConfig("com.duckduckgo.mobile.android", "com.duckduckgo.mobile.android:id/omnibarTextInput"));
//        browsers.add(new SupportedBrowserConfig("com.microsoft.emmx", "com.microsoft.emmx:id/url_bar"));
        browsers.add(new SupportedBrowserConfig("com.brave.browser", "com.brave.browser:id/url_bar"));
        return browsers;
    }

    private String captureUrl(AccessibilityNodeInfo info, SupportedBrowserConfig config) {
        AccessibilityNodeInfo addressBarNodeInfo = getAddressBarNode(info, config);

        String url = null;
        if (addressBarNodeInfo != null && addressBarNodeInfo.getText() != null) {
            url = addressBarNodeInfo.getText().toString();
        }

        return url;
    }

    private AccessibilityNodeInfo getAddressBarNode(AccessibilityNodeInfo info, SupportedBrowserConfig config) {
        List<AccessibilityNodeInfo> nodes = info.findAccessibilityNodeInfosByViewId(config.addressBarId);
        if (nodes == null || nodes.size() <= 0) {
            return null;
        }
        return nodes.get(0);
    }


    @Override
    public void onInterrupt() {
        // Do nothing
    }

    @Override
    public void onServiceConnected() {
        // Do nothing
    }

    private static final String TAG = "BrowserRedirectService";
    private static final String URL_ZAREBIN_SEARCH = "https://zarebin.ir/search?q=";
    private static final String URL_GOOGLE_SEARCH = "google.com/search?";
}
