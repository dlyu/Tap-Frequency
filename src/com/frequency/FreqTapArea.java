package com.frequency;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/*
 * This is a ViewGroup object that contains several things: a touch area to record touch events and other things to provide information and control.
 * This object acts as a state machine; it has an idle mode, an active mode, and a paused mode.
 * */
public class FreqTapArea extends RelativeLayout {
	// The available states for the state machine
	private static final int STATE_IDLE = 0;
	private static final int STATE_RUNNING = 1;
	private static final int STATE_PAUSED = 2;
	
	// The interval in which the on-screen metrics update in milliseconds
	private static final int UPDATE_INTERVAL = 1;
	
	// Labels used to store important values in the Bundle object for when the enclosing Activity gets killed
	private static final String LABEL_COUNT = "count";
	private static final String LABEL_START_TIME = "initTime";
	private static final String LABEL_CURRENT_TIME = "currTime";
	private static final String LABEL_INTERVAL_OFFSET = "offset";
	
	// The number of taps
	private int mCount;
	
	// The start time of when the tap area started, counted in milliseconds since the UNIX epoch time
	private long mStartTime;
	
	// The time interval that has passed since the start of the operation counted in seconds.
	// Calculated as the current time from the epoch minus mStartTime plus mIntervalPassed.
	private double mCurrentTime;
	
	// The interval from when the tap area last started to when it last paused.
	private long mIntervalPassed;
	
	// The current state of the state machine
	private int mState;
	private Handler mHandler;

	// Provides the user with the time elapsed, tap count, and frequency
	private TextView mText;
	
	// The area the user touches to record touch events
	private View mTouchArea;
	
	// Used to toggle between running and pause mode
	private Button mButton;
	
	public FreqTapArea(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mHandler = new Handler();
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.tap_area, this);
		
		// Get the reference of the three View objects that I will be updating
		this.mText = (TextView) this.findViewById(R.id.freq);
		this.mTouchArea = this.findViewById(R.id.toucharea);
		this.mButton = (Button) this.findViewById(R.id.button);
		
		// Set the touch listener to the touch area to listen to taps
		this.mTouchArea.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (mState == STATE_IDLE || mState == STATE_PAUSED) { resume(); }
				else if (mState == STATE_RUNNING) { mCount++; }
				return false;
			}
		});
		
		// Set the click listener to the button to toggle running/pausing
		this.mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mState == STATE_RUNNING) { pause(); }
				else { resume(); }
			}
		});
	}
	
	protected Runnable mUpdateTimeTask = new Runnable() {
		   public void run() {
			   // Update the current time that has passed
			   mCurrentTime = (System.currentTimeMillis() - mStartTime + mIntervalPassed)/1000f;
			   mHandler.postDelayed(this, UPDATE_INTERVAL);
			   updateText();
		   }
		};

	public void updateText() {
		this.mText.setText(String.format(
				"Time elapsed: %.2f seconds\nTap count: %d taps\nFrequency: %s",
				this.mCurrentTime,
				this.mCount,
				this.mCurrentTime == 0 ? "N/A" : String.format("%.2f taps/sec", this.mCount/this.mCurrentTime))
			);	
	}

	/*
	 * Reset everything to initial state
	 * */
    public void reset() {
    	this.mHandler.removeCallbacks(this.mUpdateTimeTask);
    	this.mCount = 0;
    	this.mStartTime = System.currentTimeMillis();
    	this.mCurrentTime = 0;
    	this.mIntervalPassed = 0;
    	this.mState = STATE_IDLE;
    	
    	this.updateText();
    }
    
    /*
     * Halt the current operation; update the button display, the state, and record the time interval between when the tap area first started running and now.
     * */
    public void pause() {
    	this.mState = STATE_PAUSED;
    	this.mHandler.removeCallbacks(mUpdateTimeTask);
    	this.mIntervalPassed = System.currentTimeMillis() - this.mStartTime;
    	this.mButton.setText("Go");
    }
    
    /*
     * Resume the current operation; update the button display, the state, and record a new start time in milliseconds from the epoch.
     * */
    public void resume() {
    	this.mStartTime = System.currentTimeMillis();
    	this.mState = STATE_RUNNING;
    	this.mHandler.postDelayed(this.mUpdateTimeTask, UPDATE_INTERVAL);
    	this.mButton.setText("Pause");
    }
    
    /*
     * Restores the state of the tap area when the Activity backed up a previous state.
     * */
    public void restoreState(Bundle data) {
    	this.mCount = data.getInt(LABEL_COUNT, 0);
    	this.mStartTime = data.getLong(LABEL_START_TIME, System.currentTimeMillis());
    	this.mCurrentTime = data.getDouble(LABEL_CURRENT_TIME, 0);
    	this.mIntervalPassed = data.getLong(LABEL_INTERVAL_OFFSET, 0);
    }
    
    /*
     * Backup the state of the tap area when the Activity is about to get killed.
     * */
    public void backupState(Bundle data) {
        data.putInt(LABEL_COUNT, this.mCount);
        data.putLong(LABEL_START_TIME, this.mStartTime);
        data.putDouble(LABEL_CURRENT_TIME, this.mCurrentTime);
        data.putLong(LABEL_INTERVAL_OFFSET, this.mIntervalPassed);
    }
}
