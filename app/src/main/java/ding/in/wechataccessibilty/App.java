package ding.in.wechataccessibilty;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import ding.in.wechataccessibilty.data.DatabaseHelp;
import ding.in.wechataccessibilty.data.ItemEntity;
import ding.in.wechataccessibilty.data.SimpleDatabaseOperate;
import okhttp3.OkHttpClient;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2017/1/16.
 */

public class App extends Application {

    private static App app;
    private OkHttpClient okHttpClient = new OkHttpClient();
    private List<ItemEntity> keyWordList = new ArrayList<>();
    private List<ItemEntity> keyWordListRed = new ArrayList<>();
    private SQLiteDatabase mWritableDatabase;

    public static boolean weChat = false;
    public static boolean qqChat = false;
    public static boolean weRead = true;

    public static App getInstance() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        init();
    }

    private void init() {
        DatabaseHelp sqlHelp = new DatabaseHelp(this, "ding_in_redchart", null, 1);
        mWritableDatabase = sqlHelp.getWritableDatabase();
        updataData();
        updataDataRed();
        initReceiver();
    }

    public SQLiteDatabase getDatabase() {
        return mWritableDatabase;
    }

    public OkHttpClient getOkHttp() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient();
        }
        return okHttpClient;
    }

    /**
     * 聊天关键字
     */
    public void updataData() {
        keyWordList.clear();
        SimpleDatabaseOperate.query(getDatabase(), keyWordList);
    }

    public List<ItemEntity> getData() {
        return keyWordList;
    }

    /**
     * 红包关键字
     */
    public void updataDataRed() {
        keyWordListRed.clear();
        SimpleDatabaseOperate.queryRed(getDatabase(), keyWordListRed);
    }

    public List<ItemEntity> getDataRed() {
        return keyWordListRed;
    }

    public void initReceiver() {
        /* 注册屏幕唤醒时的广播 */
        IntentFilter mScreenOnFilter = new IntentFilter("android.intent.action.SCREEN_ON");
        registerReceiver(mScreenOReceiver, mScreenOnFilter);

        /* 注册机器锁屏时的广播 */
//        IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
//        registerReceiver(mScreenOReceiver, mScreenOffFilter);

    }

    /**
     * 锁屏的管理类叫KeyguardManager，
     * 通过调用其内部类KeyguardLockmKeyguardLock的对象的disableKeyguard方法可以取消系统锁屏，
     * newKeyguardLock的参数用于标识是谁隐藏了系统锁屏
     */
    private BroadcastReceiver mScreenOReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {
                //System.out.println("—— SCREEN_ON ——");
                //DevicesUtils.wakeAndUnlock(App.app);
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                //System.out.println("—— SCREEN_OFF ——");
            }
        }

    };


}
