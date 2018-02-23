package com.example.jisla2.runningapp2.Post;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;

import com.example.jisla2.runningapp2.MainActivity;
import com.example.jisla2.runningapp2.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class CreatePostActivity extends AppCompatActivity {
    public static final String POST = "POST";

    public static Bitmap image = null;

    public static final int PICK_IMAGE = 1;

    private Post post;

    private NumberPicker mMileageOnesNumberPicker;
    private NumberPicker mMileageTenthsNumberPicker;
    private NumberPicker mPaceMinutesNumberPicker;
    private NumberPicker mPaceSecondsNumberPicker;
    private EditText mDescriptionEditText;
    private ImageButton mAddPhotoButton;
    private ImageView mImageView;
    private Button mPostButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        final String currentUser = MainActivity.currentUser;

        initializeFields(currentUser);
    }

    /**
     * Initializes all input fields for creating a post. This includes NumberPickers
     * for mileage and pace, an EditText for a description, an ImageView for a photo,
     * and a Button to create the post.
     *
     * @param currentUser
     */
    private void initializeFields(final String currentUser) {
        mMileageOnesNumberPicker = (NumberPicker) findViewById(R.id.mileageOnesNumberPicker);
        // TODO: MAKE THESE VALUES CONSTANT VARIABLES?
        mMileageOnesNumberPicker.setMinValue(0);
        mMileageOnesNumberPicker.setMaxValue(30);
        mMileageOnesNumberPicker.setWrapSelectorWheel(true);

        mMileageTenthsNumberPicker = (NumberPicker) findViewById(R.id.mileageTenthsNumberPicker);
        mMileageTenthsNumberPicker.setMinValue(0);
        mMileageTenthsNumberPicker.setMaxValue(9);
        mMileageTenthsNumberPicker.setWrapSelectorWheel(true);

        mPaceMinutesNumberPicker = (NumberPicker) findViewById(R.id.paceMinutesNumberPicker);
        mPaceMinutesNumberPicker.setMinValue(0);
        mPaceMinutesNumberPicker.setMaxValue(20);
        mPaceMinutesNumberPicker.setWrapSelectorWheel(true);

        mPaceSecondsNumberPicker = (NumberPicker) findViewById(R.id.paceSecondsNumberPicker);
        ArrayList<String> seconds = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
            for (int j = 0; j <= 9; j++) {
                seconds.add(String.format("%s%s", i, j));
            }
        }
        // Convert seconds ArrayList to array
        String[] secondsArray = new String[seconds.size()];
        secondsArray = seconds.toArray(secondsArray);
        mPaceSecondsNumberPicker.setDisplayedValues(secondsArray);
        mPaceSecondsNumberPicker.setMinValue(00);
        mPaceSecondsNumberPicker.setMaxValue(59);
        mPaceSecondsNumberPicker.setWrapSelectorWheel(true);

        mDescriptionEditText = (EditText) findViewById(R.id.descriptionEditText);

        mImageView = (ImageView) findViewById(R.id.imageView);

        mAddPhotoButton = (ImageButton) findViewById(R.id.addPhotoButton);
        mAddPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        mPostButton = (Button) findViewById(R.id.postButton);
        // Add post information to database and put on main feed
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPost(currentUser);
            }
        });
    }

    /**
     * Gets the data from each input field and creates a new Post object from this data.
     * Then adds this post data to the Firebase database.
     *
     * @param currentUser
     */
    private void createPost(String currentUser) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference feedRef = database.getReference("feed").getRef();
        DatabaseReference newPostRef = feedRef.push();

        int mileageOnes = mMileageOnesNumberPicker.getValue();
        int mileageTenths = mMileageTenthsNumberPicker.getValue();
        String mileage = String.format("%s.%s", mileageOnes, mileageTenths);

        int paceMinutes = mPaceMinutesNumberPicker.getValue();
        int paceSeconds = mPaceSecondsNumberPicker.getValue();
        String pace;
        if (paceSeconds / 10.0 < 1) {
            pace = String.format("%s:0%s", paceMinutes, paceSeconds);
        }
        else {
            pace = String.format("%s:%s", paceMinutes, paceSeconds);
        }

        // Encodes a bitmap image into a String
        String encodedImage = "";
        if (image != null) {
            Bitmap image = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteFormat = stream.toByteArray();
            encodedImage = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
        }

        String description = mDescriptionEditText.getText().toString();

        String databaseKey = newPostRef.getKey();

        addPostToDatabase(currentUser, newPostRef, mileage, pace, encodedImage, description);
        addPostToFeed(currentUser, mileage, pace, encodedImage, description, databaseKey);
    }

    /**
     * Creates a new post object given a series of data
     * and sends it to MainActivity to be added to the main feed
     *
     * @param currentUser
     * @param mileage
     * @param pace
     * @param encodedImage
     * @param description
     * @param databaseKey
     */
    private void addPostToFeed(String currentUser, String mileage, String pace,
                               String encodedImage, String description, String databaseKey) {
        post = new Post(currentUser, mileage, pace, encodedImage, description, databaseKey);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(POST, post);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * Pushes the data for a post into a new post child in the Firebase database.
     * This new post has a uniquely generated key.
     * 
     * @param currentUser
     * @param newPostRef
     * @param mileage
     * @param pace
     * @param encodedImage
     * @param description
     */
    private void addPostToDatabase(String currentUser, DatabaseReference newPostRef, String mileage,
                                   String pace, String encodedImage, String description) {
        Map<String, String> postInfo = new HashMap<String, String>();
        postInfo.put("author", currentUser);
        postInfo.put("mileage", mileage);
        postInfo.put("pace", pace);
        postInfo.put("image", encodedImage);
        postInfo.put("description", description);
        postInfo.put("likes", "");

        newPostRef.setValue(postInfo);
    }

    /**
     * Adds selected photo from Gallery to ImageView
     *
     * @param requestCode code sent from this activity
     * @param resultCode code received from other activity
     * @param data data sent from other activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                mImageView.setImageBitmap(bitmap);
                image = bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
