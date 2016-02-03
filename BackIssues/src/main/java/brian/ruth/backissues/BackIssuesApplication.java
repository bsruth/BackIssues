package brian.ruth.backissues;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bsruth on 1/29/16.
 */
public class BackIssuesApplication extends Application{

    private BackIssuesDBHelper database = null;
    private List<ComicSeries> mostRecentlyViewedSeries = new ArrayList<ComicSeries>();
    private final int MAX_RECENTLY_VIEWED = 4;
    public BackIssuesApplication() {
    }

    public void setDatabase(BackIssuesDBHelper db) {
        database = db;
    }

    public BackIssuesDBHelper getDatabase() {
        return database;
    }

    public void pushRecentlyViewedSeries(ComicSeries series) {
        //find duplicate and remove, it will be added back at the end.
        for(int seriesIndex = 0; seriesIndex < mostRecentlyViewedSeries.size(); ++seriesIndex) {
            if(mostRecentlyViewedSeries.get(seriesIndex).getID() == series.getID()) {
                mostRecentlyViewedSeries.remove(seriesIndex);
                break;
            }
        }

        if (mostRecentlyViewedSeries.size() == MAX_RECENTLY_VIEWED) {
            mostRecentlyViewedSeries.remove(0);
        }
        mostRecentlyViewedSeries.add(series);
    }

    public List<ComicSeries> getMostRecentlyViewedSeries() {
        return mostRecentlyViewedSeries;
    }

}
