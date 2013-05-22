package brian.ruth.backissues;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

/**
 * Created by brian on 5/21/13.
 */
public class ComicDetailListingActivity extends Activity {

    //members
    private BackIssuesDBHelper mBackIssuesDatabase;
    private String mSeriesID;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get intent
        Intent intent = getIntent();
        mSeriesID = intent.getStringExtra(ComicSeriesListingActivity.SELECTED_COMIC_SERIES_ID);

        setContentView(R.layout.activity_comic_detail_listing);

        mBackIssuesDatabase = new BackIssuesDBHelper(this);

        Cursor c = RefreshListCursor();

        final ListView lv = (ListView)findViewById(R.id.comic_issue_list);

        String[] uiBindFrom = {ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER};
        int[] uiBindTo = {R.id.comic_series_list_item_title};
        CursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.comic_series_list_item_layout, c,uiBindFrom, uiBindTo);
        lv.setAdapter(adapter);
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
        String sortOrder =
                ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER + " ASC;";
        String rowSelection = ComicSeriesContract.ComicIssueEntry.COLUMN_SERIES_ID + "=" + mSeriesID;

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
        try {
            //todo: see if series already in database before adding again
            SQLiteDatabase db = mBackIssuesDatabase.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ComicSeriesContract.ComicIssueEntry.COLUMN_SERIES_ID, mSeriesID);
            values.put(ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER, issueNumber);
            values.put(ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_CHECKED_OFF, BackIssuesDBHelper.SQL_FALSE);


            long newRowID = db.insert(ComicSeriesContract.ComicIssueEntry.TABLE_NAME, "null", values);
        }catch (Exception e) {
            //todo: do something meaningful
            String ex = e.toString();
        }

        ListView lv = (ListView)findViewById(R.id.comic_issue_list);
        ((CursorAdapter)lv.getAdapter()).changeCursor(RefreshListCursor());
    }
}