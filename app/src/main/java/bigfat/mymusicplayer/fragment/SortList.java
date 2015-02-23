package bigfat.mymusicplayer.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import bigfat.mymusicplayer.MusicListActivity;
import bigfat.mymusicplayer.R;
import bigfat.mymusicplayer.util.DBUtil;
import bigfat.mymusicplayer.util.FileUtil;

/**
 * Created by bigfat on 2014/5/8.
 */
public class SortList extends ListFragment {
    SortListAdapter adapter;
    //显示的类别
    private FileUtil.SortKey sortKey;
    //分组列表
    private ArrayList<HashMap<String, String>> groupList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sortlist, null);
        Bundle b = getArguments();
        try {
            sortKey = FileUtil.SortKey.valueOf(b.getString("sortKey"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ListView listView = getListView();
        new Runnable() {
            @Override
            public void run() {
                initGroupList();
                adapter = new SortListAdapter();
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        new Runnable() {
                            @Override
                            public void run() {
                                //设置Intent数据
                                Intent intent = new Intent();
                                intent.putExtra("sortKey", sortKey.name());
                                intent.putExtra("keyStr", groupList.get(position).get("groupname"));
                                intent.setClass(getActivity(), MusicListActivity.class);
                                //跳转至音乐文件显示列表Activity
                                startActivity(intent);
                            }
                        }.run();
                    }
                });
            }
        }.run();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    //从数据库中获取数据
    private void initGroupList() {
        groupList = new ArrayList<HashMap<String, String>>();
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            db = DBUtil.getReadableDB(getActivity(), DBUtil.databaseName);
            switch (sortKey) {
                case Folder:
                    cursor = DBUtil.rawQueryCursor(db, "select folder as groupname,count(*) as count from " + DBUtil.T_MusicFile_Name + " group by folder order by groupname", null);
                    while (cursor.moveToNext()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("groupname", cursor.getString(cursor.getColumnIndex("groupname")));
                        map.put("count", cursor.getString(cursor.getColumnIndex("count")));
                        groupList.add(map);
                    }
                    break;
                case Album:
                    cursor = DBUtil.rawQueryCursor(db, "select album as groupname,count(*) as count from " + DBUtil.T_MusicFile_Name + " group by album order by groupname", null);
                    while (cursor.moveToNext()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("groupname", cursor.getString(cursor.getColumnIndex("groupname")));
                        map.put("count", cursor.getString(cursor.getColumnIndex("count")));
                        groupList.add(map);
                    }
                    break;
                case Artist:
                    cursor = DBUtil.rawQueryCursor(db, "select artist as groupname,count(*) as count from " + DBUtil.T_MusicFile_Name + " group by artist order by groupname", null);
                    while (cursor.moveToNext()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("groupname", cursor.getString(cursor.getColumnIndex("groupname")));
                        map.put("count", cursor.getString(cursor.getColumnIndex("count")));
                        groupList.add(map);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "程序出bug了，如果您方便，劳烦联系开发者18607006059", Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    class SortListAdapter extends BaseAdapter {
        private ViewHolder holder;

        @Override
        public int getCount() {
            return groupList.size();
        }

        @Override
        public Object getItem(int position) {
            return groupList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_sort, null);
                holder.textViewGroupName = (TextView) convertView.findViewById(R.id.textViewGroupName);
                holder.textViewCount = (TextView) convertView.findViewById(R.id.textViewCount);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //读取要显示的数据
            HashMap<String, String> map = groupList.get(position);
            String groupname = map.get("groupname");
            //如果是文件夹页面，则只显示文件夹的名字，而不显示完整的路径
            groupname = sortKey == FileUtil.SortKey.Folder ? groupname.substring(groupname.lastIndexOf("/") + 1) : groupname;
            String count = map.get("count") + "首歌曲";
            holder.textViewGroupName.setText(groupname);
            holder.textViewCount.setText(count);
            return convertView;
        }

        private final class ViewHolder {
            TextView textViewGroupName;
            TextView textViewCount;
        }
    }
}
