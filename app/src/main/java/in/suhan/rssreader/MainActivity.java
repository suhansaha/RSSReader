package in.suhan.rssreader;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private FeedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Feed.Entry> list = new ArrayList<>();
        adapter = new FeedAdapter(this, list);

        adapter.createRSSList((ViewGroup) findViewById(R.id.sceneRoot));
        fetchFeed();

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
            fetchFeed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void fetchFeed() {
        TextView txt = (TextView) findViewById(R.id.url);
        adapter.removeAll();
        Feed feed = new Feed(this, txt.getText().toString(), adapter);

    }

    @Override
    public void onBackPressed() {
        if (adapter.state) {
            adapter.doReverseTransition();
        } else {
            super.onBackPressed();
        }
    }
}
