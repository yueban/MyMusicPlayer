package bigfat.mymusicplayer.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by bigfat on 2014/5/5.
 */
public class DBUtil extends SQLiteOpenHelper {
    public static final String databaseName = "database";
    public static final String T_MusicFile_Name = "T_MusicFile";
    public static final String T_PlayListFile_Name = "T_PlayListFile";
    public static final String T_PlayList_Name = "T_PlayList";
    private static final int VERSION = 1;

    public DBUtil(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBUtil(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public DBUtil(Context context, String name) {
        this(context, name, VERSION);
    }

    public static SQLiteDatabase getWritableDB(Context context, String db_name) {
        return new DBUtil(context, db_name).getWritableDatabase();
    }

    public static SQLiteDatabase getReadableDB(Context context, String db_name) {
        return new DBUtil(context, db_name).getReadableDatabase();
    }

    public static Cursor rawQueryCursor(SQLiteDatabase db, String sql, String[] selectionArgs) {
        return db.rawQuery(sql, selectionArgs);
    }

    public static void execSqlDatabase(Context context, String db_name, String[] sql) {
        SQLiteDatabase db = getWritableDB(context, db_name);
        db.beginTransaction();
        for (String s : sql) {
            db.execSQL(s);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public static void execSqlDatabase(Context context, String db_name, String sql) {
        SQLiteDatabase db = getWritableDB(context, db_name);
        db.execSQL(sql);
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + T_MusicFile_Name + " (path nvarchar(200),title nvarchar(100),pinyin nvarchar(150),folder nvarchar(100),album nvarchar(30),artist nvarchar(30),favorite boolean,primary key(path))");
        db.execSQL("create table " + T_PlayListFile_Name + " (path nvarchar(200),title nvarchar(100),pinyin nvarchar(150),album nvarchar(30),artist nvarchar(30),playlist int,primary key(path,playlist))");
        db.execSQL("create table " + T_PlayList_Name + " (id integer primary key autoincrement,playlist nvarchar(30))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }
}