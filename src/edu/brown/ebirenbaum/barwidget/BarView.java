package edu.brown.ebirenbaum.barwidget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

/**
 * A configurable bar view with the following properties:
 *   - Orientation (horizontal or vertical)
 *   - Direction (Primary left/up vs. primary right/down)
 *   - Primary bar color
 *   - Secondary bar color
 *   - Bar divider visibility and color
 *   - Bar fill percentage
 *   - Animation duration
 *   - Animate on load
 *   
 * @author evanb
 */
public class BarView extends LinearLayout {
	
	// The container layout for overall bar
	private final LinearLayout mContainer;
	
	// The colored primary, secondary, and divider bar views
	private final View mPrimarySection, mSecondarySection, mDividerSection;
	private final Resources mResources;
	
	// Configurable parameters for the view
	private boolean mIsReversed, mIsVertical;
	private float mPercentage;
	private long mDuration;
	
	public BarView(Context context) {
		this(context, null);
	}
	
	/**
	 * Constructs and configures the view given a set of attributes.
	 * 
	 * @param context	the application context
	 * @param attrs		the set of conigurable attributes
	 */
	public BarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		// Inflate the bar into the merged LinearLayout
		LayoutInflater inflater = 
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    inflater.inflate(R.layout.bar_view, this, true);
	    mResources = context.getResources();
	    
	    // Get hooks to the necessary views
	    mContainer = (LinearLayout) findViewById(R.id.bar_container);
	    mPrimarySection = findViewById(R.id.bar_left_half);
		mSecondarySection = findViewById(R.id.bar_right_half);
		mDividerSection = findViewById(R.id.bar_divider);
		
		// Parse the AttributeSet to determine the characteristics of the bar
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BarView, 0, 0);
		boolean shouldAnimate = a.getBoolean(R.styleable.BarView_animateOnLoad, true);
		mIsVertical = a.getBoolean(R.styleable.BarView_vertical, false);
		mIsReversed = a.getBoolean(R.styleable.BarView_reversed, false);
		
		// Update the orientation attributes for the bar
		setVertical(mIsVertical);
	    setBarPercentage(a.getFloat(R.styleable.BarView_fillPercentage, 1f), shouldAnimate);
	    setDuration(a.getInt(R.styleable.BarView_animationDuration, 500));
	    
	    // Update the color attributes for the bar
	    setDividerVisibility(a.getBoolean(R.styleable.BarView_showDivider, false));
	    setPrimaryBarColor(a.getColor(R.styleable.BarView_primaryColor, android.R.color.black));
	    setSecondaryBarColor(a.getColor(R.styleable.BarView_secondaryColor, android.R.color.white));
	    setDividerBarColor(a.getColor(R.styleable.BarView_dividerColor, android.R.color.black));
	    
	    // Clean up attribute data
	    a.recycle();
	}
	
	/**
	 * Set this bar to be a horizontal or vertical bar.
	 * 
	 * @param isVertical	true if the bar is vertical, false if horizontal
	 */
	public void setVertical(boolean isVertical) {
		
		// Update the container layout to match the orientation
		mContainer.setOrientation(isVertical ? VERTICAL : HORIZONTAL);
		
		// Get the current layout parameters for the three sections of the bar
		LinearLayout.LayoutParams primarySectionParams = (LayoutParams) mPrimarySection.getLayoutParams();
		LinearLayout.LayoutParams secondarySectionParams = (LayoutParams) mSecondarySection.getLayoutParams();
		LinearLayout.LayoutParams dividerSectionParams = (LayoutParams) mDividerSection.getLayoutParams();
		
		// If vertical, sections match the width of the bar
		if (isVertical) {
			primarySectionParams.width = LayoutParams.MATCH_PARENT;
			primarySectionParams.height = 0;
			
			secondarySectionParams.width = LayoutParams.MATCH_PARENT;
			secondarySectionParams.height = 0;
			
			dividerSectionParams.width = LayoutParams.MATCH_PARENT;
			dividerSectionParams.height = mResources.getDimensionPixelSize(R.dimen.bar_view_divider_width);
			
		// If horizontal, sections match the height of the bar
		} else {
			primarySectionParams.width = 0;
			primarySectionParams.height = LayoutParams.MATCH_PARENT;
			
			secondarySectionParams.width = 0;
			secondarySectionParams.height = LayoutParams.MATCH_PARENT;
			
			dividerSectionParams.width = mResources.getDimensionPixelSize(R.dimen.bar_view_divider_width);
			dividerSectionParams.height = LayoutParams.MATCH_PARENT;
		}
		
		// Update the layout parameters for the sections
		mPrimarySection.setLayoutParams(primarySectionParams);
		mSecondarySection.setLayoutParams(secondarySectionParams);
		mDividerSection.setLayoutParams(dividerSectionParams);
		
		// Invalidate this view to ensure it updates visually
		invalidate();
	}
	
	/**
	 * Set whether or not this bar is regular (primary left/top) or reversed (primary right/bottom).
	 * 
	 * @param isReversed	true if this bar is reversed, false if regular
	 */
	public void setReversed(boolean isReversed) {
		mIsReversed = isReversed;
		invalidate();
	}
	
	/**
	 * Set the percentage of the bar taken up by the primary section. Values not between
	 * 0 and 1 will be clamped to that range.
	 * 
	 * @param percentage	the fill of the bar
	 */
	public void setBarPercentage(float percentage) {
		setBarPercentage(percentage, false);
	}
	
	/**
	 * Set the percentage of the bar taken up by the primary section. Values not between
	 * 0 and 1 will be clamped to that range. Animates the bar after changing the fill if
	 * given true for the second parameter.
	 * 
	 * @param percentage	the fill of the bar
	 * @param shouldAnimate	true if the bar should animate into the new fill, false if not
	 */
	public void setBarPercentage(float percentage, boolean shouldAnimate) {
		
		// Clamp the percentage to [0,1]
		if (percentage < 0f) percentage = 0f;
		if (percentage > 1f) percentage = 1f;
		mPercentage = percentage;
		
		// Update or animate the bars
		if (!shouldAnimate) {
			updateBars(mPercentage);
		} else {
			startBarAnimation();
		}
	}
	
	/**
	 * Updates the layout parameters of the sections.
	 * 
	 * @param percentage
	 */
	private void updateBars(float percentage) {
		LinearLayout.LayoutParams primaryBarParams = (LayoutParams) mPrimarySection.getLayoutParams();
		primaryBarParams.weight = mIsReversed ? 1f - percentage : percentage;
		mPrimarySection.setLayoutParams(primaryBarParams);
		
		LinearLayout.LayoutParams secondaryBarParams = (LayoutParams) mSecondarySection.getLayoutParams();
		secondaryBarParams.weight = mIsReversed ? percentage : 1f - percentage;
		mSecondarySection.setLayoutParams(secondaryBarParams);
		
		invalidate();
	}
	
	/**
	 * Get the current fill percentage.
	 * 
	 * @return	the current fill of the bar
	 */
	public float getBarPercentage() {
		return mPercentage;
	}
	
	/**
	 * Set the color of the primary bar.
	 * 
	 * @param color	the new color of the primary bar
	 */
	public void setPrimaryBarColor(int color) {
		if (!mIsReversed) {
			mPrimarySection.setBackgroundColor(color);
		} else {
			mSecondarySection.setBackgroundColor(color);
		}
	}
	
	/**
	 * Set the color of the secondary bar.
	 * 
	 * @param color	the new color of the secondary bar
	 */
	public void setSecondaryBarColor(int color) {
		if (!mIsReversed) {
			mSecondarySection.setBackgroundColor(color);
		} else {
			mPrimarySection.setBackgroundColor(color);
		}
	}
	
	/**
	 * Set the color of the divider bar.
	 * 
	 * @param color	the new color of the divider bar
	 */
	public void setDividerBarColor(int color) {
		mDividerSection.setBackgroundColor(color);
	}
	
	/**
	 * Set the visibility of the divider bar.
	 * 
	 * @param isVisible	true if the divider should be visible, false if not
	 */
	public void setDividerVisibility(boolean isVisible) {
		if (!isVisible) {
	    	mDividerSection.setVisibility(View.GONE);
	    } else {
	    	mDividerSection.setVisibility(View.VISIBLE);
	    }
	}
	
	/**
	 * Set the duration of the animation in milliseconds.
	 * 
	 * @param duration	the duration of the animation in milliseconds
	 */
	public void setDuration(long duration) {
		mDuration = duration;
	}
	
	/**
	 * Do the bar animation based on the current animation parameters.
	 */
	public void startBarAnimation() {
		updateBars(0f);
		
		ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
		animator.setDuration(mDuration);
		animator.addUpdateListener(new BarFillAnimatorListener());
		animator.start();
	}
	
	/**
	 * A listener class responsible for updating the relative fill of the bars when animated.
	 * 
	 * @author evanb
	 */
	private class BarFillAnimatorListener implements ValueAnimator.AnimatorUpdateListener {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			float value = mPercentage * animation.getAnimatedFraction();
			BarView.this.updateBars(value);
		}
	}
}
