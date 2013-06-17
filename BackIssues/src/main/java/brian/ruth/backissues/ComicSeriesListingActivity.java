package brian.ruth.backissues;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComicSeriesListingActivity extends Activity {

    //activity messages
    public final static String SELECTED_COMIC_SERIES_ID = "brian.ruth.backissues.SELECTED_COMIC_SERIES_ID";
    public final static String SELECTED_COMIC_SERIES_TITLE = "brian.ruth.backissues.SELECTED_COMIC_SERIES_TITLE";

    //members
    private BackIssuesDBHelper mBackIssuesDatabase; //database used for entire app
    private ComicSeriesCursorAdapter adapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_series_listing);

        EditText textEntry = (EditText) findViewById(R.id.comic_series_text_entry);
        textEntry.addTextChangedListener(filterTextWatcher);

        mBackIssuesDatabase = new BackIssuesDBHelper(this);

        Cursor c = RefreshListCursor();

        final ListView lv = (ListView)findViewById(R.id.comic_series_list);



        String[] uiBindFrom = {ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE};
        int[] uiBindTo = {R.id.comic_series_list_item_title};
        adapter = new ComicSeriesCursorAdapter(this, c, true);
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

    //needed to watch as text is typed into the edit box
    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

            ListView lv = (ListView)findViewById(R.id.comic_series_list);
            ((CursorAdapter)lv.getAdapter()).changeCursor(RefreshListCursor());
        }
    };



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

    public ArrayList<String> searchComicBookDB(String searchString) {

        ArrayList<String> searchResults = new ArrayList<String>();

        try {
            //converts all special chars to their % equivalents for URLs
            String encodedSerchString = URLEncoder.encode(searchString, "utf-8");

            HttpClient client = new DefaultHttpClient();
            //TODO: format the search string for the comicbookdb search
            String url = "http://mobile.comicbookdb.com/search.php?form_search=" + encodedSerchString + "&form_searchtype=Title";
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            InputStream in = response.getEntity().getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null)
            {
                str.append(line);
            }
            in.close();


            String html = str.toString();


            //find all links to tiltes in the search
            //NOTE: \\p{P} is supposedly all punctuation
            Pattern pattern = Pattern.compile("<a href=\"title.php\\?ID=(\\d+)\">([\\w\\s\\p{P}]+)\\((\\d+)\\)</a>\\s*\\(([\\w\\s]+)\\)\\s*<br>");

            Matcher matcher = pattern.matcher(html);
            while(matcher.find()) {
                //fromHTML decodes all HTML special chars to printable chars (e.g. amp; to &)
                searchResults.add( Html.fromHtml(matcher.group(2)).toString() + " " + Html.fromHtml(matcher.group(3)).toString());
            }
        } catch (Exception ex) {
            String e = ex.toString();
        }


        return searchResults;
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

        //see if we need to filter the cursor based on the current filter text
        EditText editText = (EditText) findViewById(R.id.comic_series_text_entry);
        String filterText = editText.getText().toString();
        String row_select = null;
        if(filterText != "") {
            row_select = "UPPER(" + ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE + ") LIKE UPPER('%" +
                    filterText + "%')";

        }

        Cursor c = db.query(
                ComicSeriesContract.ComicSeriesEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                row_select,                                // The columns for the WHERE clause
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

        ArrayList<String> searchResults =  searchComicBookDB(seriesTitle);

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
