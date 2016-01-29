package brian.ruth.backissues;

import android.app.Application;

/**
 * Created by bsruth on 1/29/16.
 */
public class BackIssuesApplication extends Application{

    private static BackIssuesDBHelper database = null;

    public BackIssuesApplication() {
    }

    public void setDatabase(BackIssuesDBHelper db) {
        database = db;
    }

    public BackIssuesDBHelper getDatabase() {
        return database;
    }
}
