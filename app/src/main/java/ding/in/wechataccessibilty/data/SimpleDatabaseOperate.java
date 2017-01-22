package ding.in.wechataccessibilty.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2017/1/17.
 */

public class SimpleDatabaseOperate {

    public static void insert(SQLiteDatabase db, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("keyword", value);
        db.insert("usertable", null, contentValues);
    }

    public static void delete(SQLiteDatabase db, int id) {
       db.execSQL("DELETE FROM usertable WHERE _id="+id);
    }

    public static void  query(SQLiteDatabase db,List<ItemEntity> list) {
        Cursor cursor = db.query ("usertable",new String[]{"_id","keyword"},null,null,null,null,null);
        if(cursor.moveToFirst()){
            while(cursor != null && cursor.moveToNext()){
                //获得ID
                int id = cursor.getInt(0);
                //获得关键字
                String keyword=cursor.getString(1);
                list.add(new ItemEntity(id,keyword));
            }
        }
    }

    public static void insertRed(SQLiteDatabase db, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("keyword", value);
        db.insert("usertablered", null, contentValues);
    }

    public static void deleteRed(SQLiteDatabase db, int id) {
        db.execSQL("DELETE FROM usertablered WHERE _id="+id);
    }

    public static void  queryRed(SQLiteDatabase db,List<ItemEntity> list) {
        Cursor cursor = db.query ("usertablered",new String[]{"_id","keyword"},null,null,null,null,null);
        if(cursor.moveToFirst()){
            while(cursor != null && cursor.moveToNext()){
                //获得ID
                int id = cursor.getInt(0);
                //获得关键字
                String keyword=cursor.getString(1);
                list.add(new ItemEntity(id,keyword));
            }
        }
    }

}
