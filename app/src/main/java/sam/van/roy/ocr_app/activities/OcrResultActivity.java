package sam.van.roy.ocr_app.activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sam.van.roy.ocr_app.R;

public class OcrResultActivity extends AppCompatActivity {
    private TessBaseAPI mTess; //Tess API reference
    String datapath = ""; //path to folder containing language data file
    Bitmap image; //our image

    private String ocrLanguage = "eng";
    private String OCRresultText = null;
//    private TextView ocrResultTextView;

    private ShareActionProvider mShareActionProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);

        datapath = getFilesDir()+ "/tesseract/";

        //make sure training data has been copied
        checkFile(new File(datapath + "tessdata/"), ocrLanguage);

        initTesseractApi(ocrLanguage);

        //        ocrResultTextView = findViewById(R.id.ocrResultTextView);
//        ocrResultTextView.setMovementMethod(new ScrollingMovementMethod());

//        ocrBtn = findViewById(R.id.ocr_btn);
//        ocrBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                processImage(view);
//                ocrResultTextView.setVisibility(View.VISIBLE);
//                mCropImageView.requestLayout();
//                mCropImageView.getLayoutParams().height = 2000;
////                LinearLayout.LayoutParams loparams = (LinearLayout.LayoutParams) ocrResultTextView.getLayoutParams();
////                loparams.weight = 2;
////                ocrResultTextView.setLayoutParams(loparams);
////
////                LinearLayout.LayoutParams loparams2 = (LinearLayout.LayoutParams) mCropImageView.getLayoutParams();
////                loparams2.weight = 1;
////                mCropImageView.setLayoutParams(loparams2);
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, OCRresultText);
        shareIntent.setType("text/plain");

        setShareIntent(shareIntent);
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpToolbar();
    }

    public void setUpToolbar(){
        Toolbar myToolbar = (Toolbar) findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
    }

    private void initTesseractApi(String language) {
        //initialize Tesseract API
//        String lang = "eng";
        mTess = new TessBaseAPI();
        mTess.init(datapath, language);
    }

    //Tesseract
    private void copyFilesTrainedData(String language) {
        try {
            //location we want the file to be at
            String filepath = datapath + "/tessdata/" + language + ".traineddata";

            //get access to AssetManager
            AssetManager assetManager = getAssets();

            //open byte streams for reading/writing
            InputStream instream = assetManager.open("tessdata/nld.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Tesseract
    private void checkFile(File dir, String language) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists()&& dir.mkdirs()){
            copyFilesTrainedData(language);
        }
        //The directory exists, but there is no data file in it
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/" + language + ".traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFilesTrainedData(language);
            }
        }
    }

    //Tesseract
    public void processImage(View view){
        mTess.setImage(image);
        OCRresultText = mTess.getUTF8Text();
//        TextView OCRTextView = (TextView) findViewById(R.id.ocrResultTextView);
//        OCRTextView.setText(OCRresultText);
    }
}
