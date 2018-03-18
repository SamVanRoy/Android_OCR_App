package sam.van.roy.ocr_app.activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sam.van.roy.ocr_app.R;

public class OcrResultActivity extends AppCompatActivity {

//    private TextView ocrResultTextView;
//    String bitmapFilename;
    private String OCRresultText = null;
    Intent shareIntent = new Intent();

    private ShareActionProvider mShareActionProvider;
    EditText OCREditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);
        OCRresultText = getIntent().getStringExtra("ocrResultText");

        OCREditText = (EditText) findViewById(R.id.ocrResultTextView);
        OCREditText.setText(OCRresultText);

        OCREditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                OCRresultText = OCREditText.getText().toString();
                setShareIntent(createShareIntent());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
//        bitmapFilename = getIntent().getStringExtra("image");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        setShareIntent(createShareIntent());
        return true;
    }

    private Intent createShareIntent() {
        Bundle mBundle = new Bundle();
        mBundle.putString(Intent.EXTRA_TEXT, OCRresultText);

        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.replaceExtras(mBundle);
//        shareIntent.putExtra(Intent.EXTRA_TEXT, OCRresultText);
        shareIntent.setType("text/plain");
        return shareIntent;
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

//        try {
//            FileInputStream is = this.openFileInput(bitmapFilename);
//            image = BitmapFactory.decodeStream(is);
//            is.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        processImage();
    }

    public void setUpToolbar(){
        Toolbar myToolbar = (Toolbar) findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
    }


}
