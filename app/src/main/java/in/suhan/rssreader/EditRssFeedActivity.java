package in.suhan.rssreader;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class EditRssFeedActivity extends ActionBarActivity {
    public static final int EDITOR_ACTIVITY_REQUEST = 1732;
    private String key;
    private String url;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_rss_feed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = this.getIntent();
        key = intent.getStringExtra("key");
        url = intent.getStringExtra("url");
        title = intent.getStringExtra("title");

        EditText et = (EditText) findViewById(R.id.editText);

        et.setText(title);
        //et.setSelection(phoneNo.length());

        et = (EditText) findViewById(R.id.editText2);
        et.setText(url);
        et.setSelection(0);
    }

    private void saveAndFinish() {
        EditText et = (EditText) findViewById(R.id.editText);
        title = et.getText().toString();

        et = (EditText) findViewById(R.id.editText2);
        url = et.getText().toString();


        Intent intent = new Intent();
        intent.putExtra("key", key);
        intent.putExtra("title", title);
        intent.putExtra("url", url);

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_rss_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (item.getItemId() == android.R.id.home) {
            saveAndFinish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        saveAndFinish();
    }
}
