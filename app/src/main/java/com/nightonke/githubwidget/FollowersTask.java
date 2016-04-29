package com.nightonke.githubwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Weiping on 2016/4/26.
 */

public class FollowersTask extends AsyncTask<String, Void, String> {

    private Widget widget;
    private RemoteViews remoteViews;
    private Context context;
    private ComponentName componentName;
    private int appWidgetId;
    private int bitmapWidth = 0;
    private int bitmapHeight = 0;

    public FollowersTask(
            Widget widget,
            RemoteViews remoteViews,
            Context context,
            ComponentName componentName,
            int appWidgetId,
            int bitmapWidth,
            int bitmapHeight) {
        this.widget = widget;
        this.remoteViews = remoteViews;
        this.context = context;
        this.componentName = componentName;
        this.appWidgetId = appWidgetId;
        this.bitmapWidth = bitmapWidth;
        this.bitmapHeight = bitmapHeight;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        if (BuildConfig.DEBUG) Log.d("GithubWidget", "Execute FollowersTask");
        // check whether the user id is got
        String userName = SettingsManager.getUserName();
        if (userName == null) {
            // user didn't set the user name
            // we just use the default picture
            return null;
        } else {
            int userId = SettingsManager.getUserId();
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            if (userId == -1) {
                // we haven't got the user id
                try {
                    url = new URL("https://api.github.com/users/" + userName);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.connect();
                    if(httpURLConnection.getResponseCode() == 200){
                        InputStream in = httpURLConnection.getInputStream();
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while((len = in.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, len);
                        }
                        if (BuildConfig.DEBUG) Log.d("GithubWidget", "Write user basic data: "
                                + byteArrayOutputStream.toString());
                        Util.writeUserBasicData(context, byteArrayOutputStream.toString());
                        return byteArrayOutputStream.toString();
                    } else {
                        return null;
                    }
                } catch (IOException i) {
                    i.printStackTrace();
                } finally{
                    if (httpURLConnection != null) httpURLConnection.disconnect();
                }
            } else {
                try {
                    String urlString = "https://api.github.com/users/" + userName;
                    if (BuildConfig.DEBUG)
                        Log.d("GithubWidget", "Get user followers: " + urlString);
                    url = new URL(urlString);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.connect();
                    if(httpURLConnection.getResponseCode() == 200){
                        InputStream in = httpURLConnection.getInputStream();
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while((len = in.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, len);
                        }
                        return byteArrayOutputStream.toString();
                    } else {
                        return null;
                    }
                } catch (IOException i) {
                    i.printStackTrace();
                } finally{
                    if (httpURLConnection != null) httpURLConnection.disconnect();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (SettingsManager.getUserName() == null) {
            switch (widget) {
                case WIDGET_2:
                case WIDGET_3:
                case WIDGET_4:
                case WIDGET_5:
                    remoteViews.setImageViewBitmap(R.id.motto,
                            Util.getInputUserNameBitmap(
                                    context, SettingsManager.getBaseColor()));
                    break;
            }
        } else {
            if (result == null) {
                if (BuildConfig.DEBUG) Log.d("GithubWidget", "Get user followers failed " + result);
            } else {
                if (BuildConfig.DEBUG)
                    Log.d("GithubWidget", "Get user followers successfully");
                if (BuildConfig.DEBUG) Log.d("GithubWidget", "Write followers: " + result);
                String followers = Util.writeFollowers(result);
                int baseColor = SettingsManager.getBaseColor();
                switch (widget) {
                    case WIDGET_2:
                    case WIDGET_3:
                    case WIDGET_4:
                    case WIDGET_5:
                        remoteViews.setImageViewBitmap(R.id.followers,
                                Util.getFollowersWithLetterBitmap(context, baseColor,
                                        followers, bitmapWidth, bitmapHeight));
                        break;
                }
            }
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetId == -1) {
            appWidgetManager.updateAppWidget(componentName, remoteViews);
        } else {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

}
