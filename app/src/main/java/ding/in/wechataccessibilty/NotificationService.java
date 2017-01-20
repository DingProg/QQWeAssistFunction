package ding.in.wechataccessibilty;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import static android.app.Notification.FLAG_NO_CLEAR;
import static android.app.Notification.FLAG_ONGOING_EVENT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static ding.in.wechataccessibilty.ChatConstants.CLOSE_NOTI;
import static ding.in.wechataccessibilty.ChatConstants.CLOSE_QQCHAT;
import static ding.in.wechataccessibilty.ChatConstants.CLOSE_RED;
import static ding.in.wechataccessibilty.ChatConstants.CLOSE_WECHAR;
import static ding.in.wechataccessibilty.ChatConstants.RECEIVER_TYPE;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2017/1/18.
 */

public class NotificationService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int intExtra = intent.getIntExtra(RECEIVER_TYPE, -1);
            switch (intExtra) {
                case CLOSE_QQCHAT:
                    App.qqChat = !App.qqChat;
                    break;
                case CLOSE_RED:
                    App.weRead = !App.weRead;
                    break;
                case CLOSE_WECHAR:
                    App.weChat = !App.weChat;
                    break;
            }

            if (intExtra == CLOSE_NOTI) {
                stopForeground(true);
            } else {
                startForeground(220, createNotification(this));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    public static Notification createNotification(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
        Notification notification = new Notification.Builder(context)
                .setContent(remoteViews)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("抢红包聊天软件通知")
                .build();
        notification.flags |= (FLAG_NO_CLEAR | FLAG_ONGOING_EVENT);

        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra(RECEIVER_TYPE, CLOSE_RED);
        PendingIntent pendingWeIntentRed = PendingIntent.getService(context, 101, intent, FLAG_UPDATE_CURRENT);

        Intent intentQQ = new Intent(context, NotificationService.class);
        intentQQ.putExtra(RECEIVER_TYPE, CLOSE_QQCHAT);
        PendingIntent pendingQQIntent = PendingIntent.getService(context, 1, intentQQ, FLAG_UPDATE_CURRENT);

        Intent intentWe = new Intent(context, NotificationService.class);
        intentWe.putExtra(RECEIVER_TYPE, CLOSE_WECHAR);
        PendingIntent pendingWeIntent = PendingIntent.getService(context, 2, intentWe, FLAG_UPDATE_CURRENT);


        Intent intentClose = new Intent(context, NotificationService.class);
        intentClose.putExtra(RECEIVER_TYPE, CLOSE_NOTI);
        PendingIntent pendingClose = PendingIntent.getService(context, 3, intentClose, FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.btnWeRed, pendingWeIntentRed);
        remoteViews.setOnClickPendingIntent(R.id.btnQQChat, pendingQQIntent);
        remoteViews.setOnClickPendingIntent(R.id.btnWeChat, pendingWeIntent);
        remoteViews.setOnClickPendingIntent(R.id.btnClose, pendingClose);

        setRemoteViews(remoteViews);
        return notification;

//        NotificationManager notificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(1,notification);
    }

    private static void setRemoteViews(RemoteViews remoteViews) {
        if (App.weRead) {
            remoteViews.setTextViewText(R.id.btnWeRed, "关闭抢红包");
        } else {
            remoteViews.setTextViewText(R.id.btnWeRed, "开启抢红包");
        }

        if (App.weChat) {
            remoteViews.setTextViewText(R.id.btnWeChat, "关闭微信聊天");
        } else {
            remoteViews.setTextViewText(R.id.btnWeChat, "开启微信聊天");
        }

        if (App.qqChat) {
            remoteViews.setTextViewText(R.id.btnQQChat, "关闭QQ聊天");
        } else {
            remoteViews.setTextViewText(R.id.btnQQChat, "开启QQ聊天");
        }
    }
}
