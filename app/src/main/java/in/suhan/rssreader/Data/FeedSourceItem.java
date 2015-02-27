package in.suhan.rssreader.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by suhan on 12/02/15.
 */
public class FeedSourceItem {
    private String key;
    private String feedURL;
    private String feedTitle;

    public static FeedSourceItem getNew() {
        Locale locale = new Locale("en_US");
        Locale.setDefault(locale);

        String pattern = "yyyy-MM-dd HH:mm:ss Z";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);

        String key = formatter.format(new Date());

        FeedSourceItem sms = new FeedSourceItem();
        sms.setKey(key);
        sms.setFeedTitle("");
        sms.setFeedURL("");

        return sms;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFeedURL() {
        return feedURL;
    }

    public void setFeedURL(String feedURL) {
        this.feedURL = feedURL;
    }

    public String getFeedTitle() {
        return feedTitle;
    }

    public void setFeedTitle(String feedTitle) {
        this.feedTitle = feedTitle;
    }
}
