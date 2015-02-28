package in.suhan.rssreader;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import in.suhan.rssreader.Data.FeedDataSource;
import in.suhan.rssreader.Data.FeedSourceItem;


public class AddRssFeedActivity extends ActionBarActivity {
    public static final int EDITOR_ACTIVITY_REQUEST = 1732;
    private static final int MENU_DELETE_ID = 1234;
    private static final int MENU_EDIT_ID = 1235;
    List<FeedSourceItem> rssSourceList;
    String[] urlList = {"http://www.engadget.com/rss.xml",
            "http://feeds.wired.com/wired/index",
            "http://timesofindia.feedsportal.com/c/33039/f/533965/index.rss",
            "http://feeds.bbci.co.uk/news/rss.xml?edition=uk",
            "http://www.theguardian.com/world/rss",
            "http://feeds.reuters.com/reuters/topNews",
            "http://rss.nytimes.com/services/xml/rss/nyt/InternationalHome.xml",
            "http://www.suhan.in/rss.xml"};
    String[] titleList = {"Engadget", "Wired", "Times Of India", "BBC", "Guardian",
            "Reuter", "Newyork Times", "Anandabazar"};
    private int current_note_id;
    private FeedDataSource rssSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rss_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        registerForContextMenu(findViewById(R.id.listView1));
        rssSource = new FeedDataSource(this);
        refreshDisplay();
    }

    private void refreshDisplay() {
        ListView lView = (ListView) findViewById(R.id.listView1);
        rssSourceList = rssSource.findAll();
        if (rssSourceList.isEmpty()) {
            for (int i = 0; i < urlList.length; i++) {
                FeedSourceItem item = FeedSourceItem.getNew();
                item.setFeedURL(urlList[i]);
                item.setFeedTitle(titleList[i]);
                rssSource.update(item);
            }

            rssSourceList = rssSource.findAll();
        }

        //ArrayAdapter<SMSDataSource> adapter = new ArrayAdapter<SMSDataSource>(this, R.layout.list_item_layout, dataSource);
        FeedSourceAdapter adapter = new FeedSourceAdapter(this, R.layout.source_item_layout, rssSourceList);

        lView.setAdapter(adapter);
        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                saveAndFinish(i);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_rss_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            createNote(FeedSourceItem.getNew());
            return true;
        }

//        if (item.getItemId() == android.R.id.home){
//            saveAndFinish();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private void createNote(FeedSourceItem smsItem) {
        Intent intent = new Intent(AddRssFeedActivity.this, EditRssFeedActivity.class);
        intent.putExtra("key", smsItem.getKey());
        intent.putExtra("title", smsItem.getFeedTitle());
        intent.putExtra("url", smsItem.getFeedURL());
        startActivityForResult(intent, EDITOR_ACTIVITY_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDITOR_ACTIVITY_REQUEST && resultCode == RESULT_OK) {
            FeedSourceItem feedItem = new FeedSourceItem();
            feedItem.setFeedTitle(data.getStringExtra("title").toString());
            feedItem.setFeedURL(data.getStringExtra("url").toString());
            feedItem.setKey(data.getStringExtra("key").toString());
            rssSource.update(feedItem);
            refreshDisplay();
        }
    }

    private void saveAndFinish(int index) {

        Intent intent = new Intent();
        intent.putExtra("index", index);

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        current_note_id = (int) info.id;

        menu.add(1, MENU_DELETE_ID, 0, "Delete");
        menu.add(0, MENU_EDIT_ID, 0, "Edit");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_DELETE_ID) {
            rssSource.remove(rssSourceList.get(current_note_id));
            //Toast.makeText(getApplicationContext(),"Delete Number",Toast.LENGTH_SHORT).show();
            refreshDisplay();
        }
        if (item.getItemId() == MENU_EDIT_ID) {
            createNote(rssSourceList.get(current_note_id));
            //Toast.makeText(getApplicationContext(),"Delete Number",Toast.LENGTH_SHORT).show();
        }

        return super.onContextItemSelected(item);
    }
//
//    public void onBackPressed() {
//        saveAndFinish();
//    }
}
