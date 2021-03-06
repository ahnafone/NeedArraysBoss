package com.example.moodtracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.emoji.widget.EmojiTextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ViewFriendMood extends AppCompatActivity {

    private String email;
    private int index;
    private Mood mood;

    private TextView header, socialState, location;
    private EmojiTextView feeling, reason;
    private ImageView image;

    private HashMap<String, String> moodEmojis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friend_mood);

        //initialise the widgets
        header = findViewById(R.id.editMoodTitle);
        feeling = findViewById(R.id.feelingTV2);
        reason = findViewById(R.id.reasonTV2);
        socialState = findViewById(R.id.socialStateSpinner2);
        image = findViewById(R.id.moodImage);
        location = findViewById(R.id.location);

        initArrays();

        email = getIntent().getExtras().getString("email");
        index = getIntent().getExtras().getInt("index");

        FirebaseFirestore.getInstance().collection("users").document("user"+email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                mood = user.getMoodHistory().get(index);
                mood.setFriend(user.getUserID());

                //load data into layout
                header.setText(mood.getFriend() + "'s mood...");
                feeling.setText(mood.getFeeling() + " " + moodEmojis.get(mood.getFeeling()));
                reason.setText(mood.getReason());
                socialState.setText(mood.getSocialState());
                decodeImage(mood.getImg(), image);
                if(mood.getGeo_point() == null)
                    location.setText("");
                else {
                    location.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), MapActivity.class);

                            intent.putExtra("flag", "2");
                            intent.putExtra("username", mood.getFriend());
                            intent.putExtra("feeling", mood.getFeeling());
                            intent.putExtra("reason", mood.getReason());
                            intent.putExtra("lat", mood.getGeo_point().getLatitude());
                            Log.d(TAG, "ViewMood: onCreate f2 geopoint Latitude: " + mood.getGeo_point().getLatitude());
                            intent.putExtra("long", mood.getGeo_point().getLongitude());
                            startActivity(intent);
                        }
                    });
                }
            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
     * Initializes the arrays for feelings and social states
     */
    public void initArrays() {

        //initializes emoji array
        moodEmojis = new HashMap<>();
        moodEmojis.put("happy", new String(Character.toChars(0x1F601)));
        moodEmojis.put("excited", new String(Character.toChars(0x1F606)));
        moodEmojis.put("hopeful", new String(Character.toChars(0x1F60A)));
        moodEmojis.put("satisfied", new String(Character.toChars(0x1F60C)));
        moodEmojis.put("sad", new String(Character.toChars(0x1F61E)));
        moodEmojis.put("angry", new String(Character.toChars(0x1F621)));
        moodEmojis.put("frustrated", new String(Character.toChars(0x1F623)));
        moodEmojis.put("confused", new String(Character.toChars(0x1F635)));
        moodEmojis.put("annoyed", new String(Character.toChars(0x1F620)));
        moodEmojis.put("hopeless", new String(Character.toChars(0x1F625)));
        moodEmojis.put("lonely", new String(Character.toChars(0x1F614)));
    }
}
