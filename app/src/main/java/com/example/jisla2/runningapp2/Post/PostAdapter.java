package com.example.jisla2.runningapp2.Post;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jisla2.runningapp2.MainActivity;
import com.example.jisla2.runningapp2.R;

import java.util.ArrayList;

/**
 * This fills the RecyclerView with Posts
 */

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    public static final String POST = "POST";

    ArrayList<Post> posts;

    public PostAdapter(ArrayList<Post> posts) {
        this.posts = posts;
    }

    /**
     * This function is called only enough times to cover the screen with views.  After
     * that point, it recycles the views when scrolling is done.
     * @param parent the intended parent object (our RecyclerView)
     * @param viewType unused in our function
     *                 (enables having different kinds of views in the same RecyclerView)
     * @return the new ViewHolder we allocate
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // a LayoutInflater turns a layout XML resource into a View object.
        final View post= LayoutInflater.from(parent.getContext()).
                inflate(R.layout.post, parent, false);
        return new ViewHolder(post);
    }

    /**
     * This function gets called each time a ViewHolder needs to hold data for a different
     * position in the list.  We don't need to create any views (because we're recycling), but
     * we do need to update the contents in the views.
     * @param viewHolder the ViewHolder that knows about the Views we need to update
     * @param position the index into the array of Posts
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Post post = posts.get(position);

        final ViewHolder holder = viewHolder;

        String details = String.format("%s ran %s miles at an average pace of %s",
                post.getAuthor(),
                post.getMileage(),
                post.getPace());
        holder.detailsTextView.setText(details);

        holder.descriptionTextView.setText(post.getDescription());
        holder.likesTextView.setText(String.format("%s like(s)", post.getNumLikes()));

        // If a user selects a photo from the gallery, add it to the post
        // Otherwise, leave a blank space where the image should be
        if (!("").equals(post.getEncodedImage())) {
            byte[] decodedString = Base64.decode(post.getEncodedImage(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imageView.setImageBitmap(decodedByte);
            CreatePostActivity.image = null;
        }
        else {
            holder.imageView.setImageResource(android.R.color.white);
        }

        // When like button clicked, add a like to this post
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> likes = post.getLikes();
                if (!likes.contains(MainActivity.currentUser)) {
                    post.addLike(MainActivity.currentUser);
                    holder.likesTextView.setText(String.format("%s like(s)", post.getNumLikes()));
                }
            }
        });
    }

    /**
     * RecyclerView wants to know how many list items there are, so it knows when it gets to the
     * end of the list and should stop scrolling.
     * @return the number of posts in the array.
     */
    @Override
    public int getItemCount() {
        return posts.size();
    }

    /**
     * A ViewHolder class for our adapter that 'caches' the references to the
     * subviews, so we don't have to look them up each time.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView detailsTextView;
        public ImageView imageView;
        public TextView descriptionTextView;
        public TextView likesTextView;
        public Button likeButton;


        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            detailsTextView = (TextView) itemView.findViewById(R.id.detailsTextView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
            likesTextView = (TextView) itemView.findViewById(R.id.likesTextView);
            likeButton = (Button) itemView.findViewById(R.id.likeButton);
        }
    }
}