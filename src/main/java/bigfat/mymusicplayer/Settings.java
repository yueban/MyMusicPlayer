package bigfat.mymusicplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.CompoundButton;

import bigfat.mymusicplayer.service.MusicService;
import bigfat.mymusicplayer.util.CodeUtil;
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

        Switch switchSaveMusicStatus = (Switch) findViewById(R.id.switchSaveMusicStatus);
        switchSaveMusicStatus.setChecked(getSharedPreferences("settings", 0).getBoolean(CodeUtil.EXIT_MUSIC_STATUS, false));
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
