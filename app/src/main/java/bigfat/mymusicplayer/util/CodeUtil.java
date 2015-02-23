package bigfat.mymusicplayer.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bigfat on 2014/5/21.
 */
public class CodeUtil {
    public static final String MUSIC_CHANGE_ACTION = "action_musicchange";
    public static final String EXIT_MUSIC_STATUS = "exit_music_status";

    //获取列表数据
    public static ArrayList<HashMap<String, String>> getMusicList(Context context, String sql, FileUtil.SortKey sortKey) {
        ArrayList<HashMap<String, String>> musicList = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = DBUtil.getReadableDB(context, DBUtil.databaseName);
            cursor = DBUtil.rawQueryCursor(db, sql, null);
            final Cursor cursorFinal = cursor;
            if (cursorFinal != null) {
                while (cursorFinal.moveToNext()) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("path", cursorFinal.getString(cursorFinal.getColumnIndex("path")));
                    map.put("title", cursorFinal.getString(cursorFinal.getColumnIndex("title")));
                    map.put("pinyin", cursorFinal.getString(cursorFinal.getColumnIndex("pinyin")));
                    map.put("album", cursorFinal.getString(cursorFinal.getColumnIndex("album")));
                    map.put("artist", cursorFinal.getString(cursorFinal.getColumnIndex("artist")));
                    if (sortKey == FileUtil.SortKey.PlayList) {
                        map.put("playlist", cursorFinal.getString(cursorFinal.getColumnIndex("playlist")));
                    }
                    musicList.add(map);
                }
                return musicList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return musicList;
    }
}
