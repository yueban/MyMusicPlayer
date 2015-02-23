package bigfat.mymusicplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SectionIndexer;

/**
 * Created by bigfat on 2014/5/14.
 */
public class IndexBar extends View {
    private ListView listView;
    private char[] index;
    private SectionIndexer sectionIndexer = null;
    private Paint paint = new Paint();

    public IndexBar(Context context) {
        super(context);
        initChar();
    }

    public IndexBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initChar();
    }

    public IndexBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initChar();
    }

    private void initChar() {
        index = new char[]{'#', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    }

    public void setListView(ListView listView) {
        this.listView = listView;
        sectionIndexer = (SectionIndexer) listView.getAdapter();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(0xFFFFFFFF);
        paint.setTextSize(getMeasuredHeight() / 35);
        paint.setTextAlign(Paint.Align.CENTER);
        float widthCenter = getMeasuredWidth() / 2;
        if (index.length > 0) {
            float height = getMeasuredHeight() / index.length;
            for (int i = 0; i < index.length; i++) {
                canvas.drawText(String.valueOf(index[i]), widthCenter, (i + 1) * height, paint);
            }
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int idx = ((int) event.getY()) / (getMeasuredHeight() / index.length);
        if (idx > index.length - 1) {
            idx = index.length - 1;
        } else if (idx < 0) {
            idx = 0;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            setBackgroundColor(Color.parseColor("#25242424"));

            int position = sectionIndexer.getPositionForSection(idx);
            if (position != -1) {
                listView.setSelection(position);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            setBackground(null);
        }
        return true;
    }
}
