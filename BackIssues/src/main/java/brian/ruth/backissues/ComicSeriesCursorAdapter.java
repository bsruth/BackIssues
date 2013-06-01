package brian.ruth.backissues;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by brian on 5/30/13.
 */
public class ComicSeriesCursorAdapter extends CursorAdapter{

    public SQLiteDatabase db;

    public ComicSeriesCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        //return null;
        // when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View retView = inflater.inflate(R.layout.comic_series_list_item_layout, viewGroup, false);


        return retView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // here we are setting our data
        // that means, take the data from the cursor and put it in views

        TextView textViewPersonName = (TextView) view.findViewById(R.id.comic_series_list_item_title);
        textViewPersonName.setText(cursor.getString(cursor.getColumnIndex(ComicSeriesContract.ComicSeriesEntry.COLUMN_NAME_TITLE)));


        if(db != null) {
            String[] projection = {
                    ComicSeriesContract.ComicIssueEntry.COLUMN_SERIES_ID,
            };

            int seriesID = cursor.getInt(cursor.getColumnIndex(ComicSeriesContract.ComicSeriesEntry._ID));
            String rowSelection = ComicSeriesContract.ComicIssueEntry.COLUMN_SERIES_ID + "=" + seriesID;

            try {
                Cursor c = db.query(
                        ComicSeriesContract.ComicIssueEntry.TABLE_NAME,  // The table to query
                        projection,                               // The columns to return
                        rowSelection,                                // The columns for the WHERE clause
                        null,                            // The values for the WHERE clause
                        null,                                     // don't group the rows
                        null,                                     // don't filter by row groups
                        null                                 // The sort order
                );

                TextView textViewCount = (TextView) view.findViewById(R.id.issue_count);
                textViewCount.setText(String.valueOf(c.getCount()));
            } catch (Exception ex) {

                String e = ex.toString();
                int i = 0;
            }

        }
    }
}
