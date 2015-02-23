package bigfat.mymusicplayer.util;

import java.text.SimpleDateFormat;

/**
 * Created by bigfat on 2014/5/12.
 */
public class TimeUitl {
    public static String changeMillsToDateTime(int millsTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        return simpleDateFormat.format(millsTime);
    }
}
