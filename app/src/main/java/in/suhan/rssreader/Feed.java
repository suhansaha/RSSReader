package in.suhan.rssreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
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
    private final FeedAdapter adapter;
    private final String url;
    private final Context context;

    public Feed(Context context, String url, FeedAdapter adapter) {
        this.url = url;
        this.context = context;
        this.adapter = adapter;
        getFeedData();
    }

    void getFeedData() {
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                processResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Could not fetch feed from: "+url, Toast.LENGTH_LONG).show();
            }
        });

        queue.add(stringRequest);
    }
    private void processResponse(String response){
        Log.d("RSSTrace", "Got Successful Response" + response.substring(1000,1100) );

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(response));
            parser.nextTag();
            readFeed(parser);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException{

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
            } else if(name.equals("channel")){
                //noinspection UnnecessaryContinue
                continue;
            }
            else {
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
            Log.d("RSSTrace", m.groupCount() + "=>" + m.group(1));
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
        public Bitmap bitmap;


        private Entry(String title, String summary, String link, String image) {
            this.title = title;
            this.summary = summary;
            this.link = link;
            if (image != null) {
                new LoadImage().execute(image);
            } else {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rss);

                adapter.add(this);
            }
        }

        private class LoadImage extends AsyncTask<String, String, Bitmap> {
            @Override
            protected Bitmap doInBackground(String... args) {
                try {
                    bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                adapter.add(Entry.this);
            }
        }
    }
}
