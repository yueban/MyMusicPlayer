package bigfat.mymusicplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import bigfat.mymusicplayer.fragment.MusicList;
import bigfat.mymusicplayer.fragment.SortList;
import bigfat.mymusicplayer.util.FileUtil;

/**
 * Created by bigfat on 2014/5/7.
 */
public class MusicSort extends ActionBarActivity {
    //控件
    private View viewLineAllMusic;
    private View viewLineFolder;
    private View viewLineAlbum;
    private View viewLineArtist;
    //ViewPager中的Fragment列表
    private ArrayList<Fragment> fragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicsort);
        //配置ActionBar
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.musiclist);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        //绑定控件
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPagerMusicList);
        viewLineAllMusic = findViewById(R.id.viewLineAllMusic);
        viewLineFolder = findViewById(R.id.viewLineFolder);
        viewLineAlbum = findViewById(R.id.viewLineAlbum);
        viewLineArtist = findViewById(R.id.viewLineArtist);
        new Runnable() {
            @Override
            public void run() {
                //配置ViewPager
                initFragmentList();
                viewPager.setAdapter(new MusicSortViewPagerAdapter(getSupportFragmentManager()));
                viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        switch (position) {
                            case 0:
                                viewLineAllMusic.setVisibility(View.VISIBLE);
                                viewLineFolder.setVisibility(View.INVISIBLE);
                                viewLineAlbum.setVisibility(View.INVISIBLE);
                                viewLineArtist.setVisibility(View.INVISIBLE);
                                break;
                            case 1:
                                viewLineAllMusic.setVisibility(View.INVISIBLE);
                                viewLineFolder.setVisibility(View.VISIBLE);
                                viewLineAlbum.setVisibility(View.INVISIBLE);
                                viewLineArtist.setVisibility(View.INVISIBLE);
                                break;
                            case 2:
                                viewLineAllMusic.setVisibility(View.INVISIBLE);
                                viewLineFolder.setVisibility(View.INVISIBLE);
                                viewLineAlbum.setVisibility(View.VISIBLE);
                                viewLineArtist.setVisibility(View.INVISIBLE);
                                break;
                            case 3:
                                viewLineAllMusic.setVisibility(View.INVISIBLE);
                                viewLineFolder.setVisibility(View.INVISIBLE);
                                viewLineAlbum.setVisibility(View.INVISIBLE);
                                viewLineArtist.setVisibility(View.VISIBLE);
                                break;
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
            }
        }.run();
    }

    public void initFragmentList() {
        fragmentList = new ArrayList<Fragment>();

        Bundle b1 = new Bundle();
        b1.putString("sortKey", FileUtil.SortKey.All.name());
        b1.putString("keyStr", null);
        MusicList musicList = new MusicList();
        musicList.setArguments(b1);
        fragmentList.add(musicList);

        Bundle b2 = new Bundle();
        b2.putString("sortKey", FileUtil.SortKey.Folder.name());
        SortList sortListFolder = new SortList();
        sortListFolder.setArguments(b2);
        fragmentList.add(sortListFolder);

        Bundle b3 = new Bundle();
        b3.putString("sortKey", FileUtil.SortKey.Album.name());
        SortList sortListAlbum = new SortList();
        sortListAlbum.setArguments(b3);
        fragmentList.add(sortListAlbum);

        Bundle b4 = new Bundle();
        b4.putString("sortKey", FileUtil.SortKey.Artist.name());
        SortList sortListArtist = new SortList();
        sortListArtist.setArguments(b4);
        fragmentList.add(sortListArtist);
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

    class MusicSortViewPagerAdapter extends FragmentPagerAdapter {
        public MusicSortViewPagerAdapter(FragmentManager fm) {
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

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);
        }
    }
}
