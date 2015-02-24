package in.suhan.rssreader;


import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.transitions.everywhere.Transition;
import android.transitions.everywhere.ChangeBounds;
import android.transitions.everywhere.TransitionManager;
import android.transitions.everywhere.Scene;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by ssaha8 on 18/02/2015.
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedAdapterHolder> {
    private static int scrollDelay = 0;
    public boolean state = false;
    private ActionBarActivity context;
    private LayoutInflater inflater;
    private List<Feed.Entry> dataList = Collections.emptyList();
    private ViewGroup mViewGroup;
    private Boolean willAnimate = true;

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
        int temp = scrollDelay;

        scrollDelay = 0;
        Log.d("MaterialAnimationTrace", "Resetted scrolldelay old: " + temp + ", new: " + scrollDelay);
    }

    @Override
    public void onBindViewHolder(FeedAdapterHolder holder, int position) {
        Feed.Entry item = dataList.get(position);
        holder.textView.setText(Html.fromHtml(item.title));

        holder.imageView.setImageBitmap(item.bitmap);

        holder.view.setTag(position);

        if (willAnimate) {
            //holder.view.setTranslationX(800);
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
        Log.d("RSSTrace", "Item Clicked: " + i);

        ViewGroup sceneRoot = (ViewGroup) context.findViewById(R.id.sceneRoot);

        View view = inflater.inflate(R.layout.feed_content_layout, mViewGroup, false);
        ImageView img = (ImageView) view.findViewById(R.id.imageView);
        TextView title = (TextView) view.findViewById(R.id.textView);
        TextView body = (TextView) view.findViewById(R.id.textView2);

        Feed.Entry item = dataList.get(i);

        img.setImageBitmap(item.bitmap);
        title.setText(item.title);
        body.setText(Html.fromHtml(item.summary));

        Scene mSceneA = Scene.getSceneForLayout(sceneRoot, R.layout.feed_list_layout, context);
        Scene mSceneB = new Scene(sceneRoot, view);

        Transition transition = new ChangeBounds();
        transition.setDuration(5000);
        transition.addTarget(v);

        TransitionManager.go(mSceneB, transition);
        state = true;

    }

    public void doReverseTransition(final FeedAdapter adapter) {
        toggleAnimation(false);
        ViewGroup sceneRoot = (ViewGroup) context.findViewById(R.id.sceneRoot);

        ViewGroup viewContainer = (ViewGroup) inflater.inflate(R.layout.feed_list_layout, mViewGroup, false);

        RecyclerView recyclerView = (RecyclerView) viewContainer.findViewById(R.id.FeedList);

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                adapter.resetScrollDelay();
                willAnimate = true;
                super.onScrolled(recyclerView, dx, dy);
            }
        };

        recyclerView.setOnScrollListener(scrollListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));

        Toolbar toolbar = (Toolbar) viewContainer.findViewById(R.id.toolbar);
        context.setSupportActionBar(toolbar);

        sceneRoot.removeAllViews();
        sceneRoot.addView(viewContainer);


        /*Scene mSceneA = new Scene(sceneRoot, recyclerView);

        Transition transition = new ChangeBounds();
        transition.setDuration(5000);
        TransitionManager.go(mSceneA, transition);*/
        state = false;
    }

    public class FeedAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;
        private TextView textView;
        private View view;

        public FeedAdapterHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.feedThumb);
            textView = (TextView) itemView.findViewById(R.id.feedTitle);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            doTransition(v);
        }
    }
}

