package com.example.jisla2.runningapp2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jisla2.runningapp2.Post.CreatePostActivity;
import com.example.jisla2.runningapp2.Post.Post;
import com.example.jisla2.runningapp2.Post.PostAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

// TODO: WHEN CREATED, ADD ALL POSTS ALREADY IN DATABASE

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "EmailPassword";

    public static String currentUser;
    public static final int CREATE_POST_ACTIVITY_REQUEST_CODE = 1;

    private TextView mCurrentUserTextView;
    private Button mSignOutButton;
    private FloatingActionButton mPostButton;
    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private RecyclerView mFeedRecyclerView;
    private ArrayList<Post> mPosts = new ArrayList<>();
    private PostAdapter mPostAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView to hold posts in feed
        mFeedRecyclerView = (RecyclerView) findViewById(R.id.feedRecyclerView);
        mFeedRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mPostAdapter = new PostAdapter(mPosts);
        mFeedRecyclerView.setAdapter(mPostAdapter);

//        // Add post to feed
//        try {
//            Intent intent = getIntent();
//            Post post = intent.getParcelableExtra(PostAdapter.POST);
//            mPosts.clear();
//            mPosts.add(post);
//            mPostAdapter.notifyDataSetChanged();
//        }
//        catch (Exception e) {
//
//        }

        mCurrentUserTextView = (TextView) findViewById(R.id.currentUserTextView);

        mSignOutButton = (Button) findViewById(R.id.signOutButton);
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT);
            }
        });

        mPostButton = (FloatingActionButton) findViewById(R.id.postButton);
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createPostIntent = new Intent(MainActivity.this, CreatePostActivity.class);
                startActivityForResult(createPostIntent, CREATE_POST_ACTIVITY_REQUEST_CODE);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            /**
             * If a user is signed in, gets the current user's data
             * (including name, email, photoUrl, and uid)
             * and stores it in the appropriate variables
             *
             * If a user is not signed in, opens up a pop-up window to sign in
             *
             * Source: https://firebase.google.com/docs/auth/android/start/
             */
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in: display current user at top of screen
                    Log.d(TAG, String.format("onAuthStateChanged:signed_in:%s",
                            user.getUid()));
                    currentUser = user.getEmail();
                    String email = user.getEmail();
                    mCurrentUserTextView.setText(email);

                }
                else {
                    // User is signed out: open up authentication pop-up window
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Toast.makeText(MainActivity.this, "User is signed out", Toast.LENGTH_SHORT);
                    mCurrentUserTextView.setText("You are not currently signed in");
                    displayAuthenticationPopup();
                }
            }
        };
    }

    /**
     * Come back from CreatePostActivity and add new post to feed.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (CREATE_POST_ACTIVITY_REQUEST_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    // TODO: Extract the data returned from the child Activity.
                    Post post = data.getParcelableExtra(PostAdapter.POST);
                    mPosts.add(0, post);
                    mPostAdapter.notifyDataSetChanged();                }
                break;
            }
        }
    }

    /**
     * Displays a pop-up that allows the user to sign in or create an account
     * with an email address and password. The pop-up disappears once the user
     * enters a valid email address and password.
     *
     * Outline of function provided by Professor Zilles     *
     * Source: https://github.com/zillesc/SimpleFirebaseExample/blob/master/app/src/main/
     * java/ninja/zilles/lecture2/MainActivity.java
     */
    private void displayAuthenticationPopup() {
        // Build a authentication_dialog from layout XML
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.authentication_dialog);
        dialog.setTitle("Sign in / Create an account");

        mEmailField = (EditText) dialog.findViewById(R.id.emailEditText);
        mPasswordField = (EditText) dialog.findViewById(R.id.passwordEditText);

        final Button createAccountButton = (Button) dialog.findViewById(R.id.createAccountButton);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();
                createAccount(email, password);

                // Close pop-up window if an account is successfully created
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    dialog.dismiss();
                }
            }
        });
        final Button signInButton = (Button) dialog.findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();
                signIn(email, password);

                // Close pop-up window if a user is successfully signed in
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    /**
     * Source: https://firebase.google.com/docs/auth/android/start/
     */
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    /**
     * Source: https://firebase.google.com/docs/auth/android/start/
     */
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Creates a new user account given an email and password.
     * Displays a success message if a new account is created.
     * Displays an error message if there is a failure in creating a new account.
     *
     * Source: https://firebase.google.com/docs/auth/android/start/
     *
     * @param email
     * @param password
     */
    private void createAccount(final String email, final String password) {
        Log.d(TAG, String.format("createAccount:%s", email));
        if (!validateForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, String.format("createUserWithEmail:onComplete:%s",
                                task.isSuccessful()));

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Failed to create a new account",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(MainActivity.this,
                                    String.format("Created new account: %s", email),
                                    Toast.LENGTH_SHORT).show();
                            signIn(email, password);
                        }
                    }
                });
    }


    /**
     * Signs a user into their account given an email and password.
     * Displays a success message if the user successfully signs in.
     * Displays an error message if there is a failure signing in.
     *
     * Source: https://firebase.google.com/docs/auth/android/start/
     *
     * @param email
     * @param password
     */
    private void signIn(final String email, String password) {
        Log.d(TAG, String.format("signIn:%s", email));
        if (!validateForm()) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, String.format("signInWithEmail:onComplete:%s",
                                task.isSuccessful()));

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(MainActivity.this,
                                    "Sign in failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(MainActivity.this,
                                    String.format("Signed in as: %s", email),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    /**
     * Source: https://firebase.google.com/docs/auth/android/start/
     */
    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }


}
