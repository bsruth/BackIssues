package brian.ruth.backissues;

import android.os.Environment;

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
        File currentDB = new File(privateDBPath);
        String dbFileName = currentDB.getName();
        File backupDB = new File(sd, dbFileName);
        return CopyDatabase(currentDB, backupDB);
    }

    public static boolean RestoreFromSDCard(String privateDBPath ) {
        File sd = Environment.getExternalStorageDirectory();
        File currentDB = new File(privateDBPath);
        String dbFileName = currentDB.getName();
        File backupDB = new File(sd, dbFileName);
        return CopyDatabase(backupDB, currentDB);
    }

    private static boolean CopyDatabase(File source, File dest) {
        try {
            FileChannel sourceChannel = new FileInputStream(source).getChannel();
            FileChannel destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            sourceChannel.close();
            destChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
