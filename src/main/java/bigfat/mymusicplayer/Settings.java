package bigfat.mymusicplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import bigfat.mymusicplayer.service.MusicService;
import bigfat.mymusicplayer.util.CodeUtil;
import bigfat.mymusicplayer.util.FileUtil;
import bigfat.mymusicplayer.util.ThreadUtil;
import de.ankri.views.Switch;

/**
 * Created by bigfat on 2014/5/7.
 */
public class Settings extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //配置ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("设置");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        //绑定控件
        final TextView textViewUpdateMusicFile = (TextView) findViewById(R.id.textViewUpdateMusicFile);
        Switch switchSaveMusicStatus = (Switch) findViewById(R.id.switchSaveMusicStatus);
        switchSaveMusicStatus.setChecked(getSharedPreferences("settings", 0).getBoolean(CodeUtil.EXIT_MUSIC_STATUS, false));
        //设置监听器
        textViewUpdateMusicFile.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    textViewUpdateMusicFile.setBackgroundColor(0x25FFFFFF);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    textViewUpdateMusicFile.setBackgroundColor(0x00FFFFFF);
                }
                return false;
            }
        });
        textViewUpdateMusicFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ThreadUtil.DatabaseAsyncTask(Settings.this, true).execute(FileUtil.getRootPath());
            }
        });
        switchSaveMusicStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Runnable() {
                    @Override
                    public void run() {
                        getSharedPreferences("settings", 0).edit().putBoolean(CodeUtil.EXIT_MUSIC_STATUS, isChecked).commit();
                        if (isChecked) {
                            SharedPreferences.Editor editor = getSharedPreferences("data", 0).edit();
                            editor.putString("sql", MusicService.sql);
                            editor.putInt("position", MusicService.position);
                            editor.commit();
                        }
                    }
                }.run();
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
}
