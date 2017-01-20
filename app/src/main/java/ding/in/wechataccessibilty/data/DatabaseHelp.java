package ding.in.wechataccessibilty.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2017/1/17.
 */

public class DatabaseHelp extends SQLiteOpenHelper {

    public DatabaseHelp(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSqlTable = "create table usertable(_id integer primary key autoincrement,keyword text)";
        String createSqlTableRed = "create table usertablered(_id integer primary key autoincrement,keyword text)";
        db.execSQL(createSqlTable);
        db.execSQL(createSqlTableRed);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            onCreate(db);
        }
    }
}
