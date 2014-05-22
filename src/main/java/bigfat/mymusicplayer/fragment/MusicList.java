package bigfat.mymusicplayer.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import bigfat.mymusicplayer.MainActivity;
import bigfat.mymusicplayer.R;
import bigfat.mymusicplayer.dialog.PlayListDialog;
import bigfat.mymusicplayer.util.DBUtil;
import bigfat.mymusicplayer.util.FileUtil;
import bigfat.mymusicplayer.widget.IndexBar;
import bigfat.mymusicplayer.widget.MyListView;

/**
 * Created by bigfat on 2014/5/8.
 */
public class MusicList extends Fragment {
    MusicListAdapter musicListAdapter;
    //控件
    private MyListView myListViewMusicList;
    private RelativeLayout relativeLayoutMusicListAction;
    private RelativeLayout relativeLayoutPlayListAction;
    private Button buttonMusicListAdd;
    private Button buttonMusicListDelete;
    private IndexBar indexBarMusicList;
    //取消操作和全选音乐列表
    private RelativeLayout relativeLayoutMusicListControl;
    private Button buttonMusicListActionCancel;
    private Button buttonMusicListChoseAll;
    //类别信息
    private FileUtil.SortKey sortKey;
    private String keyStr;
    //列表数据
    private ArrayList<HashMap<String, String>> musicList;
    //是否是选择模式
    private boolean isSelectedMode = false;
    //CheckBox选择情况
    private boolean[] checkArray;
    //是否是全选状态
    private boolean isAllSelected = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_musiclist, null);
        //获取传递的数据
        Bundle b = getArguments();
        sortKey = FileUtil.SortKey.valueOf(b.getString("sortKey"));
        keyStr = b.getString("keyStr");
        //绑定控件
        myListViewMusicList = (MyListView) view.findViewById(R.id.myListViewMusicList);
        relativeLayoutMusicListControl = (RelativeLayout) view.findViewById(R.id.relativeLayoutMusicListControl);
        relativeLayoutMusicListAction = (RelativeLayout) view.findViewById(R.id.relativeLayoutMusicListAction);
        relativeLayoutPlayListAction = (RelativeLayout) view.findViewById(R.id.relativeLayoutPlayListAction);
        indexBarMusicList = (IndexBar) view.findViewById(R.id.indexBarMusicList);
        buttonMusicListActionCancel = (Button) view.findViewById(R.id.buttonMusicListActionCancel);
        buttonMusicListChoseAll = (Button) view.findViewById(R.id.buttonMusicListChoseAll);
        buttonMusicListAdd = (Button) view.findViewById(R.id.buttonMusicListAdd);
        buttonMusicListDelete = (Button) view.findViewById(R.id.buttonMusicListDelete);
        //设置监听器
        OnClickEvent onClickEvent = new OnClickEvent();
        buttonMusicListActionCancel.setOnClickListener(onClickEvent);
        buttonMusicListChoseAll.setOnClickListener(onClickEvent);
        buttonMusicListAdd.setOnClickListener(onClickEvent);
        buttonMusicListDelete.setOnClickListener(onClickEvent);
        //开启线程配置ListView
        new Runnable() {
            @Override
            public void run() {
                initMusicList();
                //初始化ListView适配器
                musicListAdapter = new MusicListAdapter();
                myListViewMusicList.setTitle(LayoutInflater.from(getActivity()).inflate(R.layout.list_item_tag, myListViewMusicList, false));
                myListViewMusicList.setAdapter(musicListAdapter);
                myListViewMusicList.setOnScrollListener(musicListAdapter);
                //初始化IndexBar
                indexBarMusicList.setListView(myListViewMusicList);
                myListViewMusicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        if (isSelectedMode) {
                            checkArray[position] = !checkArray[position];
                            musicListAdapter.notifyDataSetChanged();
                        } else {
                            new Runnable() {
                                @Override
                                public void run() {
                                    //播放该列表中position位置的歌曲
                                    MainActivity.getMusicBinder().playMusicList(musicList, position);
                                    //返回播放界面
                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                }
                            }.run();
                        }
                    }
                });
                myListViewMusicList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        if (!isSelectedMode) {
                            isSelectedMode = true;
                            checkArray[position] = true;
                            musicListAdapter.notifyDataSetChanged();
                        }
                        return true;
                    }
                });
            }
        }.run();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        musicListAdapter.notifyDataSetChanged();
    }

    //获取列表数据
    private void initMusicList() {
        musicList = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = DBUtil.getReadableDB(getActivity(), DBUtil.databaseName);
            switch (sortKey) {
                case All:
                    cursor = DBUtil.rawQueryCursor(db, "select path,title,pinyin,album,artist from " + DBUtil.T_MusicFile_Name + " order by pinyin", null);
                    break;
                case Folder:
                    cursor = DBUtil.rawQueryCursor(db, "select path,title,pinyin,album,artist from " + DBUtil.T_MusicFile_Name + " where folder='" + keyStr.replace("'", "''") + "' order by pinyin", null);
                    break;
                case Album:
                    cursor = DBUtil.rawQueryCursor(db, "select path,title,pinyin,album,artist from " + DBUtil.T_MusicFile_Name + " where album='" + keyStr.replace("'", "''") + "' order by pinyin", null);
                    break;
                case Artist:
                    cursor = DBUtil.rawQueryCursor(db, "select path,title,pinyin,album,artist from " + DBUtil.T_MusicFile_Name + " where artist='" + keyStr.replace("'", "''") + "' order by pinyin", null);
                    break;
                case PlayList:
                    cursor = DBUtil.rawQueryCursor(db, "select path,title,pinyin,album,artist,playlist from " + DBUtil.T_PlayListFile_Name + " where playlist=" + keyStr.replace("'", "''") + " order by pinyin", null);
                    break;
            }
            final Cursor cursorFinal = cursor;
            if (cursorFinal != null) {
                while (cursorFinal.moveToNext()) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("path", cursorFinal.getString(cursorFinal.getColumnIndex("path")));
                    map.put("title", cursorFinal.getString(cursorFinal.getColumnIndex("title")));
                    map.put("pinyin", cursorFinal.getString(cursorFinal.getColumnIndex("pinyin")));
                    map.put("album", cursorFinal.getString(cursorFinal.getColumnIndex("album")));
                    map.put("artist", cursorFinal.getString(cursorFinal.getColumnIndex("artist")));
                    if (sortKey == FileUtil.SortKey.PlayList) {
                        map.put("playlist", cursorFinal.getString(cursorFinal.getColumnIndex("playlist")));
                    }
                    musicList.add(map);
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
        checkArray = new boolean[musicList.size()];
        for (int i = 0; i < checkArray.length; i++) {
            checkArray[i] = false;
        }
    }

    //获取已选歌曲列表
    private ArrayList<HashMap<String, String>> getSelectedMusicList() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < checkArray.length; i++) {
            if (checkArray[i]) {
                list.add(musicList.get(i));
            }
        }
        return list;
    }

    private class OnClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == buttonMusicListActionCancel) {
                new Runnable() {
                    @Override
                    public void run() {
                        isSelectedMode = false;
                        for (int i = 0; i < checkArray.length; i++) {
                            checkArray[i] = false;
                        }
                        musicListAdapter.notifyDataSetChanged();
                    }
                }.run();
            } else if (v == buttonMusicListChoseAll) {
                new Runnable() {
                    @Override
                    public void run() {
                        isAllSelected = !isAllSelected;
                        if (isAllSelected) {
                            for (int i = 0; i < checkArray.length; i++) {
                                checkArray[i] = true;
                            }
                        } else {
                            for (int i = 0; i < checkArray.length; i++) {
                                checkArray[i] = false;
                            }
                        }
                        musicListAdapter.notifyDataSetChanged();
                    }
                }.run();
            } else if (v == buttonMusicListAdd) {
                new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<HashMap<String, String>> list = getSelectedMusicList();
                        PlayListDialog dialog = new PlayListDialog(getActivity(), list);
                        //改变dialog位置与大小
//                        Window dialogWindow = dialog.getWindow();
//                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//                        DisplayMetrics dm = new DisplayMetrics();
//                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//                        lp.width = dm.widthPixels;
//                        dialogWindow.setGravity(Gravity.BOTTOM);

                        dialog.setTitle("添加到");
                        dialog.show();
                    }
                }.run();
            } else if (v == buttonMusicListDelete) {
                new Runnable() {
                    @Override
                    public void run() {
                        //获取已选歌曲列表
                        ArrayList<HashMap<String, String>> list = getSelectedMusicList();
                        //拼接sql语句
                        String[] sql = new String[list.size()];
                        for (int i = 0; i < sql.length; i++) {
                            sql[i] = ("delete from " + DBUtil.T_PlayListFile_Name + " where path='" + list.get(i).get("path").replace("'", "''") + "' and playlist=" + list.get(i).get("playlist"));
                        }
                        DBUtil.execSqlDatabase(getActivity(), DBUtil.databaseName, sql);
                        Toast.makeText(getActivity(), "删除了" + list.size() + "首歌曲", Toast.LENGTH_SHORT).show();
                        initMusicList();
                        musicListAdapter.notifyDataSetChanged();
                    }
                }.run();
            }
        }
    }

    public class MusicListAdapter extends BaseAdapter implements SectionIndexer, AbsListView.OnScrollListener {
        private char[] indexChar = {'#', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        private ArrayList<Character> indexCharList;
        private ArrayList<Integer> indexIntList;

        private ViewHolder holder;

        public MusicListAdapter() {
            initIndexChar();
        }

        private void initIndexChar() {
            indexCharList = new ArrayList<Character>();
            indexIntList = new ArrayList<Integer>();
            indexCharList.add('#');
            indexIntList.add(0);
            int x = 0;
            for (int i = 0; i < getCount(); i++) {
                if (musicList.get(i).get("pinyin").charAt(0) != indexCharList.get(x)) {
                    indexCharList.add(musicList.get(i).get("pinyin").charAt(0));
                    x++;
                    indexIntList.add(i);
                }
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_music, null);
                holder.textViewListItemTag = (TextView) convertView.findViewById(R.id.textViewListItemTag);
                holder.textViewMusicItem = (TextView) convertView.findViewById(R.id.textViewMusicItem);
                holder.checkBoxMusicItem = (CheckBox) convertView.findViewById(R.id.checkBoxMusicItem);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textViewMusicItem.setText(musicList.get(position).get("title"));

            //判断当前位置的索引字母与前一个是否相同，若不同，则当前位置为其索引字母下的第一个，显示当前位置的索引字母
            char cur = musicList.get(position).get("pinyin").charAt(0);
            char pre = position - 1 >= 0 ? musicList.get(position - 1).get("pinyin").charAt(0) : '@';
            if (cur != pre) {
                holder.textViewListItemTag.setText(String.valueOf(indexCharList.get(indexIntList.indexOf(position))));
                holder.textViewListItemTag.setVisibility(View.VISIBLE);
            } else {
                holder.textViewListItemTag.setVisibility(View.GONE);
            }
            if (isSelectedMode) {
                holder.checkBoxMusicItem.setVisibility(View.VISIBLE);
                holder.checkBoxMusicItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myListViewMusicList.performItemClick(null, position, position);
                    }
                });
                holder.checkBoxMusicItem.setChecked(checkArray[position]);
            } else {
                holder.checkBoxMusicItem.setVisibility(View.GONE);
            }
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return musicList.get(position);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            //如果列表有变化，要重新初始化索引列表
            if (indexChar.length != getCount()) {
                initIndexChar();
            }
            //判断是否为选择状态
            if (isSelectedMode) {
                relativeLayoutMusicListControl.setVisibility(View.VISIBLE);
                //判断是否是播放列表界面
                if (sortKey == FileUtil.SortKey.PlayList) {
                    relativeLayoutPlayListAction.setVisibility(View.VISIBLE);
                } else {
                    relativeLayoutMusicListAction.setVisibility(View.VISIBLE);
                }
            } else {
                relativeLayoutMusicListControl.setVisibility(View.GONE);
                if (sortKey == FileUtil.SortKey.PlayList) {
                    relativeLayoutPlayListAction.setVisibility(View.GONE);
                } else {
                    relativeLayoutMusicListAction.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getCount() {
            return musicList.size();
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
        public int getPositionForSection(int section) {
            for (int i = 0; i < indexCharList.size(); i++) {
                if (indexChar[section] == indexCharList.get(i)) {
                    return indexIntList.get(i);
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            char c = musicList.get(position).get("pinyin").charAt(0);
            for (int i = 0; i < indexChar.length; i++) {
                if (c == indexChar[i]) {
                    return i;
                }
            }
            return -1;
        }

        public int getTitleState(int position) {
            if (position < 0 || getCount() == 0) {
                return 0;
            }
            int section = getSectionForPosition(position);
            if (section == -1 || section > indexChar.length) {
                return 0;
            }
            int currentCharIndex = indexCharList.indexOf(indexChar[section]);
            //如果索引Char列表内有当前位置Char索引，且当前Char索引不是索引Char列表内最后一个，且索引Int列表内下一个索引的位置在ListView中刚好在当前Char索引位置的后一个，则返回2：索引滑动效果
            if (currentCharIndex != -1 && currentCharIndex != indexCharList.size() - 1 && position == indexIntList.get(currentCharIndex + 1) - 1) {
                return 2;
            }
            return 1;
        }

        public void setTitleText(View mHeader, int firstVisiblePosition) {
            String title = String.valueOf(musicList.get(firstVisiblePosition).get("pinyin").charAt(0));
            TextView sectionHeader = (TextView) mHeader;
            sectionHeader.setText(title);
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (view instanceof MyListView) {
                ((MyListView) view).titleLayout(firstVisibleItem);
            }
        }

        private final class ViewHolder {
            TextView textViewListItemTag;
            TextView textViewMusicItem;
            CheckBox checkBoxMusicItem;
        }
    }
}
