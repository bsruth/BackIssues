package brian.ruth.backissues;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by brian on 5/19/13.
 */
public class BackIssuesDBHelper extends SQLiteOpenHelper {

    //SQL helper constants
    private static final String TEXT_TYPE = " TEXT";
    private static final String BOOL_TYPE = " INTEGER"; //no built in bool type
    private static final String COMMA_SEP = ", ";
    public static final int SQL_TRUE = 1; //boolean constants
    public static final int SQL_FALSE = 0;


    //ComicSeries Table creation
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ComicSeriesContract.ComicSeriesEntry.TABLE_NAME + " (" +
                    ComicSeriesContract.ComicSeriesEntry._ID + " INTEGER PRIMARY KEY, " +
                    ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE + TEXT_TYPE +
                    " );";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ComicSeriesContract.ComicSeriesEntry.TABLE_NAME;


    //ComicIssues Table creation
    //NOTE: issue number is text since issue numbers can contain
    //non-numeric characters (e.g. Annual 1, 6AU, etc.)
    private static final String SQL_CREATE_ISSUE_ENTRIES =
            "CREATE TABLE " + ComicSeriesContract.ComicIssueEntry.TABLE_NAME + " (" +
                    ComicSeriesContract.ComicIssueEntry._ID + " INTEGER PRIMARY KEY, " +
                    ComicSeriesContract.ComicIssueEntry.COLUMN_SERIES_ID + " INTEGER, " +
                    ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_NUMBER + TEXT_TYPE + COMMA_SEP +
                    ComicSeriesContract.ComicIssueEntry.COLUMN_ISSUE_CHECKED_OFF + BOOL_TYPE +
                    " );";

    private static final String SQL_DELETE_ISSUE_ENTRIES =
            "DROP TABLE IF EXISTS " + ComicSeriesContract.ComicIssueEntry.TABLE_NAME;



    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "BackIssues.db";

    public BackIssuesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ISSUE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_ISSUE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


}
