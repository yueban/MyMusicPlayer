package bigfat.mymusicplayer.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import bigfat.mymusicplayer.R;
import bigfat.mymusicplayer.service.MusicService;

/**
 * Created by bigfat on 2014/5/21.
 */
public class MusicInfo extends Fragment {
    private TextView textViewMusicTitle;
    //    private TextView textViewMusicAlbum;
    private TextView textViewMusicArtist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_musicinfo, null);
        textViewMusicTitle = (TextView) view.findViewById(R.id.textViewMusicTitle);
//        textViewMusicAlbum = (TextView) view.findViewById(R.id.textViewMusicAlbum);
        textViewMusicArtist = (TextView) view.findViewById(R.id.textViewMusicArtist);
        showMusicInfo();
        return view;
    }

    public void showMusicInfo() {
        if (MusicService.isFileLoaded) {
            textViewMusicTitle.setVisibility(View.VISIBLE);
//            textViewMusicAlbum.setVisibility(View.VISIBLE);
            textViewMusicArtist.setVisibility(View.VISIBLE);
            textViewMusicTitle.setText(MusicService.title);
//            textViewMusicAlbum.setText(MusicService.album);
            textViewMusicArtist.setText(MusicService.artist);
        } else {
            textViewMusicTitle.setVisibility(View.GONE);
//            textViewMusicAlbum.setVisibility(View.GONE);
            textViewMusicArtist.setVisibility(View.GONE);
        }
    }
}
