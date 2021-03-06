package com.example.moodtracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.emoji.bundled.BundledEmojiCompatConfig;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.widget.EmojiEditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * This is an activity that handles editing for existing moods
 */
public class EditMoodEvent extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView location;
    private EmojiEditText et;
    private Spinner feelingSpinner, socialStateSpinner;
    private ImageView imageView;
    private int index;      // holds the index of the event in the db that is being edited
    private String feeling = "", socialState = "", image;

    private FirebaseAuth mAuth;
    private DocumentReference docRef;
    private Mood mood;
    private User user;

    private ArrayList<String> moods;
    private ArrayList<String> socialStates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mood_event);

        //set up emoji compatibility
        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
        EmojiCompat.init(config);

        et = findViewById(R.id.reasonET2);
        imageView = findViewById(R.id.moodImage);

        location = findViewById(R.id.locationTV2);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditMoodEvent.this, MapActivity.class);
                intent.putExtra("flag", "2");
                intent.putExtra("username", user.getUserID());
                Log.d(TAG, "EditMoodEvent: onCreate f2 username: " + user.getUserID());
                intent.putExtra("feeling", mood.getFeeling());
                intent.putExtra("reason", mood.getReason());
                intent.putExtra("lat", mood.getGeo_point().getLatitude());
                intent.putExtra("long", mood.getGeo_point().getLongitude());
                startActivity(intent);
            }
        });

        feelingSpinner = findViewById(R.id.editMoodFeelingSpinner);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.feelings, R.layout.spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_list_item_1);
        feelingSpinner.setAdapter(adapter1);
        feelingSpinner.setOnItemSelectedListener(this);

        socialStateSpinner = findViewById(R.id.socialStateSpinner2);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.socialStates, R.layout.spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_list_item_1);
        socialStateSpinner.setAdapter(adapter2);
        socialStateSpinner.setOnItemSelectedListener(this);

        index = getIntent().getExtras().getInt("index");
        initializeArrays();

        //load data from DB
        mAuth = FirebaseAuth.getInstance();
        loadDataFromDB();
    }

    /**
     * This is a helper function that takes the ImageView and image as a string and sets
     * the image of the ImageView to the image
     * @param completeImageData the image file represented as a string
     * @param imageView the view on which the image will be shown
     */
    public void decodeImage(String completeImageData, ImageView imageView) {
        if (completeImageData == null) { return; }

        // Incase you're storing into aws or other places where we have extension stored in the starting.
        String imageDataBytes = completeImageData.substring(completeImageData.indexOf(",")+1);
        InputStream stream = new ByteArrayInputStream(Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT));
        Bitmap bitmap = BitmapFactory.decodeStream(stream);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * This is a helper function to onResume. It uses the global user variable and fills UI
     * with the appropriate information
     */
    public void loadDataFromDB() {
        mAuth = FirebaseAuth.getInstance();
        docRef = FirebaseFirestore.getInstance().collection("users").document("user"+mAuth.getCurrentUser().getEmail());

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);
                mood = user.getMoodHistory().get(index);

                //initialise spinners and edittexts
                et.setText(mood.getReason());
                decodeImage(mood.getImg(), imageView);
                feelingSpinner.setSelection(moods.indexOf(mood.getFeeling())+1);
                socialStateSpinner.setSelection(socialStates.indexOf(mood.getSocialState())+1);
                if(mood.getGeo_point() == null)
                    location.setText("");
            }
        });
    }

    /**
     * Initialises the array lists
     */
    public void initializeArrays() {
        moods = new ArrayList<>();
        moods.add("happy");
        moods.add("excited");
        moods.add("hopeful");
        moods.add("satisfied");
        moods.add("sad");
        moods.add("angry");
        moods.add("frustrated");
        moods.add("confused");
        moods.add("annoyed");
        moods.add("hopeless");
        moods.add("lonely");

        socialStates = new ArrayList<>();
        socialStates.add("alone");
        socialStates.add("with one person");
        socialStates.add("with two or more people");
        socialStates.add("with a crowd");
    }

    /**
     * This function handles all the changes the user wishes to make and commits them to the
     * database
     * @param v is a View object
     */
    public void editMoodEvent(View v) {
        boolean change = false;
        String reason = et.getText().toString();

        if(!feeling.equals("")) {
            if(!feeling.equals(mood.getFeeling())) {
                change = true;
                mood.setFeeling(feeling);
            }
            if(!reason.equals(mood.getReason())) {
                change = true;
                mood.setReason(reason);
            }
            if(!socialState.equals(mood.getSocialState())) {
                change = true;
                mood.setSocialState(socialState);
            }
            if(image != null && !image.equals(mood.getImg())) {
                change = true;
                mood.setImg(image);
            }

            if(change) {
                user.getMoodHistory().set(index, mood);
                docRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                });
            }

        } else if (feeling.equals(""))
            Toast.makeText(getApplicationContext(), "Please select how you feel", Toast.LENGTH_LONG).show();
    }

    /**
     * This function starts an intent that allows the user to upload an image
     * @param v is a View object
     */
    public void editMoodImage(View v) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), 3);
    }

    /**
     * This function handles the case where the user wants to remove their mood from their history
     * @param v is a View object
     */
    public void deleteMood(View v) {
        user.getMoodHistory().remove(index);
        docRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                finish();
            }
        });
    }

    /**
     * This function closes the activity and goes back to the previous activity
     * @param v is a View object
     */
    public void cancel(View v) { finish(); }

    /**
     * This function is called when an intent is created that has to interact with another API
     * directly.
     * @param requestCode Indicates which action the function must take
     * @param resultCode Indicates if the action is safe to carry out
     * @param data Gives the data that the Activity returned
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 3 && resultCode == Activity.RESULT_OK && data != null) {
            Uri image = data.getData();
            try {
                Bitmap temp = MediaStore.Images.Media.getBitmap(getContentResolver(), image);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                temp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                int num = 50;
                while (byteArray.length > 10000 && num > 0) {   // compress image to not more than 10 kb
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.reset();

                    temp.compress(Bitmap.CompressFormat.JPEG, num, byteArrayOutputStream);
                    num = num / 2;
                    byteArray = byteArrayOutputStream.toByteArray();
                }
                this.image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                Toast.makeText(this, "Image Uploaded", Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This is a required method for implementing AdapterView.OnItemClickListener. This function
     * recieves the item that was selected by the user, and chooses the appropriate action to save
     * the result.
     * @param parent is the object that notifies the progrom which spinner widget was used
     * @param view returns the view object
     * @param position is the index of the spinner item that was selected
     * @param id returns a long number
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.editMoodFeelingSpinner)
            feeling = parent.getItemAtPosition(position).toString();
        else if(parent.getId() == R.id.socialStateSpinner2)
            socialState = parent.getItemAtPosition(position).toString();
    }

    /**
     * This is a required method for implementing AdapterView.OnItemClickListener. This function
     * empty and has no function
     * @param parent
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}