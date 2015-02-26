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

public class SlideTransition extends Transition {
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
        AnimatorSet animator;
        if (endValues != null) {
            ImageView imgView = (ImageView) endValues.view.findViewById(R.id.feedThumb);

            int oldWidth = (int) startvalue.values.get(WIDTH);
            int oldHeight = (int) startvalue.values.get(HEIGHT);
            int oldTop = (int) startvalue.values.get(TOP);
            int oldLeft = (int) startvalue.values.get(LEFT);

            int[] screenLocation = new int[2];
            imgView.getLocationOnScreen(screenLocation);
            int newWidth = imgView.getWidth();
            int newHeight = imgView.getHeight();
            int newLeft = screenLocation[0];
            int newTop = screenLocation[1];


            animator = new AnimatorSet();

            if (startValues == null) {
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

                View body = endValues.view.findViewById(R.id.feedBody);
                body.setAlpha(0);
                body.setTranslationY(-body.getHeight());

                PropertyValuesHolder bAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1);
                PropertyValuesHolder bY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0);
                ObjectAnimator bodyAnimator = ObjectAnimator.ofPropertyValuesHolder(body, bY, bAlpha);
                bodyAnimator.setInterpolator(new AnticipateOvershootInterpolator());

                animator.play(scaleAnimator).before(trAnimator).before(bodyAnimator);

            } else {
                PropertyValuesHolder propA = PropertyValuesHolder.ofFloat(View.ALPHA, 0.5f);
                PropertyValuesHolder propX = PropertyValuesHolder.ofFloat(View.SCALE_X, (float) oldWidth / newWidth);
                PropertyValuesHolder trX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, oldLeft - newLeft);
                PropertyValuesHolder trY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, oldTop - newTop);
                ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(imgView, propX, trX, trY, propA);
                scaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

                PropertyValuesHolder propY = PropertyValuesHolder.ofFloat(View.SCALE_Y, (float) oldHeight / newHeight);
                ObjectAnimator trAnimator = ObjectAnimator.ofPropertyValuesHolder(imgView, propY);
                animator.play(scaleAnimator).after(trAnimator);

                startvalue = null;
            }

            return animator;
        }


        return super.createAnimator(sceneRoot, startValues, endValues);
    }
}
