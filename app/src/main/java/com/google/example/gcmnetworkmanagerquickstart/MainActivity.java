/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.example.gcmnetworkmanagerquickstart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int RC_PLAY_SERVICES = 123;

    public static final String TASK_TAG_PERIODIC = "periodic_task";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_start_wifi_task).setOnClickListener(this);
        findViewById(R.id.button_start_charging_task).setOnClickListener(this);
        findViewById(R.id.button_turn_on_wifi).setOnClickListener(this);
        findViewById(R.id.button_start_periodic_task).setOnClickListener(this);
        findViewById(R.id.button_stop_periodic_task).setOnClickListener(this);

        // Check that Google Play Services is available, since we need it to use GcmNetworkManager
        // but the API does not use GoogleApiClient, which would normally perform the check
        // automatically.
        checkPlayServicesAvailable();
    }

    public void startChargingTask() {
        Log.d(TAG, "startChargingTask");

//        OneoffTask task = new OneoffTask.Builder()
//                .setService(MyTaskService.class)
//                .setTag(TASK_TAG_CHARGING)
//                .setExecutionWindow(0L, 3600L)
//                .setRequiresCharging(true)
//                .build();
//
//        mGcmNetworkManager.schedule(task);

        Constraints uploadConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresCharging(true)
                .build();

        OneTimeWorkRequest chargingTaskRequest =
                new OneTimeWorkRequest.Builder(ChargingTaskWorker.class)
                        .setConstraints(uploadConstraints)
                        .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(chargingTaskRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(@Nullable WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Log.i(TAG, "Charging task complete");
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(chargingTaskRequest);
    }

    public void startWifiTask() {
        Log.d(TAG, "startWiFiTask");

//        // Disable WiFi so the task does not start immediately
//        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        wifi.setWifiEnabled(false);
//
//        // [START start_one_off_task]
//        OneoffTask task = new OneoffTask.Builder()
//                .setService(MyTaskService.class)
//                .setTag(TASK_TAG_WIFI)
//                .setExecutionWindow(0L, 3600L)
//                .setRequiredNetwork(Task.NETWORK_STATE_UNMETERED)
//                .build();
//
//        mGcmNetworkManager.schedule(task);
//        // [END start_one_off_task]

        Constraints wifiTaskConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        OneTimeWorkRequest wifiTaskRequest =
                new OneTimeWorkRequest.Builder(WifiTaskWorker.class)
                        .setConstraints(wifiTaskConstraints)
                        .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(wifiTaskRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(@Nullable WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Log.i(TAG, "Wifi task complete");
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(wifiTaskRequest);
    }

    public void startPeriodicTask() {
        Log.d(TAG, "startPeriodicTask");

//        // [START start_periodic_task]
//        PeriodicTask task = new PeriodicTask.Builder()
//                .setService(MyTaskService.class)
//                .setTag(TASK_TAG_PERIODIC)
//                .setPeriod(30L)
//                .build();
//
//        mGcmNetworkManager.schedule(task);
//        // [END start_periodic_task]

        Constraints periodicTaskConstraints = new Constraints.Builder()
                .build();

        PeriodicWorkRequest periodicTaskRequest =
                new PeriodicWorkRequest.Builder(PeriodicTaskWorker.class, 30, TimeUnit.SECONDS)
                        .setConstraints(periodicTaskConstraints)
                        .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(periodicTaskRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(@Nullable WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Log.i(TAG, "Periodic task complete");
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(periodicTaskRequest);
    }

    public void stopPeriodicTask() {
        Log.d(TAG, "stopPeriodicTask");

        // [START stop_periodic_task]
        mGcmNetworkManager.cancelTask(TASK_TAG_PERIODIC, MyTaskService.class);
        // [END stop_per
    }

    private void checkPlayServicesAvailable() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(resultCode)) {
                // Show dialog to resolve the error.
                availability.getErrorDialog(this, resultCode, RC_PLAY_SERVICES).show();
            } else {
                // Unresolvable error
                Toast.makeText(this, "Google Play Services error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start_wifi_task:
                startWifiTask();
                break;
            case R.id.button_start_charging_task:
                startChargingTask();
                break;
            case R.id.button_turn_on_wifi:
                WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                wifi.setWifiEnabled(true);
                break;
            case R.id.button_start_periodic_task:
                startPeriodicTask();
                break;
            case R.id.button_stop_periodic_task:
                stopPeriodicTask();
                break;
        }
    }
}
