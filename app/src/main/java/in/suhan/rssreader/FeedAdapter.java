package in.suhan.rssreader;


import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.transitions.everywhere.Fade;
import android.transitions.everywhere.Transition;
import android.transitions.everywhere.TransitionManager;
import android.transitions.everywhere.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedAdapterHolder> {
    private static int scrollDelay = 0;
    public boolean state = false;
    private ActionBarActivity context;
    private LayoutInflater inflater;
    private List<Feed.Entry> dataList = Collections.emptyList();
    private ViewGroup mViewGroup;
    private Boolean willAnimate = true;
    private View sharedView;
    private int prevScrollY = 0;

    public FeedAdapter(ActionBarActivity context, List<Feed.Entry> dataList) {
        this.context = context;
        this.dataList = dataList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public FeedAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mViewGroup = parent;
        View view = inflater.inflate(R.layout.feed_item_layout, parent, false);
        return new FeedAdapterHolder(view);
    }

    public void resetScrollDelay() {
        scrollDelay = 0;
        //Log.d("MaterialAnimationTrace", "Resetted scrolldelay old: " + temp + ", new: " + scrollDelay);
    }

    @Override
    public void onBindViewHolder(FeedAdapterHolder holder, int position) {
        Feed.Entry item = dataList.get(position);
        holder.textView.setText(Html.fromHtml(item.title));

        holder.imageView.setImageBitmap(item.bitmap);

        holder.view.setTag(position);

        if (willAnimate) {
            holder.view.setScaleX(0);
            holder.view.setScaleY(0);

            PropertyValuesHolder propx = PropertyValuesHolder.ofFloat("scaleX", 1);
            PropertyValuesHolder propy = PropertyValuesHolder.ofFloat("scaleY", 1);

            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(holder.view, propx, propy);
            animator.setDuration(100);
            animator.setStartDelay(300 * scrollDelay);
            scrollDelay++;
            animator.setDuration(200);
            animator.start();
        }
    }

    public void toggleAnimation(boolean willAnimate) {
        this.willAnimate = willAnimate;
    }

    public void add(Feed.Entry item) {
        dataList.add(item);
        notifyItemInserted(dataList.indexOf(item));
    }

    public void removeAll() {
        notifyItemRangeRemoved(0, dataList.size());
        dataList.removeAll(dataList);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    private void doTransition(View v) {
        int i = (int) v.getTag();

        ViewGroup sceneRoot = (ViewGroup) context.findViewById(R.id.sceneRoot);

        View view = sceneRoot.findViewById(R.id.feedContentContainer);
        final ImageView img = (ImageView) view.findViewById(R.id.feedThumb);
        final TextView title = (TextView) view.findViewById(R.id.feedTitle);
        final TextView body = (TextView) view.findViewById(R.id.textView2);
        final Toolbar appbar = (Toolbar) view.findViewById(R.id.toolbar);
        final TextView appbarTitle = (TextView) appbar.findViewById(R.id.toolbarTitle);
        final ScrollView feedBody = (ScrollView) view.findViewById(R.id.feedBody);

        feedBody.scrollTo(0, 0);
        appbar.setVisibility(View.VISIBLE);
        appbar.setBackgroundColor(Color.argb(1, 0, 0, 0));
        appbarTitle.setText("");
        Feed.Entry item = dataList.get(i);

        ViewGroup.LayoutParams layoutParams = img.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        img.setLayoutParams(layoutParams);

        img.setImageBitmap(item.bitmap);
        title.setText(item.title);
        body.setText(Html.fromHtml(item.summary));

        sharedView = v;
        int[] screenLocation = new int[2];
        v.getLocationOnScreen(screenLocation);
        img.setPivotX(0);
        img.setPivotY(0);
        img.setTranslationX(screenLocation[0]);
        img.setTranslationY(screenLocation[1]);
        img.setScaleX((float) v.getWidth() / img.getWidth());
        img.setScaleY((float) v.getHeight() / img.getHeight());
        img.setAlpha(0.1f);
        view.setAlpha(1f);
        view.setVisibility(View.VISIBLE);
        title.setPivotY(0);
        body.setPivotY(0);
        title.setScaleY(0f);
        body.setScaleY(0f);
        v.setAlpha(0f);

        img.animate().setStartDelay(0).setDuration(500).translationX(0).translationY(0).scaleX(1).alpha(1).withEndAction(new Runnable() {
            @Override
            public void run() {
                img.animate().setStartDelay(300).setDuration(600).scaleY(1f);
                title.animate().setStartDelay(900).setDuration(600).scaleY(1f);
                body.animate().setStartDelay(1500).setDuration(600).scaleY(1f);
            }
        });

        state = true;

    }

    public void doReverseTransition() {
        toggleAnimation(false);

        ViewGroup sceneRoot = (ViewGroup) context.findViewById(R.id.sceneRoot);
        final View view = sceneRoot.findViewById(R.id.feedContentContainer);
        final ImageView img = (ImageView) view.findViewById(R.id.feedThumb);
        final TextView title = (TextView) view.findViewById(R.id.feedTitle);
        final TextView body = (TextView) view.findViewById(R.id.textView2);
        final View imgContainer = view.findViewById(R.id.feedThumbContainer);
        final Toolbar appbar = (Toolbar) view.findViewById(R.id.toolbar);
        final ScrollView feedBody = (ScrollView) view.findViewById(R.id.feedBody);

        imgContainer.setBackgroundColor(Color.argb(1, 0, 0, 0));
        img.setPivotX(0);
        img.setPivotY(0);
        img.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        appbar.setVisibility(View.INVISIBLE);
        ViewGroup.LayoutParams layoutParams = img.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        img.setLayoutParams(layoutParams);
        feedBody.smoothScrollTo(0, 0);
        title.setPivotY(0);
        body.setPivotY(0);

        int[] screenLocation = new int[2];
        sharedView.getLocationOnScreen(screenLocation);

        body.animate().setStartDelay(0).setDuration(600).scaleY(0);
        title.animate().setDuration(300).setStartDelay(600).scaleY(0);
        img.animate().setDuration(500).setStartDelay(900).translationY(screenLocation[1] - 100).scaleX((float) sharedView.getWidth() / img.getWidth()).scaleY((float) sharedView.getHeight() / img.getHeight()).translationX(screenLocation[0]).withEndAction(new Runnable() {
            @Override
            public void run() {
                sharedView.setAlpha(1f);
                view.setVisibility(View.INVISIBLE);
            }
        });

        state = false;
    }

    public void createRSSList(ViewGroup sceneRoot) {

        ViewGroup viewContainer = (ViewGroup) inflater.inflate(R.layout.feed_list_layout, mViewGroup, false);

        RecyclerView recyclerView = (RecyclerView) viewContainer.findViewById(R.id.FeedList);
        final View url = viewContainer.findViewById(R.id.url);

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                resetScrollDelay();
                willAnimate = true;
                /*if(dy >= 10 ){
                    if(url.getVisibility() != View.GONE) {
                        url.setVisibility(View.GONE);
                        context.getSupportActionBar().hide();
                    }
                }else{
                    if(url.getVisibility() != View.VISIBLE) {
                        url.setVisibility(View.VISIBLE);
                        context.getSupportActionBar().show();
                    }
                }*/

                Log.d("ScrollTrace", dx + " , " + dy);
                super.onScrolled(recyclerView, dx, dy);
            }

        };

        recyclerView.setOnScrollListener(scrollListener);
        recyclerView.setAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));

        Toolbar toolbar = (Toolbar) viewContainer.findViewById(R.id.toolbar);
        context.setSupportActionBar(toolbar);
        //context.getSupportActionBar().setHideOnContentScrollEnabled(true);

        if (willAnimate) {
            View view1 = toolbar.getChildAt(0);
            View view2 = toolbar.getChildAt(1);
            View viewUrl = viewContainer.findViewById(R.id.url);

            toolbar.setAlpha(0);
            toolbar.setTranslationY(-300);
            view1.setTranslationY(-300);
            view2.setTranslationY(-300);
            viewUrl.setTranslationY(-300);
            viewUrl.setAlpha(0);


            viewContainer.bringChildToFront(toolbar);
            viewContainer.requestLayout();
            viewContainer.invalidate();

            toolbar.animate().setDuration(1000).translationY(0).alpha(1);
            view1.animate().setStartDelay(900).setDuration(1000).translationY(0);
            view2.animate().setStartDelay(900).setDuration(1000).translationY(0);
            viewUrl.animate().setStartDelay(1000).setDuration(1000).translationY(0).alpha(1);
        }

        sceneRoot.addView(viewContainer);

        View view = inflater.inflate(R.layout.feed_content_layout, mViewGroup, false);
        sceneRoot.addView(view);
        view.setAlpha(0f);
        view.setVisibility(View.INVISIBLE);

        final ScrollView sView = (ScrollView) view.findViewById(R.id.feedBody);
        final View imgView = view.findViewById(R.id.feedThumb);
        final TextView title = (TextView) view.findViewById(R.id.feedTitle);
        final Toolbar appbar = (Toolbar) view.findViewById(R.id.toolbar);
        final TextView appbarTitle = (TextView) appbar.findViewById(R.id.toolbarTitle);
        final View imgContainer = view.findViewById(R.id.feedThumbContainer);

        sView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {

                int scrolly = sView.getScrollY();

                ViewGroup.LayoutParams params = imgView.getLayoutParams();
                int height;
                if (params.height < 0) {
                    height = imgView.getHeight();
                } else {
                    height = params.height;
                }

                int deltaY = scrolly - prevScrollY;
                prevScrollY = scrolly;

                if (scrolly < 20) {
                    imgContainer.setBackgroundColor(Color.argb(1, 0, 0, 0));
                } else {
                    imgContainer.setBackgroundColor(context.getResources().getColor(R.color.primaryColor));
                }
                //view.scrollTo(0, 900);


                if (scrolly < (height * 0.4)) {
                    title.setVisibility(View.VISIBLE);
                    imgView.setVisibility(View.VISIBLE);
                    appbarTitle.setText(" ");
                    params.height = height - deltaY;
                    imgView.setLayoutParams(params);
                    imgView.setAlpha((float) (height - scrolly) / height);
                    appbar.setBackgroundColor(Color.argb(1, 0, 0, 0));
                    Log.d("SuhanTrace", "1 =>" + scrolly + " , " + deltaY + " , " + height);

                } else if (scrolly < (height * 0.8)) {
                    appbarTitle.setText(title.getText());
                    appbar.setBackgroundColor(context.getResources().getColor(R.color.primaryColor));
                    title.setVisibility(View.GONE);
                    imgView.setVisibility(View.GONE);
                    appbar.setVisibility(View.VISIBLE);
                    Log.d("SuhanTrace", "2 =>" + scrolly + " , " + deltaY + " , " + height);
                } else if (scrolly < (height * 0.9)) {
                    appbar.setVisibility(View.INVISIBLE);
                }
            }

        });

    }

    public class FeedAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;
        private TextView textView;
        private View view;

        public FeedAdapterHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.feedThumb);
            textView = (TextView) itemView.findViewById(R.id.feedBody);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            doTransition(v);
        }
    }
}

