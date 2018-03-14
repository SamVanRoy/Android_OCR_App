package sam.van.roy.ocr_app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.theartofdev.edmodo.cropper.CropImageView;

import sam.van.roy.ocr_app.R;


public class CropImageActivity extends AppCompatActivity {
    private Uri mCropImageUri;
    private CropImageView mCropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        mCropImageView = (CropImageView) findViewById(R.id.CropImageView);

        Intent cropImageIntent = getIntent();
        mCropImageUri = Uri.parse(cropImageIntent.getStringExtra(MainActivity.CROPIMAGEURI));
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

    public void setUpToolbar(){
        Toolbar myToolbar = (Toolbar) findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
    }

    /**
     * Crop the image and set it back to the cropping view.
     */
    public void onCropImageClick() {
        Bitmap cropped = mCropImageView.getCroppedImage(500, 500);
        if (cropped != null)
            mCropImageView.setImageBitmap(cropped);
//        image = cropped;
    }

}
