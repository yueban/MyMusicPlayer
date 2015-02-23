package bigfat.mymusicplayer.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by bigfat on 2014/5/28.
 */
public class ToastUtil {

    private static Handler handler = new Handler(Looper.getMainLooper());

    private static Toast toast = null;

    private static Object synObj = new Object();

    public static void showMessage(final Context act, final String msg) {
        showMessage(act, msg, Toast.LENGTH_SHORT);
    }

    public static void showMessage(final Context act, final int msg) {
        showMessage(act, msg, Toast.LENGTH_SHORT);
    }

    public static void showMessage(final Context act, final String msg,
                                   final int len) {
        new Thread(new Runnable() {
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (toast != null) {
                            toast.setText(msg);
                        } else {
                            toast = Toast.makeText(act, msg, len);
                        }
                        toast.show();
                    }
                });
            }
        }).start();
    }


    public static void showMessage(final Context act, final int msg,
                                   final int len) {
        new Thread(new Runnable() {
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (toast != null) {
                            toast.setText(msg);
                        } else {
                            toast = Toast.makeText(act, msg, len);
                        }
                        toast.show();
                    }
                });
            }
        }).start();
    }

}
