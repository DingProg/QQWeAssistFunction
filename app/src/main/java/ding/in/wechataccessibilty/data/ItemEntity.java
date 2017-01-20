package ding.in.wechataccessibilty.data;

import java.io.Serializable;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2017/1/17.
 */

public class ItemEntity implements Serializable{
    public   int id;
    public String value;

    public ItemEntity(int id, String value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
