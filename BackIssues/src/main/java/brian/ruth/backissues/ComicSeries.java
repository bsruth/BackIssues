package brian.ruth.backissues;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bsruth on 1/27/16.
 */
public class ComicSeries implements Serializable {

    private String title;
    private long id;
    //TODO: DB is not serializable.
    private BackIssuesDBHelper backIssuesDatabase = null;

    public ComicSeries(String title, long id, BackIssuesDBHelper database) {
        this.title = title;
        this.id = id;
        backIssuesDatabase = database;
    }

    public String getTitle() {
        return title;
    }

    public long getID() {
        return id;
    }

    public boolean addComicIssue(String issueAddCode) {

        //parse to see if we are adding a list
        ArrayList<String> issuesToAdd = ParseIssuesToAdd(issueAddCode);
        try {
            //todo: see if series already in database before adding again
            SQLiteDatabase db = backIssuesDatabase.getWritableDatabase();
            for(int issue = 0; issue < issuesToAdd.size(); ++issue ) {
                String issueToAdd = issuesToAdd.get(issue);
                if(isDuplicateOrEmptyIssue(issueToAdd, db) == false){
                    ContentValues values = new ContentValues();
                    values.put(ComicSeriesContract.ComicIssueEntry.COLUMN_SERIES_ID, id);
                    values.put(ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER, issueToAdd);
                    values.put(ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_CHECKED_OFF, BackIssuesDBHelper.SQL_FALSE);


                    long newRowID = db.insert(ComicSeriesContract.ComicIssueEntry.TABLE_NAME, "null", values);
                }
            }
        }catch (Exception e) {
            return false;
        }

        return true;
    }

    //** used to add a group of issues at once
    private ArrayList<String> ParseIssuesToAdd(String addString)
    {
        ArrayList<String> issuesToAdd = new ArrayList<String>();

        //todo: doesn't handle decimal issues or things like "annual 1 - 10"
        Pattern pattern = Pattern.compile("(\\d+)\\s*\\-\\s*(\\d+)");

        Matcher matcher = pattern.matcher(addString);
        if(matcher.matches()) {
            int startNumber = Integer.parseInt(matcher.group(1));
            int endNumber = Integer.parseInt(matcher.group(2));

            for(int issueNumber = startNumber; issueNumber <= endNumber; ++issueNumber){
                issuesToAdd.add(String.valueOf(issueNumber) );
            }
        } else {
            //wasn't a range add, just put in the text passed to this function
            issuesToAdd.add(addString);
        }


        return issuesToAdd;
    }


    private boolean isDuplicateOrEmptyIssue(String issue, SQLiteDatabase db)
    {
        //check empty or all whitespace
        //todo: ensure it looks like a valid issue e.g. ".@@@7f" is probably invalid
        if(issue.trim().length() == 0 ){
            return true;
        }

        String[] projection = { ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER};
        String rowSelectionQuery = " UPPER(" + ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER + ") = UPPER('" + issue + "') " +
                "AND " + ComicSeriesContract.ComicIssueEntry.COLUMN_SERIES_ID + "=" + id;
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
