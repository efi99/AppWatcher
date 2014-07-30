package com.anod.appwatcher.backup;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.util.JsonWriter;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.utils.AppLog;
import com.anod.appwatcher.utils.BitmapUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by alex on 7/9/14.
 */
public class AppListWriter {

    /**
     * Write list in JSON format
     * @param file
     * @param listCursor
     * @throws java.io.IOException
     */
    public void writeJSON(Writer file, AppListCursor listCursor)
            throws IOException {
        JsonWriter writer = new JsonWriter(file);
        writer.beginArray();
        listCursor.moveToPosition(-1);
        while (listCursor.moveToNext()) {
            writeApp(writer, listCursor.getAppInfo());
        }
        writer.endArray();
        writer.close();
    }

    /**
     * Write one app into json
     * @param writer
     * @param appInfo
     * @throws IOException
     */
    public void writeApp(JsonWriter writer, AppInfo appInfo)
            throws IOException {
        writer.beginObject();
        writer.name("id").value(appInfo.getAppId());
        writer.name("packageName").value(appInfo.getPackageName());
        writer.name("title").value(appInfo.getTitle());
        writer.name("creator").value(appInfo.getCreator());
        writer.name("updateTime").value(appInfo.getUpdateTime());
        writer.name("versionName").value(appInfo.getVersionName());
        writer.name("versionCode").value(appInfo.getVersionCode());
        writer.name("status").value(appInfo.getStatus());
        Bitmap icon = appInfo.getIcon();
        if (icon != null) {
            byte[] iconData = BitmapUtils.flattenBitmap(icon);
            writer.name("icon").beginArray();
            for(int i=0; i<iconData.length; i++) {
                writer.value(iconData[i]);
            }
            writer.endArray();
        } else {
            writer.name("icon").beginArray().endArray();
        }
        writer.endObject();
    }
}