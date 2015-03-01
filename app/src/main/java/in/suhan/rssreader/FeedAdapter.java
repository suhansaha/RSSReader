package in.suhan.rssreader;


import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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

import com.android.volley.toolbox.ImageLoader;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedAdapterHolder> {
    private static int scrollDelay = 0;
    private final ActionBarActivity context;
    private final LayoutInflater inflater;
    public boolean state = false;
    private List<Feed.Entry> dataList = Collections.emptyList();
    private ViewGroup mViewGroup;
    private Boolean willAnimate = true;
    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
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

            //Log.d("ScrollTrace", dx + " , " + dy);
            super.onScrolled(recyclerView, dx, dy);
        }

    };
    private int prevScrollY = 0;
    private int stability = 0;
    private int[] titleColors;
    private int[] statusBarColors;
    private int titleColor;
    private ViewTreeObserver.OnScrollChangedListener feedScrollListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            if (stability > 0) {
                stability--;
                return;
            }
            int height;
            int scrolly = feedBodyScroll.getScrollY();

            ViewGroup.LayoutParams params = feedImageContainer.getLayoutParams();
            if (params.height < 0) {
                height = feedImageContainer.getHeight();
            } else {
                height = params.height;
            }

            int deltaY = scrolly - prevScrollY;
            prevScrollY = scrolly;

            if (scrolly < 20) {
                feedImageContainer.setBackgroundColor(Color.argb(1, 0, 0, 0));
            } else {
                feedImageContainer.setBackgroundColor(titleColor);
            }

            float break1 = (float) (height * 0.4);
            float break2 = (float) (height * 0.8);
            float break3 = (float) (height * 0.9);
            if (scrolly < break1) {
                feedTitle.setVisibility(View.VISIBLE);
                feedImageContainer.setVisibility(View.VISIBLE);
                feedToolbarTitle.setText(" ");
                feedToolbar.setBackgroundColor(Color.argb(1, 0, 0, 0));

                params.height = height - deltaY;
                feedImageContainer.setLayoutParams(params);
                feedImage.setAlpha((float) (height - scrolly) / height);
                //Log.d("SuhanTrace", "1 =>" + scrolly + " , " + deltaY + " , " + height);

            } else if (scrolly < break2) {
                stability = 1;
                feedToolbarTitle.setText(feedTitle.getText());
                feedToolbar.setBackgroundColor(titleColor);
                feedTitle.setVisibility(View.GONE);
                feedImageContainer.setVisibility(View.GONE);
                feedToolbar.setVisibility(View.VISIBLE);
                //Log.d("SuhanTrace", "2 =>" + scrolly + " , " + deltaY + " , " + height);
            } else if (scrolly < break3) {
                feedToolbar.setVisibility(View.INVISIBLE);
            }
        }

    };
    private boolean spawned = false;
    private View sharedView;
    private int sharedImageHeight;
    private ViewGroup feedListLayout;
    private RecyclerView feedListRView;
    private Toolbar feedListToolbar;
    //private View urlBar;
    private View feedContentLayout;
    private View feedContainer;
    private ImageView feedImage;
    private View feedImageContainer;
    private TextView feedTitle;
    private TextView feedBody;
    private Toolbar feedToolbar;
    private TextView feedToolbarTitle;
    private ScrollView feedBodyScroll;

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

    void resetScrollDelay() {
        scrollDelay = 0;
        //Log.d("MaterialAnimationTrace", "Resetted scrolldelay old: " + temp + ", new: " + scrollDelay);
    }

    int getTitleColor(int resId) {
        return context.getResources().getColor(titleColors[resId % titleColors.length]);
    }

    int getStatusBarColor(int resId) {
        return context.getResources().getColor(statusBarColors[resId % statusBarColors.length]);
    }

    @Override
    public void onBindViewHolder(FeedAdapterHolder holder, int position) {
        Feed.Entry item = dataList.get(position);
        holder.textView.setText(Html.fromHtml(item.title));
        holder.textView.setBackgroundColor(titleColors[position % titleColors.length]);
        holder.view.setTag(position);


        //if (willAnimate) {
        holder.view.setScaleX(0);
        holder.view.setScaleY(0);

        PropertyValuesHolder propx = PropertyValuesHolder.ofFloat("scaleX", 1);
        PropertyValuesHolder propy = PropertyValuesHolder.ofFloat("scaleY", 1);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(holder.view, propx, propy);
        animator.setStartDelay(300 * scrollDelay);
        scrollDelay++;
        animator.setDuration(200);
        //animator.start();

        new imageLoader(item, holder.imageView, animator);
        //}
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
        TextView txtView = (TextView) v.findViewById(R.id.feedBody);
        ColorDrawable cd = (ColorDrawable) txtView.getBackground();
        titleColor = cd.getColor();
        int i = (int) v.getTag();

        //Reset Widgets
        feedBody.scrollTo(0, 0);
        feedToolbar.setVisibility(View.VISIBLE);
        feedToolbar.setBackgroundColor(Color.argb(1, 0, 0, 0));
        feedToolbarTitle.setText("");

        feedImageContainer.setVisibility(View.VISIBLE);
        feedImageContainer.setBackgroundColor(titleColor);
        ViewGroup.LayoutParams layoutParams = feedImageContainer.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        feedImageContainer.setLayoutParams(layoutParams);

        //Set data to widgets
        Feed.Entry item = dataList.get(i);
        feedImage.setImageBitmap(item.bitmap);
        feedTitle.setText(Html.fromHtml("<a href=\"" + item.link + "\" >" + item.title + "</a>"));
        feedTitle.setBackgroundColor(titleColor);
        //Log.d("SuhanTrace",Html.fromHtml(item.summary).toString());
        //Log.d("SuhanTrace",item.summary.replaceAll("img.*?src",""));
        feedBody.setText(Html.fromHtml(item.summary));
        feedTitle.setMovementMethod(LinkMovementMethod.getInstance());
        feedBody.setMovementMethod(LinkMovementMethod.getInstance());

        sharedImageHeight = feedImageContainer.getHeight();
        //Log.d("SuhanTrace","" + sharedImageHeight);

        //Store current view for reverse transition reference
        sharedView = v;

        //Animation starts from here
        int[] screenLocation = new int[2];
        v.getLocationOnScreen(screenLocation);

        feedImageContainer.setPivotX(0);
        feedImageContainer.setPivotY(0);
        feedImageContainer.setTranslationX(screenLocation[0]);
        feedImageContainer.setTranslationY(screenLocation[1]);
        feedImageContainer.setScaleX((float) v.getWidth() / feedImageContainer.getWidth());
        feedImageContainer.setScaleY((float) v.getHeight() / feedImageContainer.getHeight());

        feedContainer.setAlpha(1f);
        feedContainer.setVisibility(View.VISIBLE);
        v.setAlpha(0f);

        feedTitle.setPivotY(0);
        feedBody.setPivotY(0);
        feedTitle.setScaleY(0f);
        feedBody.setScaleY(0f);

        feedImageContainer.animate().setStartDelay(0).setDuration(500).translationX(0)
                .translationY(0).scaleX(1).withEndAction(new Runnable() {
            @Override
            public void run() {
                feedImageContainer.animate().setStartDelay(300).setDuration(600).scaleY(1f)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                sharedImageHeight = feedImage.getHeight();
                            }
                        });
                feedTitle.animate().setStartDelay(900).setDuration(600).scaleY(1f);
                feedBody.animate().setStartDelay(1500).setDuration(600).scaleY(1f);
            }
        });

        state = true;

    }

    public void doReverseTransition() {
        toggleAnimation(false);

        feedImageContainer.setBackgroundColor(Color.argb(1, 0, 0, 0));
        feedImageContainer.setPivotX(0);
        feedImageContainer.setPivotY(0);
        feedTitle.setVisibility(View.VISIBLE);

        feedImageContainer.setVisibility(View.VISIBLE);
        feedBodyScroll.smoothScrollTo(0, 0);
        ViewGroup.LayoutParams layoutParams = feedImageContainer.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        feedImageContainer.setLayoutParams(layoutParams);


        feedTitle.setPivotY(0);
        feedBody.setPivotY(0);

        int[] screenLocation = new int[2];
        sharedView.getLocationOnScreen(screenLocation);
        Log.d("SuhanTrace", sharedView.getWidth() + "," + feedImage.getWidth() + "," + feedImage.getHeight());
        feedBody.animate().setStartDelay(0).setDuration(600).scaleY(0);
        feedTitle.animate().setDuration(300).setStartDelay(600).scaleY(0);
        feedImageContainer.animate().setDuration(500).setStartDelay(900).translationY(screenLocation[1] - 100)
                .scaleX((float) sharedView.getWidth() / feedImage.getWidth())
                .scaleY((float) sharedView.getHeight() / sharedImageHeight)
                .translationX(screenLocation[0]).withEndAction(new Runnable() {
            @Override
            public void run() {
                sharedView.setAlpha(1f);
                feedContainer.setVisibility(View.INVISIBLE);
                feedToolbar.setVisibility(View.INVISIBLE);
            }
        });

        state = false;
    }

    public void createRSSList(ViewGroup sceneRoot) {
        titleColors = new int[6];
        statusBarColors = new int[6];
        titleColors = context.getResources().getIntArray(R.array.titlecolor);
        statusBarColors = context.getResources().getIntArray(R.array.statusbarcolor);

        feedListLayout = (ViewGroup) inflater.inflate(R.layout.feed_list_layout, mViewGroup, false);
        feedListRView = (RecyclerView) feedListLayout.findViewById(R.id.FeedList);
        //urlBar = feedListLayout.findViewById(R.id.url);
        feedListToolbar = (Toolbar) feedListLayout.findViewById(R.id.toolbar);

        feedListRView.setOnScrollListener(scrollListener);
        feedListRView.setAdapter(this);
        feedListRView.setLayoutManager(new GridLayoutManager(context, 2));

        context.setSupportActionBar(feedListToolbar);

        if (willAnimate)
            animateToolbar(feedListToolbar);

        sceneRoot.addView(feedListLayout);

        feedContentLayout = inflater.inflate(R.layout.feed_content_layout, mViewGroup, false);
        feedContainer = feedContentLayout.findViewById(R.id.feedContainer);
        feedBodyScroll = (ScrollView) feedContentLayout.findViewById(R.id.feedBodyScroll);
        feedImage = (ImageView) feedContentLayout.findViewById(R.id.feedImage);
        feedImageContainer = feedContentLayout.findViewById(R.id.feedImageContainer);
        feedTitle = (TextView) feedContentLayout.findViewById(R.id.feedTitle);
        feedBody = (TextView) feedContentLayout.findViewById(R.id.feedBody);
        feedToolbar = (Toolbar) feedContentLayout.findViewById(R.id.feedToolbar);
        feedToolbarTitle = (TextView) feedContentLayout.findViewById(R.id.toolbarTitle);

        sceneRoot.addView(feedContentLayout);
        feedContentLayout.setAlpha(0f);
        feedContentLayout.setVisibility(View.INVISIBLE);
        feedContentLayout.getViewTreeObserver().addOnScrollChangedListener(feedScrollListener);

        spawned = true;
    }

    private void animateToolbar(Toolbar toolbar) {
        View view1 = toolbar.getChildAt(0);
        View view2 = toolbar.getChildAt(1);

        toolbar.setAlpha(0);
        toolbar.setTranslationY(-300);
        view1.setTranslationY(-300);
        view2.setTranslationY(-300);
        //urlBar.setTranslationY(-300);
        //urlBar.setAlpha(0);

        toolbar.animate().setDuration(1000).translationY(0).alpha(1);
        view1.animate().setStartDelay(900).setDuration(1000).translationY(0);
        view2.animate().setStartDelay(900).setDuration(1000).translationY(0);
        //urlBar.animate().setStartDelay(1000).setDuration(1000).translationY(0).alpha(1);
    }

    private class imageLoader {
        private Feed.Entry item;
        private ObjectAnimator animator;
        private ImageView imageView;

        public imageLoader(Feed.Entry im, ImageView view, ObjectAnimator an) {
            this.item = im;
            this.animator = an;
            this.imageView = view;

            if (item.bitmap != null && !item.bitmap.isRecycled()) {
                imageView.setImageBitmap(item.bitmap);
                animator.start();
                return;
            }

            if (item.image != null) {
                item.startAnimation();
                new LoadImage().execute(item.image);
            } else {
                item.bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rss);
                imageView.setImageBitmap(item.bitmap);
                animator.start();
            }

        }

        private class LoadImage extends AsyncTask<String, String, Bitmap> {
            @Override
            protected Bitmap doInBackground(String... args) {
                try {
                    item.bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (item.bitmap == null || item.bitmap.getWidth() < 10) {
                        item.bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rss);
                    }
                    return item.bitmap;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bm) {
                super.onPostExecute(bm);
                imageView.setImageBitmap(item.bitmap);
                item.stopAnimation();
                animator.start();
                //adapter.add(Entry.this);
            }
        }
    }

    public class FeedAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView imageView;
        private final TextView textView;
        private final View view;

        public FeedAdapterHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.feedThumb);
            textView = (TextView) itemView.findViewById(R.id.feedBody);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (spawned)
                doTransition(v);
        }
    }

}

