package brian.ruth.backissues;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bsruth on 1/22/16.
 */
public class MissingSeries {

    public static long INVALID_SERIES_ID = -1;
    private BackIssuesDBHelper backIssuesDatabase;
    private ComicSeriesCursorAdapter adapter = null;
    private List<ComicSeries> seriesList = new ArrayList<ComicSeries>();

    public MissingSeries(Context context, BackIssuesDBHelper database) {
        backIssuesDatabase = database;
        Cursor seriesCursor = getFilteredCursor("");
        adapter = new ComicSeriesCursorAdapter(context, seriesCursor, true);
        adapter.db = backIssuesDatabase.getReadableDatabase();
    }

    public CursorAdapter getCursorAdapter() {
        return adapter;
    }


    public long addComicSeries(String seriesTitle) {

        //ArrayList<String> searchResults =  searchComicBookDB(title);

        long newRowID = -1;
        try {
            //SelectComicbookDBSeriesDialog selectionDlg = new SelectComicbookDBSeriesDialog();
            //selectionDlg.selctionOptions = new String[searchResults.size()];
            //selectionDlg.selctionOptions = searchResults.toArray(selectionDlg.selctionOptions);
            //selectionDlg.show(getSupportFragmentManager(), "SelectComicbookDBSeriesDialog");
            //String item = selectionDlg.selectedItem;

            //todo: see if series already in database before adding again
            SQLiteDatabase db = backIssuesDatabase.getWritableDatabase();
            if(isDuplicateOrEmptyTitle(seriesTitle, db) == false) {
                ContentValues values = new ContentValues();
                values.put(ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE, seriesTitle);

                newRowID = db.insert(ComicSeriesContract.ComicSeriesEntry.TABLE_NAME, "null", values);
            }
        }catch (Exception e) {
            //todo: do something meaningful
            String ex = e.toString();
        }

        return newRowID;
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

    public boolean removeComicSeries(int seriesID) {
        try {
            //todo: see if series already in database before adding again
            SQLiteDatabase db = backIssuesDatabase.getWritableDatabase();

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
            return false;
        }
        return true;
    }

    private Cursor getFilteredCursor(String filterText) {
        SQLiteDatabase db = backIssuesDatabase.getReadableDatabase();



        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ComicSeriesContract.ComicSeriesEntry._ID,
                ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE
        };


        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE + " COLLATE NOCASE ASC;";

        String row_select = null;
        if(filterText != "") {
            row_select = "UPPER(" + ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE + ") LIKE UPPER('%" +
                    filterText + "%')";

        }

        Cursor seriesCursor = db.query(
                ComicSeriesContract.ComicSeriesEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                row_select,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        return seriesCursor;
    }

    public void filterSeriesList(String filterText){
        adapter.changeCursor(getFilteredCursor(filterText));
    }
}
