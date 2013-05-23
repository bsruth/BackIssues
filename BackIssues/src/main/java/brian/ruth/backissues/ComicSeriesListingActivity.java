package brian.ruth.backissues;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.*;

public class ComicSeriesListingActivity extends Activity {

    //activity messages
    public final static String SELECTED_COMIC_SERIES_ID = "brian.ruth.backissues.SELECTED_COMIC_SERIES_ID";
    public final static String SELECTED_COMIC_SERIES_TITLE = "brian.ruth.backissues.SELECTED_COMIC_SERIES_TITLE";

    //members
    private BackIssuesDBHelper mBackIssuesDatabase; //database used for entire app

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_series_listing);

        mBackIssuesDatabase = new BackIssuesDBHelper(this);

        Cursor c = RefreshListCursor();

        final ListView lv = (ListView)findViewById(R.id.comic_series_list);



        String[] uiBindFrom = {ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE};
        int[] uiBindTo = {R.id.comic_series_list_item_title};
        CursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.comic_series_list_item_layout, c,uiBindFrom, uiBindTo);
        lv.setAdapter(adapter);
        lv.setClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(view.getContext(), ComicDetailListingActivity.class);
                Cursor c2 = (Cursor)lv.getItemAtPosition(i);
                String seriesID = c2.getString(c2.getColumnIndex(ComicSeriesContract.ComicSeriesEntry._ID));
                intent.putExtra(SELECTED_COMIC_SERIES_ID, seriesID);

                String seriesTitle = c2.getString(c2.getColumnIndex(ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE));
                intent.putExtra(SELECTED_COMIC_SERIES_TITLE, seriesTitle);

                startActivity(intent);
            }
        });

        //set focus to listview must be done last or we don't get focus
        lv.requestFocus();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.comic_series_listing, menu);
        return true;
    }


    public Cursor RefreshListCursor(){
        SQLiteDatabase db = mBackIssuesDatabase.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                ComicSeriesContract.ComicSeriesEntry._ID,
                ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE
        };

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE + " ASC;";
        Cursor c = db.query(
                ComicSeriesContract.ComicSeriesEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        return c;
    }

    /** Called when the user clicks the Send button*/
    public void addComicSeries(View view) {
        EditText editText = (EditText) findViewById(R.id.comic_series_text_entry);
        String seriesTitle = editText.getText().toString();
        try {
            //todo: see if series already in database before adding again
            SQLiteDatabase db = mBackIssuesDatabase.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE, seriesTitle);

            long newRowID = db.insert(ComicSeriesContract.ComicSeriesEntry.TABLE_NAME, "null", values);
        }catch (Exception e) {
            //todo: do something meaningful
            String ex = e.toString();
        }

        ListView lv = (ListView)findViewById(R.id.comic_series_list);
        ((CursorAdapter)lv.getAdapter()).changeCursor(RefreshListCursor());
    }
    
}
