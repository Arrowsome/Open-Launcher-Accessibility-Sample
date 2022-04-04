package com.example.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.RemoteViews;

import com.benny.openlauncher.R;

public class AccessibilityStatusWidgetProvider extends AppWidgetProvider {
    private AccessibilityContentObserver mContentObserver;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (mContentObserver == null) {
            mContentObserver = new AccessibilityContentObserver(
                    new Handler(Looper.myLooper())
            );
            context.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES),
                    true,
                    mContentObserver
            );
        }

        for (int widgetId : appWidgetIds) {
            mContentObserver.removeListener(widgetId);

            RemoteViews remoteViews = new RemoteViews(
                    context.getPackageName(),
                    R.layout.widget_accessibility_setting
            );
            remoteViews.setOnClickPendingIntent(
                    R.id.btn_accessibility_status,
                    createAccessibilitySettingPendingIntent(context)
            );
            remoteViews.setTextViewText(
                    R.id.btn_accessibility_status,
                    getStatusLabel(context)
            );

            mContentObserver.addListener(widgetId, () -> {
                remoteViews.setTextViewText(
                        R.id.btn_accessibility_status,
                        getStatusLabel(context)
                );
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            });

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

    }

    @Override
    public void onDisabled(Context context) {
        if (mContentObserver == null) return;
        context.getContentResolver().unregisterContentObserver(mContentObserver);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        if (mContentObserver == null) return;
        for (int widgetId : appWidgetIds) {
            mContentObserver.removeListener(widgetId);
        }
    }

    private PendingIntent createAccessibilitySettingPendingIntent(Context context) {
        return PendingIntent.getActivity(
                context,
                0,
                new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                0
        );
    }

    private String getStatusLabel(Context context) {
        int resourceId = isBrowserAccessibilityServiceOn(context) ?
                R.string.accessibility_status_on :
                R.string.accessibility_status_off;
        return context.getString(resourceId);
    }

    private boolean isBrowserAccessibilityServiceOn(Context context) {
        try {
            int isAccessibilityOn = Settings.Secure.getInt(
                    context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED
            );

            if (isAccessibilityOn != 1) return false;

            String enabledServices = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );

            return enabledServices.contains(KEY_BROWSER_REDIRECT_SERVICE);
        } catch (Settings.SettingNotFoundException exception) {
            return false;
        }


    }

    private static final String KEY_BROWSER_REDIRECT_SERVICE = "BrowserRedirectService";
    private static final String TAG = "AccessStatusWidgetProv";
}
