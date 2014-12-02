package com.diegum.softkbdawareapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MainFragment extends Fragment {

        private View mBackground;
        private View mFrame;
        private TextView mText;
        private int mFrameSizeWhenFullyExpanded;
        private float mShrinkCoeff;
        private InputMethodManager mImm;
        private boolean mIsSoftKeyboardIn = false;

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_softkbd_aware, container, false);
            mBackground = rootView.findViewById(R.id.touchable_background);
            mFrame = rootView.findViewById(R.id.touchable_frame);
            mText = (TextView) rootView.findViewById(R.id.editable_text);
            mImm = (InputMethodManager) rootView.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            // Touching anywhere outside the frame hides the soft keyboard
            setupTapOutsideFrameListener();

            // Touching anywhere inside the frame, even if not the text view itself, shows the kbd
            setupTapInsideFrameListener();

            mFrameSizeWhenFullyExpanded = setupInitialSize(mFrame);

            setSoftKeyboardListener();

            return rootView;
        }

        /**
         * Sets a listener on the root view so when tapping there, the soft keyboard is hidden.
         */
        private void setupTapOutsideFrameListener() {
            mBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mImm.hideSoftInputFromWindow(mText.getWindowToken(), 0);
                }
            });
        }

        /**
         * Sets a listener on the frame view so when tapping there, even if not over the text view,
         * the soft keyboard slides in.
         */
        private void setupTapInsideFrameListener() {
            mFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mImm.showSoftInput(mText, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }

        /**
         * Sets a listener to react when the keyboard slides in or out. The reaction consists
         */
        private void setSoftKeyboardListener() {
            final View activityRootView = getActivity().getWindow().getDecorView()
                    .findViewById(android.R.id.content);
            activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            int height = activityRootView.getHeight();
                            if (isSoftKeyboardDetected(height)) {
                                // if it wasn't already expanded, the frame is shrunk
                                if (!mIsSoftKeyboardIn) {
                                    mShrinkCoeff = height * .8f / mFrameSizeWhenFullyExpanded;
                                    shrinkFrame();
                                }

                                mIsSoftKeyboardIn = true;
                            } else /* soft kbd is out, then if was in */ if (mIsSoftKeyboardIn) {
                                // the frame is stretched back to fully expanded
                                stretchFrame();
                                mIsSoftKeyboardIn = false;
                            }
                        }
                    }
            );
        }

        /**
         * True if the height received isn't, at most, much higher than the frame size when fully
         * expanded
         * @param activityRootViewHeight
         * @return
         */
        private boolean isSoftKeyboardDetected(int activityRootViewHeight) {
            return activityRootViewHeight * .9f < mFrameSizeWhenFullyExpanded;
        }

        //

        /**
         * Sets the size of the view to be an 80% of the screen width (app must only work in
         * portrait mode.
         * @return
         */
        private int setupInitialSize(View view) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay()
                    .getMetrics(metrics);

            int size = (int) (metrics.widthPixels * .8f);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = layoutParams.width = size;

            return size;
        }

        private void shrinkFrame() {
            scaleFrame(1f, mShrinkCoeff);
        }

        private void stretchFrame() {
            scaleFrame(mShrinkCoeff, 1f);
        }

        private void scaleFrame(final float from, final float to) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(from, to, from, to,
                    Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, 0f);
            scaleAnimation.setFillAfter(true);
            scaleAnimation.setDuration(500);

            float textFrom, textTo;
            // If we are shrinking
            if (from > to) {
                textFrom = from;
                textTo = to * 1.2f; // ... adjust text final size to keep harmony
            } else { // We are stretching
                textFrom = from * 1.2f; // ... then adjust text initial size to keep harmony
                textTo = to;
            }
            ScaleAnimation textAnimation = new ScaleAnimation(textFrom, textTo, textFrom, textTo,
                    Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, 0f);
            textAnimation.setFillAfter(true);
            textAnimation.setDuration(500);

            mFrame.startAnimation(scaleAnimation);
            mText.startAnimation(textAnimation);
        }

    }
}
