package in.suhan.rssreader;



import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import static android.support.v7.widget.RecyclerView.OnScrollListener;

/**
 * Created by ssaha8 on 18/02/2015.
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.CardAdapterHolder> {
    private Context context;
    private LayoutInflater inflater;
    private List<Feed.Entry> dataList = Collections.emptyList();
    private ViewGroup mViewGroup;

    public FeedAdapter(Context context, List<Feed.Entry> dataList) {
        this.context = context;
        this.dataList = dataList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public CardAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mViewGroup = parent;
        View view = inflater.inflate(R.layout.feed_item_layout,parent,false);
        return new CardAdapterHolder(view);
    }

    public void resetScrollDelay(){
        int temp = scrollDelay;

        scrollDelay = 0;
        Log.d("MaterialAnimationTrace", "Resetted scrolldelay old: " + temp + ", new: " + scrollDelay);
    }
    private static int scrollDelay = 0;
    @Override
    public void onBindViewHolder(CardAdapterHolder holder, int position) {
        Feed.Entry item = dataList.get(position);
        holder.textView.setText(item.title);

//        BitmapFactory.Options opts = new BitmapFactory.Options();
//        opts.inScaled = false;
//        holder.imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), item.getImgSrc(), opts));

        holder.view.setTag(position);

        holder.view.setTranslationX(800);
        ObjectAnimator animator = ObjectAnimator.ofFloat(holder.view,View.TRANSLATION_X,0);
        animator.setStartDelay(100 * scrollDelay );
        scrollDelay++;
        animator.setDuration(200);
        animator.start();
    }

    public void add(Feed.Entry item){
        dataList.add(item);
        notifyItemInserted(dataList.indexOf(item));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class CardAdapterHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;
        private View view;
        public CardAdapterHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.feedThumb);
            textView = (TextView) itemView.findViewById(R.id.feedTitle);
        }
    }
}

