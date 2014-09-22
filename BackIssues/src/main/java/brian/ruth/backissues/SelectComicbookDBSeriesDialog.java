package brian.ruth.backissues;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by brian on 6/18/13.
 */
public class SelectComicbookDBSeriesDialog extends DialogFragment {

        public String[] selctionOptions;

        public String selectedItem;

    public SelectComicbookDBSeriesDialog() {
        selctionOptions = new String[0];
        selectedItem = new String();
    }

    @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select series from Comicbook DB List").setItems(selctionOptions, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // The 'which' argument contains the index position
                    // of the selected item
                    selectedItem = selctionOptions[which];
                }
            });
            return builder.create();
    }
}
