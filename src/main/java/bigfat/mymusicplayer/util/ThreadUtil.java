package bigfat.mymusicplayer.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bigfat on 2014/5/7.
 */
public class ThreadUtil {
    public static class DatabaseAsyncTask extends AsyncTask<String, Integer, String> {
        private Context context;
        private ProgressDialog progressDialog;
        private ArrayList<HashMap<String, String>> musicFileList = new ArrayList<HashMap<String, String>>();
        private ArrayList<String> pathList = new ArrayList<String>();
        private ArrayList<String> fileNameList = new ArrayList<String>();
        private boolean isFirstTime;

        public DatabaseAsyncTask(Context context, boolean isFirstTime) {
            this.context = context;
            this.isFirstTime = isFirstTime;
            //不加这一句，读取音乐文件的Tag会大范围乱码
            TagOptionSingleton.getInstance().setAndroid(true);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("正在搜索歌曲");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(1);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            //如果不是第一次检索歌曲信息，则需清空数据
            if (!isFirstTime) {
                DBUtil.execSqlDatabase(context, DBUtil.databaseName, new String[]{
                        "drop table " + DBUtil.T_MusicFile_Name,
                        "drop table " + DBUtil.T_PlayListFile_Name,
                        "drop table " + DBUtil.T_PlayList_Name});
            }
            //读取歌曲文件路径
            getAllMusicFilePath(params[0]);
            //读取歌曲文件信息，存入musicList列表
            for (int i = 0; i < pathList.size(); i++) {
                String path = pathList.get(i);
                String fileName = fileNameList.get(i);
                String db_title = fileName.substring(0, fileName.lastIndexOf("."));
                String db_album = "(unknown)";
                String db_artist = "(unknown)";
                try {
                    final AudioFile audioFile = AudioFileIO.read(new File(path + fileName));
                    final Tag tag = audioFile.getTag();
                    if (tag != null) {
                        final String titleTemp = tag.getFirst(FieldKey.TITLE);
                        final String albumTemp = tag.getFirst(FieldKey.ALBUM);
                        final String artistTemp = tag.getFirst(FieldKey.ARTIST);
                        if (!titleTemp.trim().equals("")) {
                            db_title = titleTemp;
                        }
                        if (!albumTemp.trim().equals("")) {
                            db_album = albumTemp;
                        }
                        if (!artistTemp.trim().equals("")) {
                            db_artist = artistTemp;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("--->Something wrong with " + fileName);
                    e.printStackTrace();
                }
                //音乐文件基本信息
                String db_path = path + fileName;
                String db_folder = path.substring(0, path.length() - 1);
                String db_pinyin = HanZiToPinYin.toUpperPinYin(db_title);
                char c = db_pinyin.charAt(0);
                if (c < 'A' || c > 'Z') {
                    db_pinyin = "#" + db_pinyin;
                }
                //将要存入数据库中的信息加入musicFileList
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("path", db_path);
                map.put("title", db_title);
                map.put("pinyin", db_pinyin);
                map.put("folder", db_folder);
                map.put("album", db_album);
                map.put("artist", db_artist);
                musicFileList.add(map);
                publishProgress(i);
            }
            //读取musicFileList中的信息，拼接sql语句
            String[] sql = new String[musicFileList.size()];
            for (int i = 0; i < musicFileList.size(); i++) {
                HashMap<String, String> map = musicFileList.get(i);
                sql[i] = "insert or ignore into " + DBUtil.T_MusicFile_Name +
                        " (path,title,pinyin,folder,album,artist,favorite)values('" +
                        map.get("path").replace("'", "''") + "','" +
                        map.get("title").replace("'", "''") + "','" +
                        map.get("pinyin").replace("'", "''") + "','" +
                        map.get("folder").replace("'", "''") + "','" +
                        map.get("album").replace("'", "''") + "','" +
                        map.get("artist").replace("'", "''") + "',0)";
            }
            try {
                DBUtil.execSqlDatabase(context, DBUtil.databaseName, sql);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "歌曲更新时出错，如果您方便的话，请联系作者：18607006059", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == 0) {
                progressDialog.setMax(pathList.size());
            } else if (values[0] == pathList.size()) {
                progressDialog.setTitle("存储歌曲信息");
            } else {
                progressDialog.setProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            Toast.makeText(context, "歌曲更新完毕", Toast.LENGTH_SHORT).show();
        }

        //获取所有歌曲文件路径
        private void getAllMusicFilePath(String path) {
            try {
                String[] fileList = new File(path).list();
                for (String fileListItem : fileList) {
                    if (FileUtil.isDir(path + fileListItem)) {
                        if (FileUtil.isAccessDir(path + fileListItem)) {
                            getAllMusicFilePath(path + fileListItem + File.separator);
                        }
                    } else if (fileListItem.contains(".")) {
                        String fileType = fileListItem.substring(fileListItem.lastIndexOf(".") + 1, fileListItem.length());
                        //读取音乐文件Tag信息
                        if (fileType.equals("mp3") || fileType.equals("wav") || fileType.equals("flac") || fileType.equals("ape")) {
                            pathList.add(path);
                            fileNameList.add(fileListItem);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}