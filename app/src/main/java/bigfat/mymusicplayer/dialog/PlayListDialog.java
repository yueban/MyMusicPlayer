package bigfat.mymusicplayer.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import bigfat.mymusicplayer.R;
import bigfat.mymusicplayer.util.DBUtil;

/**
 * Created by bigfat on 2014/5/13.
 */
public class PlayListDialog extends Dialog {
    private Context context;
    private ArrayList<HashMap<String, String>> musicList;
    private ArrayList<HashMap<String, String>> playList;

    private ListView listView;
    private PlayListDialogAdapter adapter;

    public PlayListDialog(Context context, ArrayList<HashMap<String, String>> musicList) {
        super(context);
        this.context = context;
        this.musicList = musicList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_playlist);
        listView = (ListView) findViewById(R.id.listViewPlayListDialog);
        new Runnable() {
            @Override
            public void run() {
                initPlayList();
                adapter = new PlayListDialogAdapter();
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        if (position == playList.size() - 1) {
                            final EditText editText = new EditText(context);
                            new AlertDialog.Builder(context).setTitle("请输入").setView(
                                    editText).setPositiveButton("确定", new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!editText.getText().toString().trim().equals("")) {
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                SQLiteDatabase db = null;
                                                Cursor cursor = null;
                                                String playlistId = "";
                                                try {
                                                    DBUtil.execSqlDatabase(context, DBUtil.databaseName, "insert into " + DBUtil.T_PlayList_Name + " (id,playlist)values(null,'" + editText.getText() + "')");
                                                    db = DBUtil.getReadableDB(context, DBUtil.databaseName);
                                                    cursor = DBUtil.rawQueryCursor(db, "select max(id) as id from " + DBUtil.T_PlayList_Name, null);
                                                    while (cursor.moveToNext()) {
                                                        playlistId = cursor.getString(cursor.getColumnIndex("id"));
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
                                                insertIntoPlayList(playlistId, editText.getText().toString());
                                            }
                                        }.run();
                                    }
                                }
                            })
                                    .setNegativeButton("取消", null).show();
                        } else {
                            insertIntoPlayList(playList.get(position).get("id"), playList.get(position).get("playlist"));
                        }
                    }
                });
            }
        }.run();
    }

    //向播放列表插入歌曲
    public void insertIntoPlayList(final String playlistId, final String playlist) {
        new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = null;
                Cursor cursor = null;
                int count_before = 0;
                int count_after = 0;
                try {
                    db = DBUtil.getWritableDB(context, DBUtil.databaseName);
                    //获取插入前的歌曲数
                    cursor = DBUtil.rawQueryCursor(db, "select count(*) as count from " + DBUtil.T_PlayListFile_Name, null);
                    if (cursor.moveToNext()) {
                        count_before = Integer.parseInt(cursor.getString(cursor.getColumnIndex("count")));
                    }
                    cursor.close();
                    //向播放列表中插入歌曲
                    db.beginTransaction();
                    for (HashMap<String, String> map : musicList) {
                        db.execSQL("insert or ignore into " + DBUtil.T_PlayListFile_Name + " (path,title,pinyin,album,artist,playlist)values('" + map.get("path").replace("'", "''") + "','" + map.get("title").replace("'", "''") + "','" + map.get("pinyin").replace("'", "''") + "','" + map.get("album").replace("'", "''") + "','" + map.get("artist").replace("'", "''") + "'," + playlistId + ")");
                    }
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    //获取插入后歌曲数
                    cursor = DBUtil.rawQueryCursor(db, "select count(*) as count from " + DBUtil.T_PlayListFile_Name, null);
                    if (cursor.moveToNext()) {
                        count_after = Integer.parseInt(cursor.getString(cursor.getColumnIndex("count")));
                    }
                    Toast.makeText(context, count_after - count_before + "首歌曲添加至" + playlist, Toast.LENGTH_SHORT).show();
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
                dismiss();
            }
        }.run();
    }

    private void initPlayList() {
        playList = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = DBUtil.getReadableDB(context, DBUtil.databaseName);
            cursor = DBUtil.rawQueryCursor(db, "select id,playlist from " + DBUtil.T_PlayList_Name, null);
            while (cursor.moveToNext()) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", cursor.getString(cursor.getColumnIndex("id")));
                map.put("playlist", cursor.getString(cursor.getColumnIndex("playlist")));
                playList.add(map);
            }
            //添加新建播放列表项
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("playlist", "新建播放列表");
            playList.add(map);
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
    }

    class PlayListDialogAdapter extends BaseAdapter {
        private ViewHolder holder;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_playlist_dialog, null);
                holder.textViewPlayListDialog = (TextView) convertView.findViewById(R.id.textViewPlayListDialog);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textViewPlayListDialog.setText(playList.get(position).get("playlist"));
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return playList.get(position);
        }

        @Override
        public int getCount() {
            return playList.size();
        }

        private final class ViewHolder {
            TextView textViewPlayListDialog;
        }
    }
}
