package ding.in.wechataccessibilty.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telecom.ConnectionService;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2017/1/18.
 */

public class NetCheckUtils {

    public static boolean hasNet(Context context){
        ConnectivityManager connectivityManager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }
}
