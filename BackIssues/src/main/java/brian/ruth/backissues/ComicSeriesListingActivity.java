package brian.ruth.backissues;

import android.app.AlertDialog;
import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComicSeriesListingActivity extends FragmentActivity {

    //activity messages
    public final static String SELECTED_COMIC_SERIES_ID = "brian.ruth.backissues.SELECTED_COMIC_SERIES_ID";
    public final static String SELECTED_COMIC_SERIES_TITLE = "brian.ruth.backissues.SELECTED_COMIC_SERIES_TITLE";

    //members
    private BackIssuesDBHelper mBackIssuesDatabase; //database used for entire app
    private ComicSeriesCursorAdapter adapter = null;
    private MissingSeries missingSeries = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_series_listing);

        EditText textEntry = (EditText) findViewById(R.id.comic_series_text_entry);
        textEntry.addTextChangedListener(filterTextWatcher);

        mBackIssuesDatabase = new BackIssuesDBHelper(this);
        missingSeries = new MissingSeries(mBackIssuesDatabase);

        final ListView lv = (ListView)findViewById(R.id.comic_series_list);



        String[] uiBindFrom = {ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE};
        int[] uiBindTo = {R.id.comic_series_list_item_title};
        Cursor seriesCursor = missingSeries.getFilteredCursor("");
        adapter = new ComicSeriesCursorAdapter(this, seriesCursor, true);
        adapter.db = mBackIssuesDatabase.getReadableDatabase();
        lv.setAdapter(adapter);

        //single click to see the issues for a series
        lv.setClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor c2 = (Cursor)lv.getItemAtPosition(i);
                long seriesID = c2.getLong(c2.getColumnIndex(ComicSeriesContract.ComicSeriesEntry._ID));
                String seriesTitle = c2.getString(c2.getColumnIndex(ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE));
               openComicDetailListingActivity(view, seriesTitle, seriesID);
            }
        });

        //long click to remove a series
        //TODO: possibly long click to change things as well.
        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                Cursor c2 = (Cursor)lv.getItemAtPosition(i);


                String seriesTitle = c2.getString(c2.getColumnIndex(ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE));
                final int seriesID = c2.getInt(c2.getColumnIndex(ComicSeriesContract.ComicSeriesEntry._ID));

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                removeComicSeries(seriesID);
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Do you want to delete " + seriesTitle + "?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                return true;
            }

        });

        //set focus to listview must be done last or we don't get focus
        lv.requestFocus();
    }

    //needed to watch as text is typed into the edit box
    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

            refreshSeriesList(refreshSeriesCursor());
        }
    };



    @Override
    public void onResume() {
        super.onResume();

        //refresh the list in case an item was added or removed from
        //a series
        //todo: only update when a change is made, or only update the changed item, not the whole list
        adapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.comic_series_listing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_backupDB:
                BackupToSDCard();
                return true;
            case R.id.action_restoreDB:
                RestoreFromSDCard();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void BackupToSDCard() {
        if(BackupDBUtilities.BackupToSDCard(mBackIssuesDatabase.DB_PATH)) {
            Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "FAILED to export DB!", Toast.LENGTH_LONG).show();
        }
    }

    public void RestoreFromSDCard() {
        if(BackupDBUtilities.RestoreFromSDCard(mBackIssuesDatabase.DB_PATH)) {
            mBackIssuesDatabase.close();
            mBackIssuesDatabase = new BackIssuesDBHelper(this);
            refreshSeriesList(refreshSeriesCursor());

            Toast.makeText(this, "DB Restored!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "FAILED To Restore DB", Toast.LENGTH_LONG).show();
        }
    }


    public Cursor refreshSeriesCursor(){

        EditText editText = (EditText) findViewById(R.id.comic_series_text_entry);
        String filterText = editText.getText().toString();
       return missingSeries.getFilteredCursor(filterText);
    }

    public void refreshSeriesList(Cursor seriesCursor) {
        ListView lv = (ListView)findViewById(R.id.comic_series_list);
        adapter.changeCursor(seriesCursor);
    }

    /** Called when the user clicks the Send button*/
    public void addComicSeries(View view) {
        EditText editText = (EditText) findViewById(R.id.comic_series_text_entry);
        String seriesTitle = editText.getText().toString();

        long newSeriesID = missingSeries.addComicSeries(seriesTitle);
        if(newSeriesID != MissingSeries.INVALID_SERIES_ID) {
            editText.setText("");
            openComicDetailListingActivity(view, seriesTitle, newSeriesID);
        }
    }

    private void openComicDetailListingActivity(View view, String seriesTitle, long seriesID) {
        Intent intent = new Intent(view.getContext(), ComicDetailListingActivity.class);
        intent.putExtra(SELECTED_COMIC_SERIES_ID, Long.toString(seriesID));
        intent.putExtra(SELECTED_COMIC_SERIES_TITLE, seriesTitle);
        startActivity(intent);
    }

    public void removeComicSeries(int seriesID) {
        if(missingSeries.removeComicSeries(seriesID)) {
            refreshSeriesList(refreshSeriesCursor());
        } else {
            Toast.makeText(this, "Failed to remove series", Toast.LENGTH_LONG).show();
        }
    }


    
}
