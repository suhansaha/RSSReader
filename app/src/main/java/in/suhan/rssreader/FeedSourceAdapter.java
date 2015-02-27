package in.suhan.rssreader;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import in.suhan.rssreader.Data.FeedSourceItem;

/**
 * Created by suhan on 12/02/15.
 */
public class FeedSourceAdapter extends ArrayAdapter<FeedSourceItem> {
    private Context context;
    private List<FeedSourceItem> objects;

    public FeedSourceAdapter(Context context, int textViewResourceId, List<FeedSourceItem> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeedSourceItem smsItem = objects.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        TextView view = (TextView) inflater.inflate(R.layout.source_item_layout, null);
        view.setText(smsItem.getFeedTitle());

        return view;
    }
}
