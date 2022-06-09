package com.ioigoume.numberpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ioigoume.screenenergy.R;

/**
 * A simple layout group that provides a numeric text area with two buttons to
 * increment or decrement the value in the text area. Holding either button
 * will auto increment the value up or down appropriately. 
 * 
 * 
 */
@SuppressLint("UseValueOf")
public class NumberPicker extends LinearLayout {

	@SuppressWarnings("unused")
	private static final String TAG = "NumberPicker";
	private final long REPEAT_DELAY = 50;
	
	private final int ELEMENT_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, getResources().getDisplayMetrics());
	private final int ELEMENT_WIDTH = ELEMENT_HEIGHT; // you're all squares
	
	private final int MINIMUM = 0;
	private int MAXIMUM = 100;
	
	//private final int TEXT_SIZE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, getResources().getDisplayMetrics());
	private final int TEXT_SIZE = 30;
	
	public Integer value;
	
	Button decrement;
	Button increment;
	public EditText valueText;
	
	private Handler repeatUpdateHandler = new Handler();
	
	private boolean autoIncrement = false;
	private boolean autoDecrement = false;

	/**
	 * This little guy handles the auto part of the auto incrementing feature.
	 * In doing so it instantiates itself. There has to be a pattern name for
	 * that...
	 * 
	 *
	 */
	class RepetetiveUpdater implements Runnable {
		public void run() {
			if( autoIncrement ){
				increment();
				repeatUpdateHandler.postDelayed( new RepetetiveUpdater(), REPEAT_DELAY );
			} else if( autoDecrement ){
				decrement();
				repeatUpdateHandler.postDelayed( new RepetetiveUpdater(), REPEAT_DELAY );
			}
		}
	}
	
	public NumberPicker( Context context, AttributeSet attributeSet ) {
		super(context, attributeSet);
		
		LayoutParams numpic_params = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT );
		this.setLayoutParams( numpic_params );
		//LayoutParams elementParams = new LinearLayout.LayoutParams( ELEMENT_WIDTH, ELEMENT_HEIGHT );
		LayoutParams elementParams = new LinearLayout.LayoutParams( ELEMENT_WIDTH, LayoutParams.MATCH_PARENT );
		LayoutParams elementParams_txt = new LinearLayout.LayoutParams( ELEMENT_WIDTH + 30, LayoutParams.MATCH_PARENT );
		
		// init the individual elements
		MAXIMUM = init_attrs(context, attributeSet);
		initDecrementButton( context );
		initValueEditText( context );
		initIncrementButton( context );
		
		// Can be configured to be vertical or horizontal
		// Thanks for the help, LinearLayout!	
		if( getOrientation() == VERTICAL ){
			addView( increment, elementParams );
			addView( valueText, elementParams );
			addView( decrement, elementParams );
		} else {
			addView( decrement, elementParams );
			addView( valueText, elementParams_txt );
			addView( increment, elementParams );
		}
	}
	
	private int init_attrs(Context ctx, AttributeSet attrs){
		int value = 100;
		TypedArray tarray = ctx.obtainStyledAttributes(attrs, R.styleable.NumPickRange);
		CharSequence cseq = tarray.getString(R.styleable.NumPickRange_range);
		if(cseq!=null){
			value = Integer.parseInt(cseq.toString());
		}
		// For memory reusage
		tarray.recycle();
		//Log.i(TAG, String.valueOf(value));
		return value;
	}
	
	private void initIncrementButton( Context context){
		increment = new Button( context );
		increment.setId(R.string.increment_id);
		increment.setTextSize( TEXT_SIZE );
		increment.setText( "+" );
		
		// Increment once for a click
		increment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	increment();
            }
        });
		
		// Auto increment for a long click
		increment.setOnLongClickListener( 
				new View.OnLongClickListener(){
					public boolean onLongClick(View arg0) {
						autoIncrement = true;
						repeatUpdateHandler.post( new RepetetiveUpdater() );
						return false;
					}
				}
		);
		
		// When the button is released, if we're auto incrementing, stop
		increment.setOnTouchListener( new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if( event.getAction() == MotionEvent.ACTION_UP && autoIncrement ){
					autoIncrement = false;
				}
				return false;
			}
		});
	}
	
	private void initValueEditText( Context context){
		
		value = new Integer( 0 );
		
		valueText = new EditText( context );
		valueText.setId(R.string.text_id);
		valueText.setClickable(false);
		valueText.setKeyListener(null);
		valueText.setTextSize( TEXT_SIZE );
		
		// Since we're a number that gets affected by the button, we need to be
		// ready to change the numeric value with a simple ++/--, so whenever
		// the value is changed with a keyboard, convert that text value to a
		// number. We can set the text area to only allow numeric input, but 
		// even so, a carriage return can get hacked through. To prevent this
		// little quirk from causing a crash, store the value of the internal
		// number before attempting to parse the changed value in the text area
		// so we can revert to that in case the text change causes an invalid
		// number
		/*valueText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int arg1, KeyEvent event) {
				int backupValue = value;
				try {
					value = Integer.parseInt( ((EditText)v).getText().toString() );
				} catch( NumberFormatException nfe ){
					value = backupValue;
				}
				return false;
			}
		});*/
		
		// Highlight the number when we get focus
		valueText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if( hasFocus ){
					((EditText)v).selectAll();
				}
			}
		});
		valueText.setGravity( Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL );
		valueText.setText( value.toString() );
		//valueText.setInputType( InputType.TYPE_CLASS_NUMBER );
	}
	
	private void initDecrementButton( Context context){
		decrement = new Button( context );
		decrement.setId(R.string.decrement_id);
		decrement.setTextSize( TEXT_SIZE );
		decrement.setText( "-" );
		

		// Decrement once for a click
		decrement.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	decrement();
            }
        });
		

		// Auto Decrement for a long click
		decrement.setOnLongClickListener( 
				new View.OnLongClickListener(){
					public boolean onLongClick(View arg0) {
						autoDecrement = true;
						repeatUpdateHandler.post( new RepetetiveUpdater() );
						return false;
					}
				}
		);
		
		// When the button is released, if we're auto decrementing, stop
		decrement.setOnTouchListener( new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if( event.getAction() == MotionEvent.ACTION_UP && autoDecrement ){
					autoDecrement = false;
				}
				return false;
			}
		});
	}
	
	public void increment(){
		if( value < MAXIMUM ){
			value = value + 1;
			valueText.setText( value.toString() );
		}
	}

	public void decrement(){
		if( value > MINIMUM ){
			value = value - 1;
			valueText.setText( value.toString() );
		}
	}
	
	public int getValue(){
		return value;
	}
	
	public void setValue( int value ){
		if( value > MAXIMUM ) value = MAXIMUM;
		if( value >= 0 ){
			this.value = value;
			valueText.setText( this.value.toString() );
		}
	}
	
}
