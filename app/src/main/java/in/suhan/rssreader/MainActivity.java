package in.suhan.rssreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.suhan.rssreader.Data.FeedDataSource;
import in.suhan.rssreader.Data.FeedSourceItem;


public class MainActivity extends ActionBarActivity {
    public static final int ADD_ACTIVITY_REQUEST = 1327;
    List<FeedSourceItem> rssSourceList;
    private FeedAdapter adapter;
    private Feed feed;
    private FeedDataSource rssSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Feed.Entry> list = new ArrayList<>();
        adapter = new FeedAdapter(this, list);
        adapter.createRSSList((ViewGroup) findViewById(R.id.sceneRoot));
        fetchFeed(0);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            adapter.toggleAnimation(true);
            adapter.removeAll();
            feed.getFeedData();
            return true;
        }
        if (id == R.id.action_select) {
            Intent intent = new Intent(this, AddRssFeedActivity.class);
            startActivityForResult(intent, ADD_ACTIVITY_REQUEST);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_ACTIVITY_REQUEST && resultCode == RESULT_OK) {
            TextView txt = (TextView) findViewById(R.id.url);

            fetchFeed(data.getIntExtra("index", -1));
        }
    }

    void fetchFeed(int index) {
        rssSource = new FeedDataSource(this);
        rssSourceList = rssSource.findAll();
        TextView txt = (TextView) findViewById(R.id.url);

        if (!rssSourceList.isEmpty() && index >= 0 && index < rssSourceList.size()) {
            adapter.removeAll();
            txt.setText(rssSourceList.get(index).getFeedTitle());
            feed = new Feed(this, rssSourceList.get(index).getFeedURL(), adapter);
        } else {
            adapter.removeAll();
            txt.setText("Default: Anandabazar");
            feed = new Feed(this, "http://www.suhan.in/rss.xml", adapter);
        }

    }

    @Override
    public void onBackPressed() {
        if (adapter.state) {
            adapter.doReverseTransition();
        } else {
            super.onBackPressed();
        }
    }

    public void goBack(View v) {
        adapter.doReverseTransition();
    }

    public void chooseFeed(View v) {
        Intent intent = new Intent(this, AddRssFeedActivity.class);
        startActivityForResult(intent, ADD_ACTIVITY_REQUEST);
    }
}
