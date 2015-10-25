package com.alan.QuickCall;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

/**
 * The configuration screen for the {@link Call Call} AppWidget.
 */
public class CallConfigureActivity extends Activity {
    private final String IMAGE_TYPE = "image/*";
    private final int IMAGE_CODE = 0;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText et_name, et_num;
    private static final String PREFS_NAME = "com.alan.QuickCall.Call";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    private String path;
    private Bitmap bm = null;

    public CallConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.call_configure);
        et_name = (EditText) findViewById(R.id.et_name);
        et_num = (EditText) findViewById(R.id.et_num);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);
        findViewById(R.id.imageButton).setOnClickListener(mOnClickListener);
        ((ImageButton)findViewById(R.id.imageButton)).setImageResource(R.drawable.example_2);
        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        et_name.setText(loadTitlePref(CallConfigureActivity.this, mAppWidgetId));
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.add_button:
                    final Context context = CallConfigureActivity.this;

                    // When the button is clicked, store the string locally
                    saveNamePref(context, mAppWidgetId, et_name.getText().toString());
                    savePhonePref(context, mAppWidgetId, et_num.getText().toString());
                    saveImageButtonPref(context, mAppWidgetId, path);
                    // It is the responsibility of the configuration activity to update the app widget
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    Call.updateAppWidget(context, appWidgetManager, mAppWidgetId);

                    // Make sure we pass back the original appWidgetId
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    setResult(RESULT_OK, resultValue);
                    finish();
                    break;
                case R.id.imageButton:
                    Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
                    getAlbum.setType(IMAGE_TYPE);
                    startActivityForResult(getAlbum, IMAGE_CODE);
            }
        }
    };

    // Write the prefix to the SharedPreferences object for this widget
    static void saveNamePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
//        prefs.putString(PREF_PREFIX_KEY +"_phone_"+ appWidgetId,text);
        prefs.commit();
    }
    static void savePhonePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY +"_phone_"+ appWidgetId,text);
        prefs.commit();
    }
    static void saveImageButtonPref(Context context, int appWidgetId, String path) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + "_path_"+appWidgetId, path);
        prefs.commit();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode != RESULT_OK) { //此处的 RESULT_OK 是系统自定义得一个常量
            // Log.e(TAG,"ActivityResult resultCode error");
            return;
        }
        ContentResolver resolver = getContentResolver();
        if (requestCode == IMAGE_CODE) {
            try {
                Uri originalUri = data.getData(); //获得图片的uri
                bm = MediaStore.Images.Media.getBitmap(resolver, originalUri); //显得到bitmap图片
                // 这里开始的第二部分，获取图片的路径：
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                //按我个人理解 这个是获得用户选择的图片的索引值
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                //最后根据索引值获取图片路径
                path = cursor.getString(column_index);
                Log.e("Lostinai", path);
            }catch (Exception e) {
                Log.e("Lostinai", e.toString());
            }finally {
                ((ImageButton)findViewById(R.id.imageButton)).setImageBitmap(bm);
            }

        }
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }
    static String loadImageButtonPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String pathValue = prefs.getString(PREF_PREFIX_KEY + "_path_"+appWidgetId, null);
        return pathValue;
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.commit();
    }

    public static String loadPhoneNumber(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String phoneNumber = prefs.getString(PREF_PREFIX_KEY + "_phone_"+appWidgetId, null);
        return phoneNumber;

    }
}


