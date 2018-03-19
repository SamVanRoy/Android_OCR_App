package sam.van.roy.ocr_app.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.system.ErrnoException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import sam.van.roy.ocr_app.Helper;
import sam.van.roy.ocr_app.PhotoOptions;
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

//        //make sure training data has been copied
//        checkFile(new File(datapath + "tessdata/"), ocrLanguage);
//
//        initTesseractApi(ocrLanguage);

        mCropImageView = (CropImageView) findViewById(R.id.CropImageView);

        Intent cropImageIntent = getIntent();
        mCropImageUri = Uri.parse(cropImageIntent.getStringExtra(MainActivity.CROPIMAGEURI));

        if(mCropImageUri != null){
            mCropImageView.setImageUriAsync(mCropImageUri);
        }

        Spinner spinner = findViewById(R.id.language_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.languages_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (adapterView.getItemAtPosition(i).toString()) {
                    case "Nederlands":
                        ocrLanguage = "nld";
                        checkFile(new File(datapath + "tessdata/"), ocrLanguage);
                        initTesseractApi(ocrLanguage);
                        break;
                    case "English":
                        ocrLanguage = "eng";
                        checkFile(new File(datapath + "tessdata/"), ocrLanguage);
                        initTesseractApi(ocrLanguage);
                        break;
                    default:
                        ocrLanguage = "nld";
                        checkFile(new File(datapath + "tessdata/"), ocrLanguage);
                        initTesseractApi(ocrLanguage);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ocrButton = findViewById(R.id.ocrBtn);
        ocrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image = mCropImageView.getCroppedImage();
//                AlertDialog alertDialog = Helper.showLoadingDialog(CropImageActivity.this);
                String OCRresultText = processImage();
//                Helper.dismissLoadingDialog(alertDialog);

                Intent ocrIntent = new Intent(getApplication(), OcrResultActivity.class);
                ocrIntent.putExtra("ocrResultText", OCRresultText);
                startActivity(ocrIntent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_item_crop:
                onCropImageClick();
                return true;
            case R.id.menu_item_camera:
                startActivityForResult(getPickImageChooserIntent(PhotoOptions.CAMERA), 200);
                return true;
            case R.id.menu_item_photogallery:
                startActivityForResult(getPickImageChooserIntent(PhotoOptions.GALLERY), 200);
                return true;
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

    /**
     * On load image button click, start pick image chooser activity.
     */
//    public void onLoadImageClick(View view) {
//        //startActivityForResult(getPickImageChooserIntent(), 200);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri imageUri = getPickImageResultUri(data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage,
            // but we don't know if we need to for the URI so the simplest is to try open the stream and see if we get error.
            boolean requirePermissions = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    isUriRequiresPermissions(imageUri)) {

                // request permissions and handle the result in onRequestPermissionsResult()
                requirePermissions = true;
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }

            if (!requirePermissions) {
                mCropImageUri = imageUri;
                goToCropImageActivity();
//                mCropImageView.setImageUriAsync(imageUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            goToCropImageActivity();
//            mCropImageView.setImageUriAsync(mCropImageUri);
        } else {
            Toast.makeText(this, "Required permissions are not granted", Toast.LENGTH_LONG).show();
        }
    }

    private void goToCropImageActivity(){
        Intent cropImageIntent = new Intent(this, CropImageActivity.class);
        cropImageIntent.putExtra(MainActivity.CROPIMAGEURI, mCropImageUri.toString());
        startActivity(cropImageIntent);
    }

    /**
     * Create a chooser intent to select the source to get image from.<br/>
     * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the intent chooser.
     */
    public Intent getPickImageChooserIntent(PhotoOptions photoOption) {

        // Determine Uri of camera image to save.
        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        switch (photoOption){
            case CAMERA:
                getAllCameraIntents(allIntents, packageManager, outputFileUri);
                break;
            case GALLERY:
                getAllGalleryIntents(allIntents, packageManager, outputFileUri);
                break;
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    public void getAllCameraIntents(List<Intent> allIntents, PackageManager packageManager, Uri outputFileUri){
        // collect all camera intents
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }
    }

    private void getAllGalleryIntents(List<Intent> allIntents, PackageManager packageManager, Uri outputFileUri){
        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }
    }

    /**
     * Get URI to image received from capture by camera.
     */
    public Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpeg"));
        }
        return outputFileUri;
    }

    /**
     * Get the URI of the selected image from {@link #getPickImageChooserIntent(PhotoOptions)}.<br/>
     * Will return the correct URI for camera and gallery image.
     *
     * @param data the returned data of the activity result
     */
    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }

    /**
     * Test if we can open the given Android URI to test if permission required error is thrown.<br>
     */
    public boolean isUriRequiresPermissions(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream stream = resolver.openInputStream(uri);
            stream.close();
            return false;
        } catch (FileNotFoundException e) {
            if (e.getCause() instanceof ErrnoException) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }


}
