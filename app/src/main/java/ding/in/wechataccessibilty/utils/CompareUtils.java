package ding.in.wechataccessibilty.utils;

import java.util.List;

import ding.in.wechataccessibilty.App;
import ding.in.wechataccessibilty.data.ItemEntity;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2017/1/17.
 */

public class CompareUtils {

    public static boolean hasSame(String str) {
        List<ItemEntity> data = App.getInstance().getData();
        if(data == null || data.size() == 0) return false;
        for (ItemEntity itemEntity : data) {
            if(str.contains(itemEntity.value)){
                return true;
            }
        }
        return false;
    }

    public static boolean hasRedSame(String str) {
        List<ItemEntity> data = App.getInstance().getDataRed();
        if(data == null || data.size() == 0) return false;
        for (ItemEntity itemEntity : data) {
            if(str.contains(itemEntity.value)){
                return true;
            }
        }
        return false;
    }
}
