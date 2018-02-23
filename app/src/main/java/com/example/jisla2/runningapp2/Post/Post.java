package com.example.jisla2.runningapp2.Post;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

// TODO: ADD PHOTO

public class Post implements Parcelable {
    private String author;
    private String mileage;
    private String pace;
    private String encodedImage;
    private String description;
    private ArrayList<String> likes = new ArrayList<>();
    private int numLikes = 0;
    private int numComments = 0;
    private String databaseKey;

    public String getAuthor() {
        return author;
    }

    public String getMileage() {
        return mileage;
    }

    public String getPace() {
        return pace;
    }

    public String getEncodedImage() {
        return encodedImage;
    }

    public String getDescription() {
        return description;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public int getNumComments() {
        return numComments;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void addLike(String user) {
        numLikes++;
        likes.add(user);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference feedRef = database.getReference("feed");
        DatabaseReference postRef = feedRef.child(databaseKey);
        DatabaseReference likesRef = postRef.child("likes");
        likesRef.push().setValue(user);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.author);
        dest.writeString(this.mileage);
        dest.writeString(this.pace);
        dest.writeString(this.encodedImage);
        dest.writeString(this.description);
        dest.writeString(this.databaseKey);
    }

    public Post(String author, String mileage, String pace, String encodedImage, String description, String databaseKey) {
        this.author = author;
        this.mileage = mileage;
        this.pace = pace;
        this.encodedImage = encodedImage;
        this.description = description;
        this.databaseKey = databaseKey;
    }

    protected Post(Parcel in) {
        this.author = in.readString();
        this.mileage = in.readString();
        this.pace = in.readString();
        this.encodedImage = in.readString();
        this.description = in.readString();
        this.databaseKey = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel source) {
            return new Post(source);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };
}
