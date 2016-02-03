package brian.ruth.backissues;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by brian on 5/21/13.
 */
public class ComicDetailListingActivity extends Activity {

    //members
    private BackIssuesDBHelper mBackIssuesDatabase;
    private ComicSeries currentSeries;
    private List<ComicSeries> mostRecentlyViewed = null;
    //todo: should be an enum
    private int mVisibilityState;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVisibilityState = 0;

        //get intent
        Intent intent = getIntent();
        String title = intent.getStringExtra(ComicSeriesListingActivity.SELECTED_COMIC_SERIES_TITLE);
        long id = intent.getLongExtra(ComicSeriesListingActivity.SELECTED_COMIC_SERIES_ID, -1);
        BackIssuesApplication backIssuesApp = (BackIssuesApplication)getApplicationContext();
        mBackIssuesDatabase = backIssuesApp.getDatabase();

        currentSeries = new ComicSeries(title, id, mBackIssuesDatabase);
        mostRecentlyViewed = backIssuesApp.getMostRecentlyViewedSeries();

        setContentView(R.layout.activity_comic_detail_listing);


        EditText textEntry = (EditText) findViewById(R.id.comic_issues_text_entry);
        textEntry.addTextChangedListener(filterTextWatcher);

        //we want to remove all crossed off items on create so that
        //if we accidentally cross off an item, it is still shown until
        //this activity is recreated, giving the user a chance to re-add
        //it to the missing list.
        RemoveCrossedOffItems();

        loadSeries(currentSeries);

        final ListView lv = (ListView)findViewById(R.id.comic_issue_list);

        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor c2 = (Cursor)lv.getItemAtPosition(i);
                int checked = c2.getInt(c2.getColumnIndex(ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_CHECKED_OFF));

                try {
                    //todo: see if series already in database before adding again
                    SQLiteDatabase db = mBackIssuesDatabase.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    int checkedUpdateState = (checked == BackIssuesDBHelper.SQL_FALSE) ? BackIssuesDBHelper.SQL_TRUE : BackIssuesDBHelper.SQL_FALSE;
                    values.put(ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_CHECKED_OFF, checkedUpdateState);

                    int rowID = c2.getInt(c2.getColumnIndex(ComicSeriesContract.ComicIssueEntry._ID));
                    String rowSelect = ComicSeriesContract.ComicIssueEntry._ID + "=" + rowID;
                    db.update(ComicSeriesContract.ComicIssueEntry.TABLE_NAME, values, rowSelect, null);


                    ((CursorAdapter)lv.getAdapter()).changeCursor(RefreshListCursor());
                }catch (Exception e) {
                    //todo: do something meaningful
                    String ex = e.toString();
                }
                return true;
            }
        });

        //set focus to listview must be done last or we don't get focus
        lv.requestFocus();
    }

    private void loadSeries(ComicSeries series) {
        currentSeries = series;
        Cursor listCursor = RefreshListCursor();

        final ListView lv = (ListView)findViewById(R.id.comic_issue_list);

        String[] uiBindFrom = {ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER, ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_CHECKED_OFF};
        int[] uiBindTo = {R.id.comic_issue_list_item_title, R.id.comic_issue_list_item_title};
        CursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.comic_issue_list_item_layout, listCursor,uiBindFrom, uiBindTo);
        ((SimpleCursorAdapter)adapter).setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_CHECKED_OFF)) {
                    //if the comic is checked off, we will cross it out and make it a lighter color.
                    if (cursor.getInt(columnIndex) > 0) {
                        TextView tv = (TextView) view;
                        SpannableString content = new SpannableString(cursor.getString(cursor.getColumnIndex(ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER)));
                        content.setSpan(new StrikethroughSpan(), 0, content.length(), 0);
                        tv.setTextColor(Color.parseColor("#FFCCCCCC"));
                        tv.setText(content);
                    } else {
                        TextView tv = (TextView) view;
                        tv.setTextColor(Color.BLACK);
                        tv.setText(cursor.getString(cursor.getColumnIndex(ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER)));
                    }
                    return true;
                }
                return false;
            }
        });
        lv.setAdapter(adapter);

        BackIssuesApplication backIssuesApp = (BackIssuesApplication)getApplicationContext();
        backIssuesApp.pushRecentlyViewedSeries(series);

        setTitle(currentSeries.getTitle());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.comic_detail_listing_menu, menu);
        for (ComicSeries comic : mostRecentlyViewed) {
            menu.add(comic.getTitle());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.checked_off_visibility:
                mVisibilityState = (mVisibilityState == 0) ? 1 : 0;
                if(mVisibilityState == 0) {
                 //todo: show all missing issues.
                }else {
                    getAllOwnedIssues();
                }
                return true;
            default:
                for(int seriesIndex = 0; seriesIndex < mostRecentlyViewed.size(); ++seriesIndex) {
                    if(mostRecentlyViewed.get(seriesIndex).getTitle() == item.getTitle()) {
                        loadSeries(mostRecentlyViewed.get(seriesIndex));

                        break;
                    }
                }
                return super.onOptionsItemSelected(item);
        }
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

            ListView lv = (ListView)findViewById(R.id.comic_issue_list);
            ((CursorAdapter)lv.getAdapter()).changeCursor(RefreshListCursor());
        }
    };

    //generate a list of all items that are not in the database
    //for this series or are crossed off. This is the list of items
    //that are owned for this series.
    private List<String> getAllOwnedIssues() {
        SQLiteDatabase db = mBackIssuesDatabase.getWritableDatabase();

        List<String> ownedIssueList = new ArrayList<String>();

        //first get all issues in the DB for this series
        Cursor c = RefreshListCursor();

        //now iterate over the cursor list, adding skipped items until
        //all rows in the db have been processed
        //todo: we should find out how many issues are in this series and loop until that is hit

        //todo: this only works with numeric only issues
        int issueNumberToAdd = 0;
        while(c.moveToNext() ) {

            int issueNumber = c.getInt(c.getColumnIndex(ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER));
            int checkedOff = c.getInt(c.getColumnIndex(ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_CHECKED_OFF));
            while ( issueNumberToAdd  < issueNumber) {
                ownedIssueList.add(String.valueOf(issueNumberToAdd));
                ++issueNumberToAdd;
            }

            if(checkedOff == BackIssuesDBHelper.SQL_TRUE) {
                //add it to the list because it has been checked off
                //but not yet removed from the DB.
                ownedIssueList.add(String.valueOf(issueNumber));
            } else {
                //skip it since it is missing
                issueNumberToAdd = issueNumber + 1;
            }
        }

        db.close();

        return ownedIssueList;
    }

    //removes crossed off items from the database. no sense keeping them
    //around if they are no longer missing
    public void RemoveCrossedOffItems() {

        SQLiteDatabase db = mBackIssuesDatabase.getWritableDatabase();
        String delete_criteria = ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_CHECKED_OFF + "=" + BackIssuesDBHelper.SQL_TRUE + ";";

        db.delete( ComicSeriesContract.ComicIssueEntry.TABLE_NAME,
                delete_criteria,
                null
                );

        db.close();

    }
    public Cursor RefreshListCursor(){

        SQLiteDatabase db = mBackIssuesDatabase.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                ComicSeriesContract.ComicIssueEntry._ID,
                ComicSeriesContract.ComicIssueEntry.COLUMN_SERIES_ID,
                ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER,
                ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_CHECKED_OFF
        };

// How you want the results sorted in the resulting Cursor
        //casting to numbers so that 99 comes before 834 without requiring 0 padding.
        String sortOrder = " cast(" + ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER + " as unsigned) ASC;";

        //String sortOrder =
        //        ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER + " ASC;";
        String rowSelection = ComicSeriesContract.ComicIssueEntry.COLUMN_SERIES_ID + "=" + currentSeries.getID();

        //see if we need to filter the cursor based on the current filter text
        EditText editText = (EditText) findViewById(R.id.comic_issues_text_entry);
        String filterText = editText.getText().toString();
        if(filterText != "") {
            rowSelection = rowSelection + " AND UPPER(" + ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER+ ") LIKE UPPER('" +
                    filterText + "%')";

        }

        try {
        Cursor c = db.query(
                ComicSeriesContract.ComicIssueEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                rowSelection,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

            return c;
        } catch (Exception ex) {

         String e = ex.toString();
            int i = 0;
        }
        return null;
    }

    /** Called when the user clicks the Send button*/
    public void addComicIssue(View view) {
        EditText editText = (EditText) findViewById(R.id.comic_issues_text_entry);
        String issueNumber = editText.getText().toString();
        if(currentSeries.addComicIssue(issueNumber) == false) {
            Toast.makeText(this, "Failed to add series: " + issueNumber,
                    Toast.LENGTH_LONG).show();
        }

        editText.setText("");
        ListView lv = (ListView)findViewById(R.id.comic_issue_list);
        ((CursorAdapter)lv.getAdapter()).changeCursor(RefreshListCursor());
        editText.requestFocus();
    }
}