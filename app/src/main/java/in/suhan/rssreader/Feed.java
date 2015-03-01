package in.suhan.rssreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Feed {
    private static final String ns = null;
    private static boolean isProcessing = false;
    private static int nodeCount;
    private final FeedAdapter adapter;
    private final String url;
    private final Context context;
    Animation rotation;
    private View refresh;


    private Feed(Context context, String url, FeedAdapter adapter, View refresh) {
        this.url = url;
        this.context = context;
        this.adapter = adapter;
        this.refresh = refresh;
        getFeedData();
    }

    public static Feed getFeed(Context context, String url, FeedAdapter adapter, View refresh) {
        if (isProcessing)
            return null;
        return new Feed(context, url, adapter, refresh);
    }

    public void refresh() {
        if (isProcessing)
            return;

        getFeedData();
    }

    private void getFeedData() {
        if (isProcessing)
            return;
        /* Do Animation */
        rotation = AnimationUtils.loadAnimation(context, R.anim.rotate);
        refresh.startAnimation(rotation);

        isProcessing = true;

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                adapter.removeAll();
                processResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                refresh.clearAnimation();
                isProcessing = false;
                Toast.makeText(context, "Could not fetch feed from: " + url, Toast.LENGTH_LONG).show();
            }
        });

        queue.add(stringRequest);
    }

    private void processResponse(String response) {
        //Log.d("RSSTrace", "Got Successful Response");

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(response));
            parser.nextTag();
            readFeed(parser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isProcessing = false;
        refresh.clearAnimation();

    }

    private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, ns, "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            //noinspection IfCanBeSwitch
            if (name.equals("item")) {
                readEntry(parser);
            } else if (name.equals("channel")) {
                //noinspection UnnecessaryContinue
                continue;
            } else {
                skip(parser);
            }
        }
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        String title = null;
        String summary = null;
        String link = null;
        String image = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            //noinspection IfCanBeSwitch
            if (name.equals("title")) {
                title = readTitle(parser);
            } else if (name.equals("description")) {
                summary = readSummary(parser);
                image = fetchImage(summary);
                summary = summary.replaceAll("img.*?src", "");
            } else if (name.equals("link")) {
                link = readLink(parser);
            } else if (name.equals("media:thumbnail")) {
                if (image == null) {
                    image = readImage(parser);
                }
            } else {
                skip(parser);
            }
        }
        return new Entry(title, summary, link, image);
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }

    // Processes link tags in the feed.
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        String link;
        parser.require(XmlPullParser.START_TAG, ns, "link");
        //String tag = parser.getName();
        /*String relType = parser.getAttributeValue(null, "rel");
        if (tag.equals("link")) {
            if (relType.equals("alternate")){
                link = parser.getAttributeValue(null, "href");
                parser.nextTag();
            }
        }*/
        link = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    // Processes summary tags in the feed.
    private String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return summary;
    }

    private String readImage(XmlPullParser parser) throws IOException, XmlPullParserException {
        String image = "";
        parser.require(XmlPullParser.START_TAG, ns, "media:thumbnail");
        String tag = parser.getName();
        if (tag.equals("media:thumbnail")) {
            image = parser.getAttributeValue(null, "url");
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, ns, "media:thumbnail");
        return image;
    }

    String fetchImage(String body) {
        //img src="http://www.wired.com/wp-content/uploads/2015/02/184855511-660x440.jpg"
        Pattern p = Pattern.compile("img.+?src.+?[\'\"](.+?)[\'\"]");
        //Pattern p = Pattern.compile(".*img(.*)>");
        Matcher m = p.matcher(body);
        if (m.find()) {
            //Log.d("RSSTrace", m.groupCount() + "=>" + m.group(1));
            return m.group(1);
        }
        return null;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public class Entry {
        public final String title;
        public final String link;
        public final String summary;
        public String image;
        public Bitmap bitmap;


        private Entry(String title, String summary, String link, String image) {
            this.title = title;
            this.summary = summary;
            this.link = link;
            this.image = image;
            adapter.add(this);
        }

        public void startAnimation() {
            refresh.startAnimation(rotation);
        }

        public void stopAnimation() {
            refresh.clearAnimation();
        }

    }
}
