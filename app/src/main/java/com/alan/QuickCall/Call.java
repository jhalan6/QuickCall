package com.alan.QuickCall;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link CallConfigureActivity CallConfigureActivity}
 */
public class Call extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            CallConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.call);
        try{

            Intent intenta=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+CallConfigureActivity.loadPhoneNumber(context,appWidgetId)));
            PendingIntent pendingIntent=PendingIntent.getActivity(context, 0, intenta, 0);
            views.setOnClickPendingIntent(R.id.btn_call,pendingIntent);
            views.setImageViewUri(R.id.imageView, Uri.parse(CallConfigureActivity.loadImageButtonPref(context, appWidgetId)));
        }catch (Exception e){
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

