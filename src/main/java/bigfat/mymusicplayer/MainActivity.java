package bigfat.mymusicplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import bigfat.mymusicplayer.fragment.MusicInfo;
import bigfat.mymusicplayer.service.MusicService;
import bigfat.mymusicplayer.util.CodeUtil;
import bigfat.mymusicplayer.util.FileUtil;
import bigfat.mymusicplayer.util.ThreadUtil;

public class MainActivity extends FragmentActivity {
    //音乐播放Service
    private static MusicService.MusicBinder musicBinder;
    //BroadcastReceiver模块，接收播放歌曲的改变
    private BroadcastReceiver musicChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CodeUtil.MUSIC_CHANGE_ACTION)) {
                musicInfo.showMusicInfo();
            }
        }
    };
    //控件
    private ImageView imageViewSearch;
    private TextView textViewMusicList;
    private TextView textViewPlayList;
    private ImageView imageViewMenu;
    private ViewPager viewPagerMusicInfo;
    //音乐播放控件
    private ImageView imageViewPlayMusic;
    private ImageView imageViewPreviousMusic;
    private ImageView imageViewNextMusic;
    private ImageView imageViewMusicPlayMode;
    private SeekBar seekBarMusic;
    private TextView textViewPlayTimeNow;
    private TextView textViewPlayTimeTotal;
    //Fragment模块
    private ArrayList<Fragment> fragmentList;
    private MusicInfo musicInfo;
    private ServiceConnection sc;
    private boolean isBind = false;

    public static MusicService.MusicBinder getMusicBinder() {
        return musicBinder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //注册BroadcastReceiver
        registerReceiver(musicChangeReceiver, new IntentFilter(CodeUtil.MUSIC_CHANGE_ACTION));
        //绑定控件
        imageViewSearch = (ImageView) findViewById(R.id.imageViewSearch);
        textViewMusicList = (TextView) findViewById(R.id.textViewMusicList);
        textViewPlayList = (TextView) findViewById(R.id.textViewPlayList);
        imageViewMenu = (ImageView) findViewById(R.id.imageViewMenu);
        viewPagerMusicInfo = (ViewPager) findViewById(R.id.viewPagerMusicInfo);
        //绑定音乐播放控件
        imageViewPlayMusic = (ImageView) findViewById(R.id.imageViewPlayMusic);
        imageViewPreviousMusic = (ImageView) findViewById(R.id.imageViewPreviousMusic);
        imageViewNextMusic = (ImageView) findViewById(R.id.imageViewNextMusic);
        imageViewMusicPlayMode = (ImageView) findViewById(R.id.imageViewMusicPlayMode);
        seekBarMusic = (SeekBar) findViewById(R.id.seekBarMusic);
        textViewPlayTimeNow = (TextView) findViewById(R.id.textViewPlayTimeNow);
        textViewPlayTimeTotal = (TextView) findViewById(R.id.textViewPlayTimeTotal);
        //绑定监听器
        OnClickEvent onClickEvent = new OnClickEvent();
        imageViewSearch.setOnClickListener(onClickEvent);
        textViewMusicList.setOnClickListener(onClickEvent);
        textViewPlayList.setOnClickListener(onClickEvent);
        imageViewMenu.setOnClickListener(onClickEvent);
        imageViewMusicPlayMode.setOnClickListener(onClickEvent);
        //配置ViewPager
        initFragmentList();
        viewPagerMusicInfo.setAdapter(new MusicInfoViewPagerAdapter(getSupportFragmentManager()));
        //绑定音乐播放服务
        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                musicBinder = (MusicService.MusicBinder) service;
                isBind = true;
                //将控件传递给MusicBinder
                musicBinder.setView(imageViewPlayMusic, imageViewPreviousMusic, imageViewNextMusic, imageViewMusicPlayMode, seekBarMusic, textViewPlayTimeNow, textViewPlayTimeTotal);
                //刷新控件状态/显示
                musicBinder.refreshView();
                //初始化计时器
                Timer mTimer = new Timer();
                TimerTask mTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (MusicService.isPlaying) {
                            final int currentPosition = musicBinder.getMPCurrentPosition();
                            if (!MusicService.isSeekBarChanging) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        seekBarMusic.setProgress(currentPosition);
                                    }
                                });
                            }
                        }
                    }
                };
                mTimer.schedule(mTimerTask, 0, 500);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        if (!isBind) {
            startService(new Intent(MainActivity.this, MusicService.class));
            bindService(new Intent(MainActivity.this, MusicService.class), sc, Context.BIND_AUTO_CREATE);
        }

        //获取启动次数
        new Runnable() {
            @Override
            public void run() {
                SharedPreferences spData = getSharedPreferences("data", 0);
                int startCount = spData.getInt("startCount", 0);
                if (startCount == 0) {
                    //修改启动次数
                    SharedPreferences.Editor editor = spData.edit();
                    editor.putInt("startCount", 1);
                    editor.commit();
                    //读取所有音乐文件信息并存入数据库
                    new ThreadUtil.DatabaseAsyncTask(MainActivity.this).execute(FileUtil.getRootPath());
                }
            }
        }.run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑BroadcastReceiver
        unregisterReceiver(musicChangeReceiver);
        //解绑音乐播放service
        unbindService(sc);
    }

    private void initFragmentList() {
        fragmentList = new ArrayList<Fragment>();
        musicInfo = new MusicInfo();
        fragmentList.add(musicInfo);
    }

    //点击事件监听器
    private class OnClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == imageViewSearch) {
                Toast.makeText(MainActivity.this, "这个功能还没做", Toast.LENGTH_SHORT).show();
            } else if (v == textViewMusicList) {
                new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, MusicSort.class));
                    }
                }.run();
            } else if (v == textViewPlayList) {
                new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, PlayList.class));
                    }
                }.run();
            } else if (v == imageViewMenu) {
                new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, Settings.class));
                    }
                }.run();
            }
        }
    }

    //ViewPagerAdapter
    private class MusicInfoViewPagerAdapter extends FragmentPagerAdapter {
        public MusicInfoViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }
}
