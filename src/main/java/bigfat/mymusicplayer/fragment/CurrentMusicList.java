package bigfat.mymusicplayer.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import bigfat.mymusicplayer.MainActivity;
import bigfat.mymusicplayer.R;
import bigfat.mymusicplayer.service.MusicService;

/**
 * Created by bigfat on 2014/5/21.
 */
public class CurrentMusicList extends Fragment {
    private ListView listView;
    private ArrayList<HashMap<String, String>> musicList;
    private MyListViewAdapter adapter;
    private MusicService.MusicBinder musicBinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_currentmusiclist, null);
        listView = (ListView) view.findViewById(R.id.listViewCurrentMusicList);
        musicList = new ArrayList<HashMap<String, String>>();
        adapter = new MyListViewAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                musicBinder.playMusicList(position);
                adapter.notifyDataSetChanged();
            }
        });
        showCurrentMusicList();
        return view;
    }

    public void showCurrentMusicList() {
        if (musicBinder == null) {
            musicBinder = MainActivity.getMusicBinder();
        }
        if (MusicService.isFileLoaded) {
            musicList = musicBinder.getCurrentMusicList();
            adapter.notifyDataSetChanged();
            listView.setSelection(MusicService.position - 4);
        }
    }

    private class MyListViewAdapter extends BaseAdapter {
        private ViewHolder holder;

        @Override
        public int getCount() {
            return musicList.size();
        }

        @Override
        public Object getItem(int position) {
            return musicList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_music_current, null);
                holder.imageViewIsPlaying = (ImageView) convertView.findViewById(R.id.imageViewIsPlaying);
                holder.textViewMusicTitle = (TextView) convertView.findViewById(R.id.textViewMusicTitle);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (position == MusicService.position) {
                holder.imageViewIsPlaying.setVisibility(View.VISIBLE);
            } else {
                holder.imageViewIsPlaying.setVisibility(View.GONE);
            }
            holder.textViewMusicTitle.setText(musicList.get(position).get("title"));
            return convertView;
        }

        private final class ViewHolder {
            private ImageView imageViewIsPlaying;
            private TextView textViewMusicTitle;
        }
    }
}
