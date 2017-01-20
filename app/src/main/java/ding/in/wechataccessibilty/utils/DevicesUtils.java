package ding.in.wechataccessibilty.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.POWER_SERVICE;

/**
 * Description：用于解锁
 *
 * @author dingdegao
 *         create by 2017/1/18.
 */

public class DevicesUtils {

    public static void wakeAndUnlock(Context context){
        //获取电源管理器对象
        PowerManager pm = (PowerManager)context.getSystemService(POWER_SERVICE);
        //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
        PowerManager.WakeLock mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
        //点亮屏幕
        mWakelock.acquire();
        mWakelock.release();

        //得到键盘锁管理器对象
        KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("bright");
        //解锁
        //keyguardLock.disableKeyguard();

        /**
         * 锁屏代码
         *    //锁屏
         *   keyguardLock.reenableKeyguard();
         *    //释放wakeLock，关灯
         *    mWakelock.release();
         */
    }

}
