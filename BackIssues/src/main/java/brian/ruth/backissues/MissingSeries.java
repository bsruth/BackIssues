package brian.ruth.backissues;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by bsruth on 1/22/16.
 */
public class MissingSeries {

    public static long INVALID_SERIES_ID = -1;
    private BackIssuesDBHelper backIssuesDatabase;

    public MissingSeries(BackIssuesDBHelper database) {
       backIssuesDatabase = database;
    }

    /** Called when the user clicks the Send button*/
    public long addComicSeries(String seriesTitle) {

        //ArrayList<String> searchResults =  searchComicBookDB(seriesTitle);

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
}
