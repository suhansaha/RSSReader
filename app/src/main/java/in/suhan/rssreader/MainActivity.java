package in.suhan.rssreader;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.suhan.rssreader.Data.FeedDataSource;
import in.suhan.rssreader.Data.FeedSourceItem;


public class MainActivity extends ActionBarActivity {
    public static final int ADD_ACTIVITY_REQUEST = 1327;
    List<FeedSourceItem> rssSourceList;
    ImageView refresh;
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

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem item = menu.findItem(R.id.action_refresh);

        item.setActionView(R.layout.rotating_refresh);
        refresh = (ImageView) item.getActionView().findViewById(R.id.refreshButton);

        refresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onOptionsItemSelected(item);
            }
        });
        fetchFeed(0);
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

            feed.refresh();
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
            fetchFeed(data.getIntExtra("index", -1));
        }
    }

    void fetchFeed(int index) {
        rssSource = new FeedDataSource(this);
        rssSourceList = rssSource.findAll();
        //TextView txt = (TextView) findViewById(R.id.url);

        if (!rssSourceList.isEmpty() && index >= 0 && index < rssSourceList.size()) {
            getSupportActionBar().setTitle(rssSourceList.get(index).getFeedTitle());
            feed = Feed.getFeed(this, rssSourceList.get(index).getFeedURL(), adapter, refresh);
        } else {
            getSupportActionBar().setTitle("Default: Anandabazar");
            feed = Feed.getFeed(this, "http://www.suhan.in/rss.xml", adapter, refresh);
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
