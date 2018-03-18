package sam.van.roy.ocr_app;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by samva on 18/03/2018.
 */

public class Helper {
    public static AlertDialog showLoadingDialog(Context activity){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater) activity.getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View layout = inflater.inflate(R.layout.load_dialog, null);

        mBuilder.setView(layout);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        showExtraMessageIfLoadingTakesTooLong(dialog);

        return dialog;
    }

    public static void showExtraMessageIfLoadingTakesTooLong(final AlertDialog loadDialog){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(loadDialog.isShowing()){
                    TextView loadDialogMessage = loadDialog.getWindow().findViewById(R.id.load_dialog_message);
                    loadDialogMessage.setVisibility(View.VISIBLE);
                }
            }
        }, 5000);
    }

    public static AlertDialog dismissLoadingDialog(AlertDialog loadDialog){
        if(loadDialog != null){
            loadDialog.dismiss();
            loadDialog = null;
        }
        return loadDialog;
    }
}
