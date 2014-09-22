package brian.ruth.backissues;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;

/**
 * Created by brian on 9/1/14.
 */
public class BackIssuesBackupAgent extends BackupAgentHelper{

    public static final Object[] DATA_LOCK = new Object[0];
    static final String FILES_BACKUP_KEY = "backissuesDBBackup";

    @Override
    public void onCreate() {
        Log.d(toString(), "onCreate()");
        FileBackupHelper helper = new FileBackupHelper(this, BackIssuesDBHelper.DATABASE_NAME);
        addHelper(FILES_BACKUP_KEY, helper);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
                         ParcelFileDescriptor newState) throws IOException {

        Log.d(toString(), "onBackup()");
        synchronized (DATA_LOCK) {
            super.onBackup(oldState, data, newState);
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState)
            throws IOException {
        Log.d(toString(), "onRestore()");
        synchronized (DATA_LOCK) {
            super.onRestore(data, appVersionCode, newState);
        }
    }
}
