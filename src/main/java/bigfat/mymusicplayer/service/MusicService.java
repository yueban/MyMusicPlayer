package bigfat.mymusicplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import bigfat.mymusicplayer.R;
import bigfat.mymusicplayer.util.CodeUtil;
import bigfat.mymusicplayer.util.DBUtil;
import bigfat.mymusicplayer.util.TimeUitl;

/**
 * Created by bigfat on 2014/5/4.
 */
public class MusicService extends Service {
    //状态量
    public static String title;
    public static String album;
    public static String artist;
    public static boolean isFileLoaded = false;
    public static boolean isPlaying = false;
    //接收通话状态变化的BroadcastReceiver
    private BroadcastReceiver phoneStatusChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.PHONE_STATE")) {
                if (isFileLoaded) {
                    if (isPlaying) {
                        pauseMusic();
                    }
                    TelephonyManager tm = (TelephonyManager) MusicService.this.getSystemService(Service.TELEPHONY_SERVICE);
                    tm.listen(new PhoneStateListener() {
                        @Override
                        public void onCallStateChanged(int state, String incomingNumber) {
                            super.onCallStateChanged(state, incomingNumber);
                            switch (state) {
                                case TelephonyManager.CALL_STATE_IDLE:
                                    if (!isPlaying) {
                                        startMusic();
                                    }
                                    break;
                            }
                        }
                    }, PhoneStateListener.LISTEN_CALL_STATE);
                }
            }
        }
    };
    public static boolean isSeekBarChanging = false;
    private boolean isFavorite;
    //MusicPlayer模块上的控件
    private ImageView imageViewPlayMusic;
    private ImageView imageViewPreviousMusic;
    private ImageView imageViewNextMusic;
    private ImageView imageViewMusicPlayMode;
    private ImageView imageViewMusicControlFavorite;
    private SeekBar seekBarMusic;
    private TextView textViewPlayTimeTotal;
    //音乐播放组件
    private MediaPlayer mp;
    private MusicBinder musicBinder;
    private int playmodeCurrent = 0;
    //正在播放的歌曲列表
    private ArrayList<HashMap<String, String>> musicList = null;
    private int position;
    //播放顺序图片
    private int[] playmodeImgRes = {R.drawable.playmode_default,
            R.drawable.playmode_list_repeat,
            R.drawable.playmode_single_repeat,
            R.drawable.playmode_random};

    @Override
    public void onCreate() {
        super.onCreate();
        musicBinder = new MusicBinder();
        mp = new MediaPlayer();
        //注册接收通话状态变化的Receiver
        registerReceiver(phoneStatusChangedReceiver, new IntentFilter("android.intent.action.PHONE_STATE"));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    private void pauseMusic() {
        new Runnable() {
            @Override
            public void run() {
                mp.pause();
                isPlaying = false;
                imageViewPlayMusic.setImageResource(R.drawable.music_control_play);
            }
        }.run();
    }

    private void startMusic() {
        new Runnable() {
            @Override
            public void run() {
                mp.seekTo(seekBarMusic.getProgress());
                mp.start();
                isPlaying = true;
                imageViewPlayMusic.setImageResource(R.drawable.music_control_pause);
            }
        }.run();
    }

    private void playMusic(final String fileAbsolutePath) {
        new Runnable() {
            @Override
            public void run() {
                mp.reset();
                mp = MediaPlayer.create(MusicService.this, Uri.parse(fileAbsolutePath));
                isFileLoaded = true;
                mp.start();
                isPlaying = true;
                title = musicList.get(position).get("title");
                album = musicList.get(position).get("album");
                artist = musicList.get(position).get("artist");
                sendBroadcast(new Intent(CodeUtil.MUSIC_CHANGE_ACTION));
                musicBinder.refreshView();
                //播放完成后的动作
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        isFileLoaded = false;
                        isPlaying = false;
                        switch (playmodeCurrent) {
                            case 0://顺序播放
                                if (position == musicList.size() - 1) {
                                    mp.release();
                                    Toast.makeText(MusicService.this, "当前列表已播放完毕", Toast.LENGTH_SHORT).show();
                                } else {
                                    playMusic(musicList.get(++position).get("path"));
                                }
                                break;
                            case 1://列表循环
                                if (position == musicList.size() - 1) {
                                    position = 0;
                                    playMusic(musicList.get(position).get("path"));
                                } else {
                                    playMusic(musicList.get(++position).get("path"));
                                }
                                break;
                            case 2://单曲循环
                                playMusic(musicList.get(position).get("path"));
                                break;
                            case 3://随机播放
                                position = (int) (Math.random() * musicList.size());
                                playMusic(musicList.get(position).get("path"));
                                break;
                        }
                    }
                });
            }
        }.run();
    }

    private void playMusicList(ArrayList<HashMap<String, String>> musicList, int position) {
        this.musicList = musicList;
        this.position = position;
        playMusic(musicList.get(position).get("path"));
    }

    private void playDefaultMusicList(int position) {
        this.position = position;
        playMusic(musicList.get(position).get("path"));
    }

    //Button点击事件监听器
    class OnClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == imageViewPlayMusic) {
                if (isFileLoaded) {
                    if (isPlaying) {
                        pauseMusic();
                    } else {
                        startMusic();
                    }
                } else {
                    new Runnable() {
                        @Override
                        public void run() {
                            SQLiteDatabase db = null;
                            Cursor cursor = null;
                            try {
                                db = DBUtil.getReadableDB(MusicService.this, DBUtil.databaseName);
                                cursor = DBUtil.rawQueryCursor(db, "select path,title,pinyin,album,artist from " + DBUtil.T_MusicFile_Name, null);
                                musicList = new ArrayList<HashMap<String, String>>();
                                while (cursor.moveToNext()) {
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put("path", cursor.getString(cursor.getColumnIndex("path")));
                                    map.put("title", cursor.getString(cursor.getColumnIndex("title")));
                                    map.put("pinyin", cursor.getString(cursor.getColumnIndex("pinyin")));
                                    map.put("album", cursor.getString(cursor.getColumnIndex("album")));
                                    map.put("artist", cursor.getString(cursor.getColumnIndex("artist")));
                                    musicList.add(map);
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
                            if (musicList.size() > 0) {
                                playDefaultMusicList((int) (Math.random() * musicList.size()));
                            } else {
                                Toast.makeText(MusicService.this, "未检索到任何歌曲", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.run();
                }
            } else if (v == imageViewPreviousMusic) {
                if (isFileLoaded) {
                    switch (playmodeCurrent) {
                        case 0://顺序播放
                            if (position == 0) {
                                Toast.makeText(MusicService.this, "已至播放列表头部", Toast.LENGTH_SHORT).show();
                            } else {
                                playMusic(musicList.get(--position).get("path"));
                            }
                            break;
                        case 1://列表循环
                        case 2://单曲循环
                            if (position == 0) {
                                position = musicList.size() - 1;
                                playMusic(musicList.get(position).get("path"));
                            } else {
                                playMusic(musicList.get(--position).get("path"));
                            }
                            break;
                        case 3://随机播放
                            position = (int) (Math.random() * musicList.size());
                            playMusic(musicList.get(position).get("path"));
                            break;
                    }
                } else {
                    Toast.makeText(MusicService.this, "当前播放列表中没有任何曲目", Toast.LENGTH_SHORT).show();
                }
            } else if (v == imageViewNextMusic) {
                if (isFileLoaded) {
                    switch (playmodeCurrent) {
                        case 0://顺序播放
                            if (position == musicList.size() - 1) {
                                Toast.makeText(MusicService.this, "已至播放列表尾部", Toast.LENGTH_SHORT).show();
                            } else {
                                playMusic(musicList.get(++position).get("path"));
                            }
                            break;
                        case 1://列表循环
                        case 2://单曲循环
                            if (position == musicList.size() - 1) {
                                position = 0;
                                playMusic(musicList.get(position).get("path"));
                            } else {
                                playMusic(musicList.get(++position).get("path"));
                            }
                            break;
                        case 3://随机播放
                            position = (int) (Math.random() * musicList.size());
                            playMusic(musicList.get(position).get("path"));
                            break;
                    }
                } else {
                    Toast.makeText(MusicService.this, "当前播放列表中没有任何曲目", Toast.LENGTH_SHORT).show();
                }
            } else if (v == imageViewMusicPlayMode) {
                playmodeCurrent = playmodeCurrent == 3 ? 0 : ++playmodeCurrent;
                imageViewMusicPlayMode.setImageResource(playmodeImgRes[playmodeCurrent]);
            } else if (v == imageViewMusicControlFavorite) {
                if (isFileLoaded) {
                    if (isFavorite) {
                        DBUtil.execSqlDatabase(MusicService.this, DBUtil.databaseName, "update " + DBUtil.T_MusicFile_Name + " set favorite=0 where path='" + musicList.get(position).get("path").replace("'", "''") + "'");
                        isFavorite = false;
                        imageViewMusicControlFavorite.setImageResource(android.R.drawable.ic_menu_search);
                        Toast.makeText(MusicService.this, "从我的喜爱中移除", Toast.LENGTH_SHORT).show();
                    } else {
                        DBUtil.execSqlDatabase(MusicService.this, DBUtil.databaseName, "update " + DBUtil.T_MusicFile_Name + " set favorite=1 where path='" + musicList.get(position).get("path").replace("'", "''") + "'");
                        isFavorite = true;
                        imageViewMusicControlFavorite.setImageResource(R.drawable.music_love);
                        Toast.makeText(MusicService.this, "添加到我的喜爱", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public class MusicBinder extends Binder {
        public void playMusicList(ArrayList<HashMap<String, String>> musicList, int position) {
            MusicService.this.playMusicList(musicList, position);
        }

        public void refreshView() {
            imageViewMusicPlayMode.setImageResource(playmodeImgRes[playmodeCurrent]);
            if (isFileLoaded) {
                int duration = mp.getDuration();
                seekBarMusic.setMax(duration);
                seekBarMusic.setProgress(mp.getCurrentPosition());
                textViewPlayTimeTotal.setText(TimeUitl.changeMillsToDateTime(duration));
                if (isPlaying) {
                    imageViewPlayMusic.setImageResource(R.drawable.music_control_pause);
                } else {
                    imageViewPlayMusic.setImageResource(R.drawable.music_control_play);
                }
                //获取该歌曲是否被设置为喜欢
                SQLiteDatabase db = null;
                Cursor cursor = null;
                try {
                    db = DBUtil.getReadableDB(MusicService.this, DBUtil.databaseName);
                    cursor = DBUtil.rawQueryCursor(db, "select favorite from " + DBUtil.T_MusicFile_Name + " where path='" + musicList.get(position).get("path").replace("'", "''") + "'", null);
                    if (cursor.moveToNext()) {
                        if (cursor.getString(cursor.getColumnIndex("favorite")).equals("1")) {
                            isFavorite = true;
                            imageViewMusicControlFavorite.setImageResource(R.drawable.music_love);
                        } else {
                            isFavorite = false;
                            imageViewMusicControlFavorite.setImageResource(android.R.drawable.ic_menu_search);
                        }
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
        }

        public int getMPCurrentPosition() {
            return mp.getCurrentPosition();
        }

        public void setView(final ImageView _imageViewPlayMusic, final ImageView _imageViewPreviousMusic, final ImageView _imageViewNextMusic, final ImageView _imageViewMusicPlayMode, final ImageView _imageViewMusicControlFavorite, final SeekBar _seekBarMusic, final TextView _textViewPlayTimeNow, final TextView _textViewPlayTimeTotal) {
            imageViewPlayMusic = _imageViewPlayMusic;
            imageViewPreviousMusic = _imageViewPreviousMusic;
            imageViewNextMusic = _imageViewNextMusic;
            imageViewMusicPlayMode = _imageViewMusicPlayMode;
            imageViewMusicControlFavorite = _imageViewMusicControlFavorite;
            seekBarMusic = _seekBarMusic;
            textViewPlayTimeTotal = _textViewPlayTimeTotal;
            //设置监听器
            OnClickEvent onClickEvent = new OnClickEvent();
            imageViewPlayMusic.setOnClickListener(onClickEvent);
            imageViewPreviousMusic.setOnClickListener(onClickEvent);
            imageViewNextMusic.setOnClickListener(onClickEvent);
            imageViewMusicPlayMode.setOnClickListener(onClickEvent);
            imageViewMusicControlFavorite.setOnClickListener(onClickEvent);
            seekBarMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    _textViewPlayTimeNow.setText(TimeUitl.changeMillsToDateTime(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    isSeekBarChanging = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isSeekBarChanging = false;
                    if (isPlaying) {
                        mp.seekTo(seekBar.getProgress());
                    }
                }
            });
        }
    }
}