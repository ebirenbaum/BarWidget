package edu.brown.ebirenbaum.barwidget;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;

/**
 * The main Activity for the BarView demo app.
 * 
 * @author evanb
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new BarViewFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu. This adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * The main Fragment for the BarView demo app.
	 * 
	 * @author evanb
	 */
	public static class BarViewFragment extends Fragment implements OnClickListener {
		
		// The bars in the "Animate!" section
		private BarView mStandardBar, mDividedBar, mStandardBarReversed, mDividedBarReversed;
		
		// The views associated with the customizable BarView at the bottom of the app
		private BarView mCustomizableBar;
		private NumberPicker mPercentagePicker, mPrimaryPicker, mSecondaryPicker;
		private CheckBox mDividerCheckbox, mVerticalCheckbox, mReversedCheckbox, mAnimateCheckbox;

		public BarViewFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			// Inflate the main fragment
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			
			// Find the BarView's in the "Animate!" section
			mStandardBar = (BarView) rootView.findViewById(R.id.demo_bar_view_standard);
			mDividedBar = (BarView) rootView.findViewById(R.id.demo_bar_view_divided);
			mStandardBarReversed = (BarView) rootView.findViewById(R.id.demo_bar_view_standard_reversed);
			mDividedBarReversed = (BarView) rootView.findViewById(R.id.demo_bar_view_divided_reversed);
			
			// Find the customizable BarView
			mCustomizableBar = (BarView) rootView.findViewById(R.id.demo_bar_customizable);
			
			// Find and set up the pickers for fill and color for the customizable BarView
			mPercentagePicker = (NumberPicker) rootView.findViewById(R.id.demo_bar_option_fill);
			mPrimaryPicker = (NumberPicker) rootView.findViewById(R.id.demo_bar_option_primary_color);
			mSecondaryPicker = (NumberPicker) rootView.findViewById(R.id.demo_bar_option_secondary_color);
			setupPickers();
			
			// Find the check boxes for the customizable BarView
			mDividerCheckbox = (CheckBox) rootView.findViewById(R.id.demo_bar_option_divider);
			mVerticalCheckbox = (CheckBox) rootView.findViewById(R.id.demo_bar_option_vertical);
			mReversedCheckbox = (CheckBox) rootView.findViewById(R.id.demo_bar_option_reversed);
			mAnimateCheckbox = (CheckBox) rootView.findViewById(R.id.demo_bar_option_animate);
			
			// Find the animate buttons and give them an OnClickListener
			Button animateButton = (Button) rootView.findViewById(R.id.demo_bar_view_animate_button);
			animateButton.setOnClickListener(this);
			Button resetButton = (Button) rootView.findViewById(R.id.demo_bar_view_reset_button);
			resetButton.setOnClickListener(this);
			
			return rootView;
		}
		
		/**
		 * Sets up the fill and color pickers for the customizable BarView.
		 */
		private void setupPickers() {
			
			// The bar will have a fill of [0,100], which is divided by 100
			mPercentagePicker.setMinValue(0);
			mPercentagePicker.setMaxValue(100);
			mPercentagePicker.setValue(50);
			mPercentagePicker.setWrapSelectorWheel(false);
			
			// The following colors can be parsed by android.graphics.Color.parseColor()
			String[] colors = { "red", "blue", "green", "black", "white", 
								"gray", "cyan", "magenta", "yellow", "lightgray", 
								"darkgray" };
			
			// Give both pickers the available colors, and have the secondary start on a different one
			mPrimaryPicker.setDisplayedValues(colors);
			mPrimaryPicker.setMinValue(0);
			mPrimaryPicker.setMaxValue(colors.length - 1);
			mSecondaryPicker.setDisplayedValues(colors);
			mSecondaryPicker.setMinValue(0);
			mSecondaryPicker.setMaxValue(colors.length - 1);
			mSecondaryPicker.setValue(1);
		}

		@Override
		public void onClick(View v) {
			
			// If this is the animate button, start animations on the appropriate BarViews
			if (v.getId() == R.id.demo_bar_view_animate_button) {
				mStandardBar.startBarAnimation();
				mDividedBar.startBarAnimation();
				mStandardBarReversed.startBarAnimation();
				mDividedBarReversed.startBarAnimation();
				
			// If this is the reset button, interpret the current state of the UI and customize the BarView
			} else if (v.getId() == R.id.demo_bar_view_reset_button) {
				
				// First do visibility and orientation updates
				mCustomizableBar.setDividerVisibility(mDividerCheckbox.isChecked());
				mCustomizableBar.setVertical(mVerticalCheckbox.isChecked());
				mCustomizableBar.setReversed(mReversedCheckbox.isChecked());
				mCustomizableBar.setBarPercentage((float)mPercentagePicker.getValue() / 100f);
				
				// Then update colors
				mCustomizableBar.setPrimaryBarColor(Color.parseColor(
						mPrimaryPicker.getDisplayedValues()[mPrimaryPicker.getValue()]));
				mCustomizableBar.setSecondaryBarColor(Color.parseColor(
						mSecondaryPicker.getDisplayedValues()[mSecondaryPicker.getValue()]));
				
				// Animate the bar if requested, else invalidate it to force a re-draw
				if (mAnimateCheckbox.isChecked()) {
					mCustomizableBar.startBarAnimation();
				} else {
					mCustomizableBar.invalidate();
				}
			}
		}
	}
}