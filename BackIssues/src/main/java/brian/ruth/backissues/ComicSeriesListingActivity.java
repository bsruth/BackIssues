package brian.ruth.backissues;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class ComicSeriesListingActivity extends Activity {

    //members
    private BackIssuesDBHelper mBackIssuesDatabase; //database used for entire app

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_series_listing);

        mBackIssuesDatabase = new BackIssuesDBHelper(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.comic_series_listing, menu);
        return true;
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

    }
    
}
