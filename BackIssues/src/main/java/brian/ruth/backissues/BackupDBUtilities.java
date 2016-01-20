package brian.ruth.backissues;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by bsruth on 1/20/16.
 */
public class BackupDBUtilities {
    public static boolean BackupToSDCard(String privateDBPath) {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        String backupDBPath = BackIssuesDBHelper.DATABASE_NAME;
        File currentDB = new File(data, privateDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            FileChannel source=null;
            FileChannel destination=null;
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean RestoreFromSDCard(String privateDBPath ) {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source=null;
        FileChannel destination=null;
        String backupDBPath = BackIssuesDBHelper.DATABASE_NAME;
        File currentDB = new File(data, privateDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(backupDB).getChannel();
            destination = new FileOutputStream(currentDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
