package com.google.example.gcmnetworkmanagerquickstart;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class WifiTaskWorker extends Worker {

    private static final String TAG = "WifiTaskWorker";
    private OkHttpClient mClient = new OkHttpClient();

    public WifiTaskWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    @NonNull
    public Result doWork() {
        // Do the work here
        Log.i("Caren", "Doing wifi task");
        String url = "https://abc.xyz/";
        return fetchUrl(mClient, url);
    }

    private Result fetchUrl(OkHttpClient client, String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            Log.d(TAG, "fetchUrl:response:" + response.body().string());

            if (response.code() != 200) {
                return Result.failure();
            }
        } catch (IOException e) {
            Log.e(TAG, "fetchUrl:error" + e.toString());
            return Result.failure();
        }

        return Result.success();
    }
}
