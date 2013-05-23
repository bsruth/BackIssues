package brian.ruth.backissues;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;

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
        String seriesTitle = intent.getStringExtra(ComicSeriesListingActivity.SELECTED_COMIC_SERIES_TITLE);

        setContentView(R.layout.activity_comic_detail_listing);
        setTitle(seriesTitle);

        mBackIssuesDatabase = new BackIssuesDBHelper(this);

        Cursor c = RefreshListCursor();

        final ListView lv = (ListView)findViewById(R.id.comic_issue_list);

        String[] uiBindFrom = {ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER, ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_CHECKED_OFF};
        int[] uiBindTo = {R.id.comic_issue_list_item_title, R.id.comic_issue_list_item_title};
        CursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.comic_issue_list_item_layout, c,uiBindFrom, uiBindTo);
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
            if(isDuplicateOrEmptyIssue(issueNumber, db) == false){
                ContentValues values = new ContentValues();
                values.put(ComicSeriesContract.ComicIssueEntry.COLUMN_SERIES_ID, mSeriesID);
                values.put(ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER, issueNumber);
                values.put(ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_CHECKED_OFF, BackIssuesDBHelper.SQL_FALSE);


                long newRowID = db.insert(ComicSeriesContract.ComicIssueEntry.TABLE_NAME, "null", values);
            }
        }catch (Exception e) {
            //todo: do something meaningful
            String ex = e.toString();
        }

        editText.setText("");
        ListView lv = (ListView)findViewById(R.id.comic_issue_list);
        ((CursorAdapter)lv.getAdapter()).changeCursor(RefreshListCursor());
        lv.requestFocus();
    }

    private boolean isDuplicateOrEmptyIssue(String issue, SQLiteDatabase db)
    {
        //check empty or all whitespace
        //todo: ensure it looks like a valid issue e.g. ".@@@7f" is probably invalid
        if(issue.trim().length() == 0 ){
            return true;
        }

        String[] projection = { ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER};
        String rowSelectionQuery = " UPPER(" + ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER + ") = UPPER('" + issue + "')";
        Cursor c =  db.query(
                ComicSeriesContract.ComicIssueEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                rowSelectionQuery,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        if(c.getCount() == 0) {
            return false; //nothing matches this item
        } else {
            return true;
        }
    }
}