package com.example.musicplayer30.utils;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;


public class RequestPermissions {

    public static final int PERMISSIONS_REQUEST_CODE = 10;

    private static OnPermissionsRequestListener sListener;

    public static void requestPermissions(Activity activity, String[] permissions,
                                          OnPermissionsRequestListener listener) {

        sListener = listener;

        List<String> permissionList = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(activity, permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissions[i]);
            }
        }

        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    permissionList.toArray(new String[permissionList.size()]),
                    PERMISSIONS_REQUEST_CODE);
        } else {
            listener.onGranted();
        }
    }

    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0) {
                List<String> deniedPermissions = new ArrayList<>();
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i]);
                    }
                }

                if (deniedPermissions.isEmpty()) {
                    sListener.onGranted();
                } else {
                    sListener.onDenied(deniedPermissions);
                }
            }
        }
    }

    public interface OnPermissionsRequestListener {

        public void onGranted();

        public void onDenied(List<String> deniedList);
    }
}
