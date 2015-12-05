package android.eventurers.com.drawingwithcanvas;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;


public class MainActivity extends Activity implements OnClickListener{

    private String action="action";
    private Button btncirle, btnrect, btnline, btnerase, btnundo, btnredo;

    private float x, x1, length = 0;
    private float y, y1, a, b;
    private float curx, cury;
    private Paint mPaint;
    private static int shapedrawnstatus, erasestatus =0;
    private static int total_objects_created =0;
    private MyView myView;

    private ArrayList<Circle> circles = new ArrayList<Circle>();
    private ArrayList<Rect> rects = new ArrayList<Rect>();
    private ArrayList<Line> lines = new ArrayList<Line>();
    private ArrayList<Shapes> shapesArrayList = new ArrayList<>();
    private ArrayList<Bitmap> drawlist = new ArrayList<>();

    private final int LAYOUT_WIDTH=720;
    private final int LAYOUT_HEIGHT=1000;
    private final String TAG ="Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setListener();
        myView = new MyView(this);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LAYOUT_HEIGHT);
        this.addContentView(myView, params);
    }

    private void init()
    {
        btncirle = (Button) findViewById(R.id.btnforcircle);
        btnrect = (Button) findViewById(R.id.btnforrect);
        btnline = (Button) findViewById(R.id.btnforline);
        btnundo = (Button) findViewById(R.id.btnforundo);
        btnerase = (Button) findViewById(R.id.btnforerase);
        btnredo = (Button) findViewById(R.id.btnforredo);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.parseColor("#EEEEEE"));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(20);
    }

    private void setListener()
    {
        btncirle.setOnClickListener(this);
        btnrect.setOnClickListener(this);
        btnline.setOnClickListener(this);
        btnundo.setOnClickListener(this);
        btnerase.setOnClickListener(this);
        btnredo.setOnClickListener(this);
    }
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btnforcircle:
                action = "circle";
                erasestatus=0;
                break;
            case R.id.btnforrect:
                action="rect";
                erasestatus=0;
                break;
            case R.id.btnforline:
                action="line";
                erasestatus=0;
                break;
            case R.id.btnforundo:
                action="undo";
                myView.undoOperation();
                break;
            case R.id.btnforerase:
                action = "erase";
                erasestatus=1;
                break;
            case R.id.btnforredo:
                action="redo";
                break;
        }
        Log.d(TAG, "Action is :"+action);
    }

    public class MyView extends View {

        private Bitmap mBitmap, cached_bitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;
        Context context;


        public MyView(Context c) {
            super(c);
            Log.d(TAG, "Executing View class Constructor ");
            context = c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            this.setDrawingCacheEnabled(true);                      //Enabling Cache of Canvas
            total_objects_created =0;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {        //Called once when loading the view
            super.onSizeChanged(w, h, oldw, oldh);
            Log.d(TAG, "Executing View class onsizeChanged() ");
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        public void draw(Canvas canvas) {                           //called each time when the invalidate function is called automatically
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4f);
            paint.setColor(Color.RED);
            if (action == "circle")
                canvas.drawCircle(a, b, length, paint);
            if (action == "rect")
                canvas.drawRect(x, y, x1, y1, paint);
            if (action == "line")
                canvas.drawLine(x, y, x1, y1, paint);
            if (action == "erase"){
                for (Rect r : rects)                                                //Printing all the Shapes
                    canvas.drawRect(r.startX, r.startY, r.stopX, r.stopY, paint);
                for (Circle c : circles)
                    canvas.drawCircle(c.startX, c.startY, c.radius, paint);
                for (Line l : lines)
                    canvas.drawLine(l.startX, l.startY, l.stopX, l.stopY, paint);
                canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
                canvas.drawPath(mPath, mPaint);
            }
            if (action == "undo") {                                             //if action is undo, placing all the cached bitmap so it
                int index = total_objects_created;                              //will display like removing last created object to first
                if(total_objects_created >0) {                                  //created object
                    --total_objects_created;

                    for(int i=0; i<total_objects_created;i++) {
                        Bitmap bitmap = drawlist.get(i);
                        if (bitmap != null && !bitmap.isRecycled())
                            canvas.drawBitmap(bitmap, 0, 0, paint);
                    }
                }
                removeShapes();
            }
            if(action == "redo")
            {
            }
            if (shapedrawnstatus == 1 && action != "undo")   //if any shape is drawn then getting the canvas
            {                                                                   //of the drawn object into bitmap and saving in
                this.setDrawingCacheEnabled(true);                              //Arraylist for undo operation
                cached_bitmap = this.getDrawingCache(true);
                if(cached_bitmap != null && !cached_bitmap.isRecycled()) {
                    cached_bitmap.setWidth(LAYOUT_WIDTH);
                    cached_bitmap.setHeight(LAYOUT_HEIGHT);
                    Bitmap copy_cached_bitmap = Bitmap.createBitmap(cached_bitmap);
                    if (copy_cached_bitmap != null && !copy_cached_bitmap.isRecycled()) {
                        drawlist.add(copy_cached_bitmap);
                        Log.d("MainActivity : ","Total object Count : " + total_objects_created + " Bitmap of Obj: " + copy_cached_bitmap);
                    }
                }
                this.destroyDrawingCache();
                total_objects_created = drawlist.size();
            }

            if(action != "erase" && action != "undo"  && erasestatus==0)                //Displaying all available Shapes in canvas
            {
                for (Rect r : rects)
                    canvas.drawRect(r.startX, r.startY, r.stopX, r.stopY, paint);
                for (Circle c : circles)
                    canvas.drawCircle(c.startX, c.startY, c.radius, paint);
                for (Line l : lines)
                    canvas.drawLine(l.startX, l.startY, l.stopX, l.stopY, paint);
            }
        }

        public void undoOperation()
        {
            invalidate();
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int maskedAction = event.getActionMasked();
            switch (maskedAction) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    a = b = shapedrawnstatus = 0;
                    a = x = x1 = event.getX();
                    b = y = y1 = event.getY();
                    if(action == "erase") touch_start(x, y);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    curx = x1 = event.getX();
                    cury = y1 = event.getY();
                    if(action == "erase") touch_move(x1, y1);
                    if (x1 > x || y1 > y) {
                        a = (x1 - (x1 - x) / 2);
                        b = (y1 - (y1 - y) / 2);
                        length = Math.max((x1 - x) / 2, (y1 - y) / 2);
                    }
                    if (x > x1 || y > y1) {
                        a = (x - (x - x1) / 2);
                        b = (y - (y - y1) / 2);
                        length = Math.max((x - x1) / 2, (y - y1) / 2);
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP: {
                    x1 = event.getX();
                    y1 = event.getY();
                    shapedrawnstatus = 1;
                    if(action == "erase") {
                        touch_up();
                        Erase erase = new Erase();
                        Shapes shapes = new Shapes();
                        shapes.setErase(erase);
                        shapesArrayList.add(shapes);
                        Log.d(TAG, "Shape list size :"+shapesArrayList.size()+" Erase Operation");
                    }

                    if(action == "circle") {
                        Circle circle_obj = new Circle(a, b, length);
                        circles.add(circle_obj);
                        Shapes shapes = new Shapes();
                        shapes.setCircle(circle_obj);
                        shapesArrayList.add(shapes);
                        Log.d(TAG, "Shape list size :"+shapesArrayList.size()+" Circle list size:"+circles.size());
                    }
                    if(action == "rect")
                    {
                        Rect rect_obj = new Rect(x,y,x1,y1);
                        rects.add(rect_obj);
                        Shapes shapes = new Shapes();
                        shapes.setRect(rect_obj);
                        shapesArrayList.add(shapes);
                        Log.d(TAG, "Shape list size :"+shapesArrayList.size()+" Rect list size:"+rects.size());
                    }
                    if(action == "line")
                    {
                        Line line_obj = new Line(x,y,x1,y1);
                        lines.add(line_obj);
                        Shapes shapes = new Shapes();
                        shapes.setLine(line_obj);
                        shapesArrayList.add(shapes);
                        Log.d(TAG, "Shape list size :"+shapesArrayList.size()+" Lines list size:"+circles.size());
                    }
                    break;
                }
            }
            invalidate();
            return true;
        }
    }

    //During undo action removing the respective object from list
    private void removeShapes()
    {
        int shapeslist_size = shapesArrayList.size();
        shapeslist_size=shapeslist_size-1;
        if(shapeslist_size>=0)
        {
            Shapes shapes = shapesArrayList.get(shapeslist_size);
            if(shapes.getCircle()!=null)
            {
                shapesArrayList.remove(shapeslist_size);
                Log.d(TAG, "circle list size :" + circles.size());
                circles.remove(circles.size() - 1);
                drawlist.remove(drawlist.size()-1);
            }
            if(shapes.getLine()!=null)
            {
                shapesArrayList.remove(shapeslist_size);
                Log.d(TAG, "Lines list size :" + lines.size());
                lines.remove(lines.size()-1);
                drawlist.remove(drawlist.size()-1);
            }
            if(shapes.getRect()!=null)
            {
                shapesArrayList.remove(shapeslist_size);
                Log.d(TAG, "Rect list size :" + rects.size());
                rects.remove(rects.size()-1);
                drawlist.remove(drawlist.size()-1);
            }
            if(shapes.getErase()!=null)
            {
                shapesArrayList.remove(shapeslist_size);
                drawlist.remove(drawlist.size()-1);
            }
        }
    }
}

class Line extends Shapes
{
    float startX, startY, stopX, stopY;
    public Line(float startX, float startY, float stopX, float stopY)
    {
        this.startX = startX;
        this.startY = startY;
        this.stopX = stopX;
        this.stopY = stopY;
    }
}
class Rect extends Shapes
{
    float startX, startY, stopX, stopY;
    public Rect(float startX, float startY, float stopX, float stopY)
    {

        //added comments
        this.startX = startX;
        this.startY = startY;
        this.stopX = stopX;
        this.stopY = stopY;
    }
}
class Circle extends Shapes
{
    float startX, startY, radius;

    public Circle(float startX, float startY, float radius) {
        this.startX = startX;
        this.startY = startY;
        this.radius = radius;
    }
}
class Erase extends Shapes
{
}
class Shapes
{
    Circle circle;
    Rect rect;
    Line line;
    Erase erase;

    public Erase getErase() {
        return erase;
    }

    public void setErase(Erase erase) {
        this.erase = erase;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }
}