package bigfat.mymusicplayer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import bigfat.mymusicplayer.util.DBUtil;
import bigfat.mymusicplayer.util.FileUtil;

/**
 * Created by bigfat on 2014/5/7.
 */
public class PlayList extends ActionBarActivity {
    //播放列表
    private ArrayList<HashMap<String, String>> playList;
    //适配器
    private PlayListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        //配置ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.playlist);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        //设置ListView
        final ListView listView = (ListView) findViewById(R.id.listViewPlayList);
        new Runnable() {
            @Override
            public void run() {
                initPlayList();
                adapter = new PlayListAdapter();
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent();
                        //第一个列表是我的喜爱列表，传参作特殊处理
                        if (position == 0) {
                            intent.putExtra("sortKey", FileUtil.SortKey.FavoriteList.name());
                            intent.putExtra("keyStr", "");
                        } else {
                            intent.putExtra("sortKey", FileUtil.SortKey.PlayList.name());
                            intent.putExtra("keyStr", playList.get(position).get("id"));
                        }
                        intent.putExtra("playlist", playList.get(position).get("playlist"));
                        intent.setClass(PlayList.this, MusicListActivity.class);
                        startActivity(intent);
                    }
                });
                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                        //第一个列表是我的喜爱列表，补课删除，不响应长按事件
                        if (position != 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(PlayList.this);
                            builder.setMessage("确认删除吗");
                            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            String[] sql = new String[2];
                                            sql[0] = "delete from " + DBUtil.T_PlayList_Name + " where id=" + playList.get(position).get("id");
                                            sql[1] = "delete from " + DBUtil.T_PlayList_Name + " where playlist=" + playList.get(position).get("id");
                                            DBUtil.execSqlDatabase(PlayList.this, DBUtil.databaseName, sql);
                                            Toast.makeText(PlayList.this, "删除成功", Toast.LENGTH_SHORT).show();
                                            adapter.notifyDataSetChanged();
                                        }
                                    }.run();
                                }
                            });
                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.create().show();
                        }
                        return false;
                    }
                });
            }
        }.run();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    //获取播放列表数据
    private void initPlayList() {
        playList = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = DBUtil.getReadableDB(PlayList.this, DBUtil.databaseName);
            //添加我的喜爱列表
            cursor = DBUtil.rawQueryCursor(db, "select count(*) as count from " + DBUtil.T_MusicFile_Name + " where favorite=1", null);
            if (cursor.moveToNext()) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("playlist", "我的喜爱");
                map.put("count", cursor.getString(cursor.getColumnIndex("count")));
                playList.add(map);
            }
            cursor.close();
            //添加用户自定义播放列表
            cursor = db.rawQuery("select a.id,a.playlist,ifnull(b.count,0) as count from " + DBUtil.T_PlayList_Name + " as a left join (select playlist,count(*) as count from " + DBUtil.T_PlayListFile_Name + " group by playlist) as b on a.id=b.playlist", null);
            while (cursor.moveToNext()) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", cursor.getString(cursor.getColumnIndex("id")));
                map.put("playlist", cursor.getString(cursor.getColumnIndex("playlist")));
                map.put("count", cursor.getString(cursor.getColumnIndex("count")));
                playList.add(map);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, "添加");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case 1:
                new Runnable() {
                    @Override
                    public void run() {
                        final EditText editText = new EditText(PlayList.this);
                        new AlertDialog.Builder(PlayList.this).setTitle("请输入").setView(
                                editText).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!editText.getText().toString().trim().equals("")) {
                                    DBUtil.execSqlDatabase(PlayList.this, DBUtil.databaseName, "insert into " + DBUtil.T_PlayList_Name + " (id,playlist)values(null,'" + editText.getText() + "')");
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        })
                                .setNegativeButton("取消", null).show();
                    }
                }.run();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class PlayListAdapter extends BaseAdapter {
        private ViewHolder holder;

        @Override
        public int getCount() {
            return playList.size();
        }

        @Override
        public Object getItem(int position) {
            return playList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(PlayList.this).inflate(R.layout.list_item_playlist, null);
                holder.textViewPlayListName = (TextView) convertView.findViewById(R.id.textViewPlayListName);
                holder.textViewPlayListCount = (TextView) convertView.findViewById(R.id.textViewPlayListCount);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //读取要显示的数据
            HashMap<String, String> map = playList.get(position);
            String groupname = map.get("playlist");
            String count = map.get("count") + "首歌曲";
            holder.textViewPlayListName.setText(groupname);
            holder.textViewPlayListCount.setText(count);
            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            initPlayList();
        }

        private final class ViewHolder {
            TextView textViewPlayListName;
            TextView textViewPlayListCount;
        }
    }
}
