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
import android.widget.ImageView;

public class SlideTransition extends Transition {
    private static final String WIDTH = "in.suhan.rssreader:SlideTransition:width";
    private static final String HEIGHT = "in.suhan.rssreader:SlideTransition:height";
    private static final String TOP = "in.suhan.rssreader:SlideTransition:top";
    private static final String LEFT = "in.suhan.rssreader:SlideTransition:left";

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        //Log.d("RSSTrace", "Capture Start Values");
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        //Log.d("RSSTrace","Capture End Values");
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues) {
        ImageView imgView = (ImageView) transitionValues.view.findViewById(R.id.feedThumb);
        int[] screenLocation = new int[2];
        imgView.getLocationOnScreen(screenLocation);

        transitionValues.values.put(WIDTH, imgView.getWidth());
        transitionValues.values.put(HEIGHT, imgView.getHeight());
        transitionValues.values.put(LEFT, screenLocation[0]);
        transitionValues.values.put(TOP, screenLocation[1]);
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {

        if (startValues != null && endValues == null) {
            startValues.view.setAlpha(0);
            ImageView imgView = (ImageView) sceneRoot.findViewById(R.id.feedThumb);
            final View body = sceneRoot.findViewById(R.id.feedBody);

            body.setAlpha(0);

            int oldWidth = (int) startValues.values.get(WIDTH);
            int oldHeight = (int) startValues.values.get(HEIGHT);
            int oldTop = (int) startValues.values.get(TOP);
            int oldLeft = (int) startValues.values.get(LEFT);

            int[] screenLocation = new int[2];
            imgView.getLocationOnScreen(screenLocation);
            int newWidth = imgView.getWidth();
            int newHeight = imgView.getHeight();
            int newLeft = screenLocation[0];
            int newTop = screenLocation[1];

            imgView.setPivotX(0);
            imgView.setPivotY(0);
            imgView.setTranslationY(oldTop - newTop);
            imgView.setTranslationX(oldLeft - newLeft);
            imgView.setScaleX((float) oldWidth / newWidth);
            imgView.setScaleY((float) oldHeight / newHeight);

            body.setTranslationY(-body.getHeight());

            PropertyValuesHolder propX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1);
            PropertyValuesHolder propY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1);
            ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(imgView, propX, propY);

            PropertyValuesHolder trX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0);
            PropertyValuesHolder trY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0);
            ObjectAnimator trAnimator = ObjectAnimator.ofPropertyValuesHolder(imgView, trX, trY);
            trAnimator.setInterpolator(new AccelerateDecelerateInterpolator());


            PropertyValuesHolder bAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1);
            PropertyValuesHolder bY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0);
            ObjectAnimator bodyAnimator = ObjectAnimator.ofPropertyValuesHolder(body, bY, bAlpha);
            bodyAnimator.setInterpolator(new AnticipateOvershootInterpolator());

            AnimatorSet animator = new AnimatorSet();
            animator.play(trAnimator).before(scaleAnimator).before(bodyAnimator);


            return animator;
        }
        return super.createAnimator(sceneRoot, startValues, endValues);
    }
}
