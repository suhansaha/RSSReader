package in.suhan.rssreader;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.transitions.everywhere.Transition;
import android.transitions.everywhere.TransitionValues;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

class SlideTransition extends Transition {
    private static final String WIDTH = "in.suhan.rssreader:SlideTransition:width";
    private static final String HEIGHT = "in.suhan.rssreader:SlideTransition:height";
    private static final String TOP = "in.suhan.rssreader:SlideTransition:top";
    private static final String LEFT = "in.suhan.rssreader:SlideTransition:left";
    private TransitionValues startvalue = null;

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        //Log.d("RSSTrace", "Capture Start Values");
        captureValues(transitionValues);
        if (startvalue == null && transitionValues.view.getClass().equals(RelativeLayout.class)) {
            startvalue = transitionValues;
        }
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        //Log.d("RSSTrace","Capture End Values");
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues) {
        ImageView imgView = (ImageView) transitionValues.view.findViewById(R.id.feedThumb);
        if (imgView != null) {
            int[] screenLocation = new int[2];
            imgView.getLocationOnScreen(screenLocation);

            transitionValues.values.put(WIDTH, imgView.getWidth());
            transitionValues.values.put(HEIGHT, imgView.getHeight());
            transitionValues.values.put(LEFT, screenLocation[0]);
            transitionValues.values.put(TOP, screenLocation[1]);
        }
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        TransitionValues value1 = null;
        TransitionValues value2 = null;

        if (startValues != null && endValues != null) {
            return super.createAnimator(sceneRoot, startValues, endValues);
        } else if (endValues != null) {
            value1 = endValues;
            value2 = startvalue;
        } else if (startValues != null) {
            value1 = startvalue;
            value2 = startValues;
        }

        int[] screenLocation = new int[2];
        ImageView imgView = (ImageView) value1.view.findViewById(R.id.feedThumb);
        imgView.getLocationOnScreen(screenLocation);

        int newWidth = imgView.getWidth();
        int newHeight = imgView.getHeight();
        int newLeft = screenLocation[0];
        int newTop = screenLocation[1];

        int oldWidth = (int) value2.values.get(WIDTH);
        int oldHeight = (int) value2.values.get(HEIGHT);
        int oldTop = (int) value2.values.get(TOP);
        int oldLeft = (int) value2.values.get(LEFT);

        AnimatorSet animator = new AnimatorSet();

        imgView.setPivotX(0);
        imgView.setPivotY(0);
        imgView.setTranslationY(oldTop - newTop);
        imgView.setTranslationX(oldLeft - newLeft);
        imgView.setScaleX((float) oldWidth / newWidth);
        imgView.setScaleY((float) oldHeight / newHeight);
        imgView.setAlpha(0.5f);

        PropertyValuesHolder propA = PropertyValuesHolder.ofFloat(View.ALPHA, 1);
        PropertyValuesHolder propX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1);
        PropertyValuesHolder trX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0);
        PropertyValuesHolder trY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0);
        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(imgView, propX, trX, trY, propA);
        scaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        PropertyValuesHolder propY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1);
        ObjectAnimator trAnimator = ObjectAnimator.ofPropertyValuesHolder(imgView, propY);

        View body = value1.view.findViewById(R.id.feedBody);
        body.setAlpha(0);
        body.setTranslationY(-body.getHeight());

        PropertyValuesHolder bAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1);
        PropertyValuesHolder bY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0);
        ObjectAnimator bodyAnimator = ObjectAnimator.ofPropertyValuesHolder(body, bY, bAlpha);
        bodyAnimator.setInterpolator(new AnticipateOvershootInterpolator());

        animator.play(scaleAnimator).before(trAnimator).before(bodyAnimator);


        return animator;

    }
}
