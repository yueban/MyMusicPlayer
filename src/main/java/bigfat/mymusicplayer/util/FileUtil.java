package bigfat.mymusicplayer.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by bigfat on 2014/5/4.
 */
public class FileUtil {
    private static final String SDPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    private static final String ROOTPATH = "/storage/";

    public static String getRootPath() {
        return ROOTPATH;
    }

    public static String getSDPath() {
        return SDPATH;
    }

    public static boolean isDir(String dirPath) {
        File file = new File(dirPath);
        return file.isDirectory();
    }

    public static boolean isMusicFile(String fileName) {
        String endStr = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        return endStr.equals("mp3") || endStr.equals("ape") || endStr.equals("flac") || endStr.equals("wav");
    }

    public static boolean isAccessDir(String dirPath) {
        //如果不是sdcard目录（既不是手机存储目录，也不是sd卡存储目录），返回false
        if (!dirPath.contains("sdcard")) {
            return false;
        }
        //系统文件夹返回false
        if (dirPath.contains(".android_secure")) {
            return false;
        }
        return true;
    }

    public static enum SortKey {
        All, Folder, Album, Artist, PlayList
    }
}
