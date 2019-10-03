package com.google.example.gcmnetworkmanagerquickstart;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class ChargingTaskWorker extends Worker {
    private static final String TAG = "ChargingTaskWorker";
    private OkHttpClient mClient = new OkHttpClient();

    public ChargingTaskWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    @NonNull
    public Result doWork() {
        // Do the work here
        Log.i("Caren", "Doing charging task");
        String url = "http://www.nasa.gov/";
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
