package brian.ruth.backissues;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by brian on 5/21/13.
 */
public class ComicDetailListingActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get intent
        Intent intent = getIntent();
        String comicSeriesID = intent.getStringExtra(ComicSeriesListingActivity.SELECTED_COMIC_SERIES_ID);

        setContentView(R.layout.activity_comic_detail_listing);

        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText(comicSeriesID);
    }
}