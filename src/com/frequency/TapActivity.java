package com.frequency;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TapActivity extends Activity {
	private FreqTapArea mSurface;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        this.mSurface = (FreqTapArea)findViewById(R.id.taparea);
        
        // Restore previous state of the tap area
        if (savedInstanceState != null) {
        	this.mSurface.restoreState(savedInstanceState);
        	this.mSurface.pause();
        }
        else { this.mSurface.reset(); }
    }
    
    public void onPause() {
    	// When the activity pauses, so does the tap area
    	super.onPause();
    	mSurface.pause();
    }
    
    public void onStop() {
    	// When the activity stops, the tap area pauses
    	super.onStop();
    	this.mSurface.pause();
    }
    
    public void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// Back up the state of the tap area
        super.onSaveInstanceState(outState);
        this.mSurface.backupState(outState);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.options, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		// Only available in non-callback mode
	    	case R.id.reset:
				this.mSurface.reset();
	    		break;
    	}
  
    	return(super.onOptionsItemSelected(item));
    }
}
