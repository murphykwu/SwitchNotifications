package com.konka.switchnotifications.widget;


import com.konka.switchnotifications.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.CheckBox;

/**
 * 自定义一个CheckBox函数，将其修改成为SwitchBox样式。类似苹果上面的效果.
 * for forbid app send notifications-2013.06.18
 * @see 参考代码：https://github.com/IssacWang/SwitchButton 
 * @author konka-wubaiyu
 * @version 1.0
 */
public class SwitchButton extends CheckBox {
	private static final String TAG = "SwitchNotificationssss";//"SwitchNotifications/SwitchButton";
	private Paint mPaint;
	private ViewParent mParent;
	private Bitmap mBottom;
	private Bitmap mCurBtnPic;
	private Bitmap mBtnPressed;
	private Bitmap mBtnNormal;
	private Bitmap mFrame;
	private Bitmap mMask;
	private RectF mSaveLayerRectF;
	/**
	 * 使用PorterDuffXfermode实现遮罩层
	 * http://lonesane.iteye.com/blog/791267
	 */
	private PorterDuffXfermode mXfermode;
	private float mFirstDownX, mFirstDownY;//首次按下的坐标
	private float mRealPos;//图片的绘制位置
	private float mBtnPos;//按钮的位置
	private float mBtnOnPos;//开关打开的位置
	private float mBtnOffPos;//开关关闭的位置
	private float mMaskWidth, mMaskHeight;
	private float mBtnWidth;
	private float mBtnInitPos;
	private int mClickTimeout;
	private int mTouchSlop;
	private final int MAX_ALPHA = 255;
	private int mAlpha = MAX_ALPHA;
	private boolean mChecked = false;
	private boolean mBroadcasting;
	private boolean mTurningOn;
	private PerformClick mPerformClick;
	private OnCheckedChangeListener mOnCheckedChangeListener;
	private OnCheckedChangeListener mOnCheckedChangeWidgetListener;
	private boolean mAnimating;
	private final float VELOCITY = 350;
	private float mVelocity;
	private final float EXTENDED_OFFSET_Y = 15;
	private float mExtendOffsetY;//Y轴方向扩大的区域，增大点击区域
	private float mAnimationPosition;
	private float mAnimatedVelocity;
	private String mPkgName;
	

	public SwitchButton(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.checkboxStyle);
		// TODO Auto-generated constructor stub
	}

	public SwitchButton(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public SwitchButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.i(TAG, "start initView()");
		initView(context);//对象的建立都是要初始化显示形式的。
		// TODO Auto-generated constructor stub
	}
	
    private void initView(Context context) {
		// TODO Auto-generated method stub
    	mPaint = new Paint();
    	mPaint.setColor(Color.WHITE);
    	Resources resources = context.getResources();
    	
    	//获取超时相关信息，get ViewConfiguration
    	mClickTimeout = ViewConfiguration.getPressedStateDuration()
    			+ ViewConfiguration.getTapTimeout();
    	//以像素为单位，记录用户划过多长距离
    	mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    	Log.i(TAG, "initView----mTouchSlop = " + mTouchSlop + ", mClickTimeout = " + mClickTimeout);
    	
    	// get Bitmap，初始化图片资源
    	mBottom = BitmapFactory.decodeResource(resources, R.drawable.bottom);
    	mBtnPressed = BitmapFactory.decodeResource(resources, R.drawable.btn_pressed);
    	mBtnNormal = BitmapFactory.decodeResource(resources, R.drawable.btn_unpressed);
    	mFrame = BitmapFactory.decodeResource(resources, R.drawable.frame);
    	mMask = BitmapFactory.decodeResource(resources, R.drawable.mask);
    	mCurBtnPic = mBtnNormal;
    	
    	mBtnWidth = mBtnPressed.getWidth();
    	mMaskWidth = mMask.getWidth();
    	mMaskHeight = mMask.getHeight();
    	
    	mBtnOffPos = mBtnWidth / 2;
    	mBtnOnPos = mMaskWidth - mBtnWidth / 2;
    	
    	mBtnPos = mChecked ? mBtnOnPos : mBtnOffPos;
    	mRealPos = getRealPos(mBtnPos);
    	Log.i(TAG, "mBtnWidth = " + mBtnWidth + ", mMaskWidth = " + mMaskWidth
    			+ " mMaskHeight = " + mMaskHeight + ", mBtnOffPos = " + mBtnOffPos
    			+ ", mBtnOnPos " + mBtnOnPos + ", mBtnPos = " + mBtnPos 
    			+ ", mRealPos " + mRealPos);
    	
    	final float density = getResources().getDisplayMetrics().density;
    	mVelocity = (int) (VELOCITY * density + 0.5f);
    	mExtendOffsetY = (int) (EXTENDED_OFFSET_Y * density + 0.5f);
    	Log.i(TAG, "density = " + density + ",  mVelocity = " + mVelocity
    			+ ", mExtendOffsetY = " +mExtendOffsetY);
    	
    	mSaveLayerRectF = new RectF(0, mExtendOffsetY, mMask.getWidth(), mMask.getHeight()
    			+ mExtendOffsetY);
    	mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);    	
    	
    }
    
    @Override
    public void setEnabled(boolean enabled)
    {
    	mAlpha = enabled ? MAX_ALPHA : MAX_ALPHA / 2;
    	super.setEnabled(enabled);
    }
    
    public boolean isChecked()
    {
    	Log.i(TAG, "isChecked mChecked = " + mChecked);
    	return mChecked;
    }
    
    public void toggle(){
    	Log.i(TAG, "toggle");
    	setChecked(!mChecked);
    }
    
    /**
     * 内部调用该方法设置状态，延迟执行，保证动画流畅度
     * @param checked
     */
    private void setCheckedDelayed(final boolean checked)
    {
    	Log.i(TAG, "setCheckedDelayed");
    	this.postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				setChecked(checked);
			}
    		
    	}, 10);
    }
    
    /**
     * <p>
     * Changes the checked state of this button.
     * </p>
     */
    public void setChecked(boolean checked)
    {
    	Log.i(TAG, "--------->setChecked checked = " + checked + ", mChecked = " + mChecked);
    	if(mChecked != checked)
    	{
    		
    		mChecked = checked;
    		
    		mBtnPos = checked ? mBtnOnPos : mBtnOffPos;
    		mRealPos = getRealPos(mBtnPos);
    		invalidate();
    		
    		//Avoid infinite recursions if setChecked() is called from a listener
    		if(mBroadcasting){
    			return;
    		}
    		
    		mBroadcasting = true;
    		//改变选中状态的时候，触发注册在这个控件上面的响应函数。
    		if(mOnCheckedChangeListener != null)
    		{
    			Log.i(TAG, "setChecked---mOnCheckedChangeListener triggle the listener");
    			mOnCheckedChangeListener.onCheckedChanged(SwitchButton.this, mChecked);
    		}else
    		{
    			Log.i(TAG, "setChecked---mOnCheckedChangeListener = null");
    		}
    		
    		if(mOnCheckedChangeWidgetListener != null)
    		{
    			Log.i(TAG, "setChecked---mOnCheckedChangeWidgetListener");
    			mOnCheckedChangeWidgetListener.onCheckedChanged(SwitchButton.this, mChecked);
    		}
    		mBroadcasting = false;
    	}
    }
    
    /**
     * Register a callback to be invoked when the checked state of this button
     * changes.
     * 
     * @param listener the callback to call on checked state change
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    /**
     * Register a callback to be invoked when the checked state of this button
     * changes. This callback is used for internal purpose only.
     * 
     * @param listener the callback to call on checked state change
     * @hide
     */
    void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeWidgetListener = listener;
    }
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
    	//实现自TextView控件的方法，拖动触发了三个事件
    	//点击+拖动+离开屏幕，也就是ACTION_DOWN---ACTION_MOVE---ACTION_UP
    	int action = event.getAction();
    	float x = event.getX();
    	float y = event.getY();
    	float deltaX = Math.abs(x - mFirstDownX);
    	float deltaY = Math.abs(y - mFirstDownY);
//    	Log.i(TAG, "onTouchEvent");
    	switch(action)
    	{
    	case MotionEvent.ACTION_DOWN:    		
    		attemptClaimDrag();
    		mFirstDownX = x;
    		mFirstDownY = y;
    		mCurBtnPic = mBtnPressed;
    		mBtnInitPos = mChecked ? mBtnOnPos : mBtnOffPos;
//    		Log.i(TAG, "onTouchEvent-DOWN: mFirstDownX = " + mFirstDownX + ", mBtnInitPos = " + mBtnInitPos);
    		break;
    	case MotionEvent.ACTION_MOVE:
    		float time = event.getEventTime() - event.getDownTime();
    		mBtnPos = mBtnInitPos + event.getX() - mFirstDownX;
//    		Log.i(TAG, "mBtnPos = " + mBtnPos + ", (mBtnOnPos, mBtnOffPos) = (" + mBtnOnPos + ", "+ mBtnOffPos + ")"
//    				+ "evnent.getx() = " + event.getX() + ", mFirstDownX = " + mFirstDownX);
    		if(mBtnPos >= mBtnOffPos)
    		{
    			mBtnPos = mBtnOffPos;
    		}
    		
    		if(mBtnPos <= mBtnOnPos)
    		{
    			mBtnPos = mBtnOnPos;
    		}
    		mTurningOn = mBtnPos > ((mBtnOffPos - mBtnOnPos) / 2 + mBtnOnPos);   		

    		mRealPos = getRealPos(mBtnPos);
//    		Log.i(TAG, "onTouchEvent-MOVE: mBtnPos = " + mBtnPos + ", mBtnInitPos = " + mBtnInitPos);
    		break;
    	case MotionEvent.ACTION_UP:
    		mCurBtnPic = mBtnNormal;
    		time = event.getEventTime() - event.getDownTime();
    		Log.i(TAG, "onTouchEvent-UP: (deltaX, deltaY) = (" + deltaX + ", " + deltaY + ")" + ", mTouchSlop = " + mTouchSlop);
    		//由于在actionup的时候，发现按钮没有到达左右两边的最顶端，发现是action move的时候，没有将按钮坐标mBtnPos置为正确的值。所以在
    		//up的时候重新赋值。
    		if(mTurningOn)
    		{
    			mBtnPos = mBtnOffPos;
    		}else
    		{
    			mBtnPos = mBtnOnPos;
    		}
    		if(deltaY < mTouchSlop && deltaX < mTouchSlop && time < mClickTimeout)
    		{
    			Log.i(TAG, "ACTION_UP mTouchSlop");
    			if(mPerformClick == null)
    			{
    				mPerformClick = new PerformClick();
    			}
    			if(!post(mPerformClick))
    			{
    				Log.i(TAG, "ACTION_UP performClick()");
    				performClick();
    			}
    		}else{
    			Log.i(TAG, "onTouchEvent-UP startAnimation()");
    			startAnimation(!mTurningOn);
    		}
    		break;
    	}
    	invalidate();
    	return isEnabled();
//		return super.onTouchEvent(event);
	}
    
    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag()
    {
    	Log.i(TAG, "attemptClaimDrag");
    	mParent = getParent();
    	if(mParent != null)
    	{
    		mParent.requestDisallowInterceptTouchEvent(true);
    	}
    }
    
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		canvas.saveLayerAlpha(mSaveLayerRectF, mAlpha, 
				Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
				| Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
				| Canvas.CLIP_TO_LAYER_SAVE_FLAG);
		
		//绘制蒙板
		canvas.drawBitmap(mMask, 0, mExtendOffsetY, mPaint);
		mPaint.setXfermode(mXfermode);
		
		//绘制底部图片
		canvas.drawBitmap(mBottom, mRealPos, mExtendOffsetY, mPaint);
		mPaint.setXfermode(null);
//		Log.i(TAG, "onDraw--draw bottom mRealPos = " + mRealPos + ", mExtendOffsetY = " + mExtendOffsetY);
		//绘制边框
		canvas.drawBitmap(mFrame, 0, mExtendOffsetY, mPaint);
		
		//绘制按钮
		canvas.drawBitmap(mCurBtnPic, mRealPos, mExtendOffsetY, mPaint);
		canvas.restore();
//		super.onDraw(canvas);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		//重写TextView的onMeasure函数
		setMeasuredDimension((int) mMaskWidth, (int) (mMaskHeight + 2 * mExtendOffsetY));
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	private void startAnimation(boolean turnOn)
	{
		Log.i(TAG, "startAnimation");
		mAnimating = true;
		mAnimatedVelocity = turnOn ? -mVelocity : mVelocity;
		mAnimationPosition = mBtnPos;
		new SwitchAnimation().run();
	}

	private void stopAnimation()
	{
		mAnimating = false;
	}
	
	private final class SwitchAnimation implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(!mAnimating)
			{
				return;
			}
			doAnimation();
			FrameAnimationController.requestAnimationFrame(this);
		}		
	}
	
	private void doAnimation()
	{
		Log.i(TAG, "----->doAnimation");
		mAnimationPosition += mAnimatedVelocity * FrameAnimationController.ANIMATION_FRAME_DURATION / 1000;
		if(mAnimationPosition <= mBtnOnPos)
		{
			Log.i(TAG, "doAnimation---setCheckedDelayed(true)");
			stopAnimation();
			mAnimationPosition = mBtnOnPos;
			setCheckedDelayed(true);
		}else if(mAnimationPosition >= mBtnOffPos)
		{
			Log.i(TAG, "doAnimation---setCheckedDelayed(false)");
			stopAnimation();
			mAnimationPosition = mBtnOffPos;
			setCheckedDelayed(false);
		}
		moveView(mAnimationPosition);
	}
	
	private void moveView(float position)
	{
		mBtnPos = position;
		mRealPos = getRealPos(mBtnPos);
		invalidate();
	}
	
	/**
     * 将btnPos转换成RealPos
     * 
     * @param btnPos
     * @return
     */
    private float getRealPos(float btnPos) {
        return btnPos - mBtnWidth / 2;
    }

	private final class PerformClick implements Runnable {
        public void run() {
            performClick();
        }
    }

}
