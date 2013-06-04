package brian.ruth.backissues;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;

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

        EditText textEntry = (EditText) findViewById(R.id.comic_series_text_entry);
        textEntry.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            addComicSeries(v);
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        mBackIssuesDatabase = new BackIssuesDBHelper(this);

        Cursor c = RefreshListCursor();

        final ListView lv = (ListView)findViewById(R.id.comic_series_list);



        String[] uiBindFrom = {ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE};
        int[] uiBindTo = {R.id.comic_series_list_item_title};
        ComicSeriesCursorAdapter adapter = new ComicSeriesCursorAdapter(this, c, true);
        adapter.db = mBackIssuesDatabase.getReadableDatabase();
        lv.setAdapter(adapter);

        //single click to see the issues for a series
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

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
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

    @Override
    public void onResume() {
        super.onResume();

        //refresh the list in case an item was added or removed from
        //a series
        //todo: only update when a change is made, or only update the changed item, not the whole list
        final ListView lv = (ListView)findViewById(R.id.comic_series_list);
        ((BaseAdapter)(lv.getAdapter())).notifyDataSetChanged();

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
            if(isDuplicateOrEmptyTitle(seriesTitle, db) == false) {
                ContentValues values = new ContentValues();
                values.put(ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE, seriesTitle);

                long newRowID = db.insert(ComicSeriesContract.ComicSeriesEntry.TABLE_NAME, "null", values);
            }
        }catch (Exception e) {
            //todo: do something meaningful
            String ex = e.toString();
        }

        editText.setText("");
        ListView lv = (ListView)findViewById(R.id.comic_series_list);
        ((CursorAdapter)lv.getAdapter()).changeCursor(RefreshListCursor());
        lv.requestFocus();
    }


    public void removeComicSeries(int seriesID) {
        try {
            //todo: see if series already in database before adding again
            SQLiteDatabase db = mBackIssuesDatabase.getWritableDatabase();

            //delete all items from issue table first
            String whereClause = ComicSeriesContract.ComicIssueEntry.COLUMN_SERIES_ID + "=" + seriesID;
            db.delete( ComicSeriesContract.ComicIssueEntry.TABLE_NAME,
                    whereClause,
                    null
            );

            //now remove series from series table
            whereClause = ComicSeriesContract.ComicSeriesEntry._ID + "=" + seriesID;
            db.delete(ComicSeriesContract.ComicSeriesEntry.TABLE_NAME,
                    whereClause,
                    null
            );

        }catch (Exception e) {
            //todo: do something meaningful
            String ex = e.toString();
        }

        ListView lv = (ListView)findViewById(R.id.comic_series_list);
        ((CursorAdapter)lv.getAdapter()).changeCursor(RefreshListCursor());
        lv.requestFocus();
    }

    private boolean isDuplicateOrEmptyTitle(String title, SQLiteDatabase db)
    {
        //check empty or all whitespace
        //todo: ensure it looks like a valid title e.g. ".@@@7f" is probably invalid
        if(title.trim().length() == 0 ){
            return true;
        }

        String[] projection = { ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE };
        String rowSelectionQuery = " UPPER(" + ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE + ") = UPPER('" + title + "')";
        Cursor c =  db.query(
                ComicSeriesContract.ComicSeriesEntry.TABLE_NAME,  // The table to query
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
