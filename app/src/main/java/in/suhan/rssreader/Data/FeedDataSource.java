package in.suhan.rssreader.Data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by suhan on 12/02/15.
 */
public class FeedDataSource {
    private static final String PREFKEY = "RSSFeedList";
    private SharedPreferences smsPrefs;

    public FeedDataSource(Context context) {
        smsPrefs = context.getSharedPreferences(PREFKEY, Context.MODE_PRIVATE);
        smsPrefs.edit().clear();
    }

    public List<FeedSourceItem> findAll() {

        Map<String, ?> SMSMap = smsPrefs.getAll();
        SortedSet<String> keys = new TreeSet<String>(SMSMap.keySet());

        List<FeedSourceItem> SMSList = new ArrayList<FeedSourceItem>();
        for (String key : keys) {
            FeedSourceItem sms = new FeedSourceItem();
            sms.setKey(key);
            String value = (String) SMSMap.get(key);

            String[] values = value.split(";;");

            if (values.length == 2) {
                sms.setFeedTitle((String) values[0]);
                sms.setFeedURL((String) values[1]);

                SMSList.add(sms);
            } else {
                remove(sms);
            }
        }
        return SMSList;
    }

    public boolean update(FeedSourceItem smsItem) {
        SharedPreferences.Editor editor = smsPrefs.edit();
        editor.putString(smsItem.getKey(), smsItem.getFeedTitle() + ";;" + smsItem.getFeedURL());
        editor.commit();
        return true;
    }

    public boolean remove(FeedSourceItem smsItem) {

        if (smsPrefs.contains(smsItem.getKey())) {
            SharedPreferences.Editor editor = smsPrefs.edit();
            editor.remove(smsItem.getKey());
            editor.commit();
        }
        return true;
    }
}
