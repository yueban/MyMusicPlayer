package bigfat.mymusicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import bigfat.mymusicplayer.util.CodeUtil;
import bigfat.mymusicplayer.util.DBUtil;
import bigfat.mymusicplayer.widget.ClearEditText;

/**
 * Created by bigfat on 2014/5/28.
 */
public class Search extends ActionBarActivity {
    //所有音乐列表
    private ArrayList<HashMap<String, String>> musicList;
    //搜索结果列表
    private ArrayList<HashMap<String, String>> resultList = new ArrayList<HashMap<String, String>>();
    private ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //配置ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("搜索");
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //配置ListView
        final ListView listView = (ListView) findViewById(R.id.listViewMusicListSearch);
        new Runnable() {
            @Override
            public void run() {
                musicList = CodeUtil.getMusicList(Search.this, "select path,title,pinyin,album,artist from " + DBUtil.T_MusicFile_Name + " order by pinyin", null);
                adapter = new ListViewAdapter();
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
                        HashMap<String, String> map = resultList.get(position);
                        list.add(map);
                        MainActivity.getMusicBinder().playMusicList(list, 0, "select path,title,pinyin,album,artist from " + DBUtil.T_MusicFile_Name + " where path='" + map.get("path").replace("'", "''") + "'");
                        //返回播放界面
                        Intent intent = new Intent(Search.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }
                });
            }
        }.run();

        //配置EditText
        ClearEditText clearEditTextSearch = (ClearEditText) findViewById(R.id.clearEditTextSearch);
        clearEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resultList = new ArrayList<HashMap<String, String>>();
                if (s.length() == 0) {
                    adapter.notifyDataSetChanged();
                } else {
                    for (HashMap<String, String> map : musicList) {
                        if (map.get("title").contains(s)) {
                            resultList.add(map);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class ListViewAdapter extends BaseAdapter {

        private ViewHolder holder;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(Search.this).inflate(R.layout.list_item_music_search, null);
                holder.textViewMusicTitle = (TextView) convertView.findViewById(R.id.textViewMusicTitle);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textViewMusicTitle.setText(resultList.get(position).get("title"));
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        private final class ViewHolder {
            TextView textViewMusicTitle;
        }
    }
}
