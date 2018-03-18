package sam.van.roy.ocr_app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sam.van.roy.ocr_app.Helper;
import sam.van.roy.ocr_app.R;


public class CropImageActivity extends AppCompatActivity {
    private Uri mCropImageUri;
    private CropImageView mCropImageView;
    private ImageButton ocrButton;
    private Bitmap image;

    private TessBaseAPI mTess; //Tess API reference
    String datapath = ""; //path to folder containing language data file

    private String ocrLanguage = "eng";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        datapath = getFilesDir()+ "/tesseract/";

        //make sure training data has been copied
        checkFile(new File(datapath + "tessdata/"), ocrLanguage);

        initTesseractApi(ocrLanguage);

        mCropImageView = (CropImageView) findViewById(R.id.CropImageView);

        Intent cropImageIntent = getIntent();
        mCropImageUri = Uri.parse(cropImageIntent.getStringExtra(MainActivity.CROPIMAGEURI));

        if(mCropImageUri != null){
            mCropImageView.setImageUriAsync(mCropImageUri);
            image = mCropImageView.getCroppedImage();
        }

        ocrButton = findViewById(R.id.ocrBtn);
        ocrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                AlertDialog alertDialog = Helper.showLoadingDialog(CropImageActivity.this);
                String OCRresultText = processImage();
//                Helper.dismissLoadingDialog(alertDialog);

                Intent ocrIntent = new Intent(getApplication(), OcrResultActivity.class);
                ocrIntent.putExtra("ocrResultText", OCRresultText);
                startActivity(ocrIntent);
//                try {
//                    String filename = "bitmap.png";
//                    FileOutputStream stream = null;
//                    stream = openFileOutput(filename, Context.MODE_PRIVATE);
//                    image.compress(Bitmap.CompressFormat.PNG, 100, stream);
//
//                    //Cleanup
//                    stream.close();
////                ocrIntent.putExtra("image", filename);
//
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_item_crop:
                onCropImageClick();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpToolbar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.crop_menu, menu);
        getMenuInflater().inflate(R.menu.photo_menu, menu);

        // Return true to display menu
        return true;
    }

    public void setUpToolbar(){
        Toolbar myToolbar = (Toolbar) findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
    }

    /**
     * Crop the image and set it back to the cropping view.
     */
    public void onCropImageClick() {
        Bitmap cropped = mCropImageView.getCroppedImage(500, 500);
//        Bitmap cropped = mCropImageView.getCroppedImage();
        if (cropped != null)
            mCropImageView.setImageBitmap(cropped);
            image = cropped;
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
    public String processImage(){
        mTess.setImage(image);
        String OCRresultText = mTess.getUTF8Text();
//        image.recycle();
        return OCRresultText;
//        return OCRresultText != null;
    }

}
