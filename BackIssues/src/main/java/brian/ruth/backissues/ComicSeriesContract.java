package brian.ruth.backissues;

import android.provider.BaseColumns;

/**
 * Implements the schema for comic series table
 * Created by brian on 5/19/13.
 */
public class ComicSeriesContract {

    public static abstract class ComicSeriesEntry implements BaseColumns {

    public static final String TABLE_NAME = "ComicSeries";
    public static final String COLUMN_NAME_TITLE = "SeriesTitle"; //title to show for each series

    }

    public static abstract class ComicIssueEntry implements BaseColumns {

        public static final String TABLE_NAME = "ComicIssues";
        public static final String COLUMN_SERIES_ID = "SeriesID";
        public static final String COLUMN_ISSUE_NUMBER = "IssueNumber";
        public static final String COLUMN_ISSUE_CHECKED_OFF = "CheckedOff";
    }

    //no constructor since this is just a schema definition
    private ComicSeriesContract() {}
}
