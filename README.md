Guide

Hello, I’m Sam and in this guide, I’m going to quickly go over the basic stuff for setting up an OCR (optical character reading) app with android. For the OCR part we’ll use a fork of the tesseract OCR technology called rmtheis (https://github.com/rmtheis/tess-two).
This project is made with Android studio version 3.0.1.

1.	Create a New Android Project

Open Android Studio and create a new Project and add an empty activity.

2.	Add tess-two as an external dependency

First you need to add the dependency for the Tesseract fork. In your app module file (build.gradle) you need to add “compile 'com.rmtheis:tess-two:8.0.0'” to your dependencies. Once you’ve added that we can start to use the library. 

3.	Adding traineddata file

The OCR technology uses a file for trying to recognize the text in your images. This is called a traineddata file. You can get such a traineddata file from the Github repository of tesseract.

Our fork of the tesseract repository, rmtheis, uses tesseract version 3.* . 

You can get the traineddata files for this version from this link : https://github.com/tesseract-ocr/tessdata/tree/3.04.00. This is only for Tesseract version 3.* . If you want to experiment with the newer tesseract version ( 4.* ), you can get the traineddata files from this link : https://github.com/tesseract-ocr/tessdata.

In my repository I used the traineddata files for English and Dutch. If you want to try out other languages, you can find the languages and their abbreviation from this link : https://github.com/tesseract-ocr/tesseract/wiki/Data-Files

Once you’ve downloaded the traineddata file you want, you need to make an Android Resource directory named assets in your android project. In the newly created assets folder, you need to create a regular directory named “tessdata” where you can place your traineddata files. The folder structure needs to be right because Tesseract wil look in this specific folder structure for the traineddata files.

4.	Get a test image

For the tutorial we’ll use a test image from the internet to test the application.
Grab an image with text from the internet, download and save this image as "test_image.png" and stick it in the res/drawable/ folder.

*Note: If you'd like to allow the user to choose or take their own photo, take a look at my project.

5.	Design the Activity

Open activity_main.xml for editing. Using the design/text view, we’ll create a simple design. The important components to have in it are:
•	An ImageView, to hold our sample image.
•	A TextView, to hold the OCR-ed text
•	A Button, or clickable area to trigger the OCR process.
Below is an example layout for this demo.
 
Here is the corresponding xml for the design. 

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:tools="http://schemas.android.com/tools" 
android:orientation="vertical"
android:layout_width="match_parent"
android:layout_height="match_parent"
tools:context=".MainActivity"
android:weightSum="1">

<RelativeLayout
    android:layout_width="match_parent"
    android:layout_height="332dp"
    android:background="#ffffff"
    android:id="@+id/ImageContainer">

    <ImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/imageView"
        android:src="@drawable/test_image"/>

</RelativeLayout>

<RelativeLayout
    android:layout_width="match_parent"
    android:layout_height="42dp"
    android:layout_alignParentTop="true"
    android:layout_alignParentStart="true"
    android:clickable="true"
    android:onClick="processImage"
    android:background="#167865"
    android:id="@+id/OCRButtonContainer">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Run OCR"
        android:id="@+id/OCRbutton"
        android:textSize="18dp"
        android:layout_centerVertical="true"
        android:layout_centerHorizontal="true"
        android:clickable="true"
        android:onClick="processImage"/>
</RelativeLayout>

<RelativeLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#e4e3e3"
    android:id="@+id/OCRTextContainer">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="OCR Text will appear here"
        android:id="@+id/OCRTextView"
        android:textSize="18dp"
        android:layout_centerVertical="true"
        android:layout_centerHorizontal="true"
        android:background="#dedede"
        android:textColor="#a3a3a3" />
</RelativeLayout>
</LinearLayout>

 
6.	Initialize Activity Variables

We're ready to start coding the activity code, so let's open MainActivity.java for editing. There should already be some example code in there, such as an onCreate() method. 
onCreate() is generally used to initialize things needed for the activity, so we'll deal with that first. We need to initialize the Tess-Two API for use, as well as grab the test image from our assets like so:

Bitmap image; //our image
private TessBaseAPI mTess; //Tess API reference
String datapath = ""; //path to folder containing language data file
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main); //sets the view for this activity

    //init image
    image = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);

    datapath = getFilesDir()+ "/tesseract/";

    //initialize Tesseract API
    String lang = "eng";
    mTess = new TessBaseAPI();   
    mTess.init(datapath, lang);
}

Here we've declared three new class variables, image, mTess, and datapath.
  •	image is initialized by using BitmapFactory.decodeResources() to get and decode the resource R.drawable.test_ image into a bitmap.
Tesseract's API is accessed with a TessBaseAPI object. On init(), it uses the supplied datapath and checks for a child directory named 'tessdata', and then checks to see if 'tessdata' contains a language data file.
  
  •	mTess is initialized with a call to init(datapath, lang), where datapath is the path to the parent folder of the folder containing the language file, and lang is the file's language (in this case, the parent folder is "tesseract" and the language is "eng" for english).
 
 •	datapath is initialized by obtaining the absolute path to the directory on the device'sfilesystem via getFilesDir(), and adding '/tesseract/' to the end of the result.

7.	Copy Training Data to Device

We need to do a little extra to get a proper value for datapath because of the way Android handles assets. At runtime, assets may only be accessed with raw byte streams via an AssetManager, meaning files in our asset folder are not accessible by filepath. To get around this, we need to copy the language data file into the device's internal or external storage at runtime, and then use that path to initialize Tesseract.

First, let's define a method that allows us to copy the file to the device:

private void copyFiles() {
    try {
        //location we want the file to be at
        String filepath = datapath + "/tessdata/eng.traineddata";

        //get access to AssetManager
        AssetManager assetManager = getAssets();

        //open byte streams for reading/writing
        InputStream instream = assetManager.open("tessdata/eng.traineddata");
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

Let's also create a simple method that checks whether the file is already on the device in the expected location, and calls copyFile() if it isn't. 
This checkFile() method covers the two scenarios in which we should copy the file over:

private void checkFile(File dir) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        //The directory exists, but there is no data file in it
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    

This method accepts a File variable dir and checks to see if it exists. If it doesn't, we try to create it with mkdirs(), and if that's successful, we call copyFiles(). If it does exist, we check to see if the language file is where we expect it to be, and if not, we call copyFiles().

Now let's rearrange our initialization method, onCreate(), to call checkFile() before initializing Tesseract. Our new onCreate() method now looks like this:

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);                                      
    setContentView(R.layout.activity_main);

    //init image
    image = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);

    datapath = getFilesDir()+ "/tesseract/";

    //make sure training data has been copied
    checkFile(new File(datapath + "tessdata/"));

    //init Tesseract API
    String language = "eng";

    mTess = new TessBaseAPI();
    mTess.init(datapath, language);
}

* Note: On initialization, Tesseract checks the given datapath for a directory called tessdata, and then checks to see if there is a data file inside it. Make sure to supply a path to tessdata's parentfolder, i.e. Whatever/path/tesseract/ instead of Whatever/path/tesseract/tessdata/, or the check will fail.

7. Process an Image

We are finally ready to actually use the API to do OCR! Our clickable areas (OCRButtonContainer and OCRbutton) both are set to fire the method processImage() when clicked, so let's define that:

public void processImage(View view){
    String OCRresult = null;
    mTess.setImage(image);
    OCRresult = mTess.getUTF8Text();
    TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);
    OCRTextView.setText(OCRresult);
}

This method sets the image we want the API to work with via setImage(), runs the OCR on the image with getUTF8Text(), and then puts the results in a string called OCRresult. It then finds the TextView component OCRTextView, and sets its text to our result via setText().
The MainActivity.java file should look like something like this in the end:

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    Bitmap image;
    private TessBaseAPI mTess;
    String datapath = "";

    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init image
        image = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);

        //initialize Tesseract API
        String language = "eng";
        datapath = getFilesDir()+ "/tesseract/";
        mTess = new TessBaseAPI();

        checkFile(new File(datapath + "tessdata/"));

        mTess.init(datapath, language);
    }

    public void processImage(View view){
        String OCRresult = null;
        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();
        TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);
        OCRTextView.setText(OCRresult);
    }

    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

 
8.	Run your app!

Now if you run your app in an emulator or device, you should be able to see your results in the text area after clicking the 'Run OCR' button. 
Congratulations, you've made your own simple OCR application!
You’ll notice that the results aren’t always perfect. If you want to improve the results, you can train your own files. Here is a good place to start for that : https://github.com/tesseract-ocr/tesseract/wiki/Training-Tesseract.

