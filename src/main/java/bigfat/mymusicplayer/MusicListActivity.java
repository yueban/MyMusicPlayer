package bigfat.mymusicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import bigfat.mymusicplayer.fragment.MusicList;
import bigfat.mymusicplayer.util.FileUtil;

/**
 * Created by bigfat on 2014/5/9.
 */
public class MusicListActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musiclist);
        //获取ActionBar
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        new Runnable() {
            @Override
            public void run() {
                //获取Intent数据
                Intent intent = getIntent();
                final FileUtil.SortKey sortKey = FileUtil.SortKey.valueOf(intent.getStringExtra("sortKey"));
                final String keyStr = intent.getStringExtra(("keyStr"));
                //判断显示的音乐列表的类别，设置ActionBar标题
                switch (sortKey) {
                    case Folder:
                        actionBar.setTitle(keyStr.substring(keyStr.lastIndexOf("/") + 1));
                        break;
                    case PlayList:
                        actionBar.setTitle(intent.getStringExtra("playlist"));
                        break;
                    case FavoriteList:
                        actionBar.setTitle("我的喜爱");
                        break;
                    default:
                        actionBar.setTitle(keyStr);
                        break;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bundle b = new Bundle();
                        b.putString("sortKey", sortKey.name());
                        b.putString("keyStr", keyStr);
                        MusicList musicList = new MusicList();
                        musicList.setArguments(b);
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutMusicList, musicList).commit();
                    }
                });
            }
        }.run();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}