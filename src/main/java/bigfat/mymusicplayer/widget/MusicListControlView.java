package bigfat.mymusicplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by bigfat on 2014/5/21.
 */
public class MusicListControlView extends View {
    public MusicListControlView(Context context) {
        super(context);
    }

    public MusicListControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MusicListControlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
