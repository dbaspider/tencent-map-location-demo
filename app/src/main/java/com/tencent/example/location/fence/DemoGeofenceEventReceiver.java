package com.tencent.example.location.fence;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.text.format.DateFormat;


import com.tencent.example.location.R;

import java.util.Date;
import java.util.Random;

/**
 * receiver, 处理触发的地理围栏事件.
 */
public class DemoGeofenceEventReceiver extends BroadcastReceiver {
    private static final String NOTIFICATION_CHANNEL_NAME = "location_demo_geo_fence_receiver";

    private NotificationManager notificationManager;
    private boolean isCreateChannel;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null
                || !DemoGeofenceService.ACTION_TRIGGER_GEOFENCE.equals(intent
                .getAction())) {
            return;
        }

        // Tag
        // String tag = intent.getStringExtra("KEY_GEOFENCE_ID");
        // 围栏已触发, 可根据需要决定是否将其删除
        // TODO 注意, 这里仅通知TencentGeofenceManager删除围栏, 但并没有同步删除 UI 上的相应元素
        // 移除地理围栏
        // DemoGeofenceService.startMe(context,
        //		DemoGeofenceService.ACTION_DEL_GEOFENCE, tag);

        NotificationManager notiManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notiManager.notify(new Random().nextInt(),
                createNotification(context, intent));

        TencentApplication.getEvents().add(
                DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date()) + " "
                        + toString(intent));
    }

    private Notification createNotification(Context context, Intent intent) {
        // Tag
        String tag = intent.getStringExtra("KEY_GEOFENCE_ID");
        // 进入围栏还是退出围栏
        boolean enter = intent.getBooleanExtra(
                LocationManager.KEY_PROXIMITY_ENTERING, true);
        // 其他自定义的 extra 字段
        double lat = intent.getDoubleExtra("KEY_GEOFENCE_LAT", 0);
        double lng = intent.getDoubleExtra("KEY_GEOFENCE_LNG", 0);

        Notification.Builder builder = null;
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (notificationManager == null) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = context.getPackageName();
            if (!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(context.getApplicationContext(), channelId);
        } else {
            builder = new Notification.Builder(context.getApplicationContext());
        }
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("围栏事件通知")
                .setContentText(toString(enter, tag, lat, lng))
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 200, 100, 200, 100, 200})
                .setContentIntent(createPendingIntent(context))
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }
        notification.defaults = Notification.DEFAULT_ALL;
        return notification;
    }

    private PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context, DemoGeofenceEventActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        } else {
            return PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private static String toString(boolean enter, String tag, double lat,
                                   double lng) {
        if (enter) {
            return "已进入 " + tag + ",(" + lat + "," + lng + ")";
        } else {
            return "已退出 " + tag + ",(" + lat + "," + lng + ")";
        }
    }

    private static String toString(Intent intent) {
        // Tag
        String tag = intent.getStringExtra("KEY_GEOFENCE_ID");
        // 进入围栏还是退出围栏
        boolean enter = intent.getBooleanExtra(
                LocationManager.KEY_PROXIMITY_ENTERING, true);
        // 其他自定义的 extra 字段
        double lat = intent.getDoubleExtra("KEY_GEOFENCE_LAT", 0);
        double lng = intent.getDoubleExtra("KEY_GEOFENCE_LNG", 0);
        return toString(enter, tag, lat, lng);
    }
}
