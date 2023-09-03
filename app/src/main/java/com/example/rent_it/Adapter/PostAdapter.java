package com.example.rent_it.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rent_it.CommentsActivity;
import com.example.rent_it.Model.Post;
import com.example.rent_it.Model.User;
import com.example.rent_it.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context mContext;
    public List<Post> mPost;
    private FirebaseUser firebaseUser;

    public PostAdapter(Context context, List<Post> postLists) {
        this.mContext=context;
        this.mPost=postLists;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.post_item,parent,false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        Post post=mPost.get(position);
        try{
            Log.d("InPost", "PostError: "+post.getPostImage());
            Log.d("InPost", "PostError: "+holder.post_image);
            Glide.with(mContext).load(post.getPostImage()).into(holder.post_image);
            if(post.getDescription().equals("")){
                holder.discription.setVisibility(View.GONE);
            }else{
                holder.discription.setVisibility(View.VISIBLE);
                holder.discription.setText(post.getDescription());
            }
            if(post.getTitle().equals("")){
                holder.title.setVisibility(View.GONE);
                holder.email.setVisibility(View.GONE);
            }else{
                holder.title.setVisibility(View.VISIBLE);
                holder.title.setText(post.getTitle());
                holder.email.setVisibility(View.VISIBLE);
                holder.email.setText(post.getEmail());
            }
            publisherInfo(holder.image_profile,holder.username,holder.email,holder.publisher,post.getPublisher());
            isLiked(post.getPostId(),holder.like);
            nrLikes(holder.likes,post.getPostId());
            getComments(post.getPostId(),holder.comments);
            holder.like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.like.getTag().equals("like")){
                        FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId()).child(firebaseUser.getUid()).setValue(true);
                    }else{
                        FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId()).child(firebaseUser.getUid()).removeValue();
                    }
                }
            });
            holder.comments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(mContext, CommentsActivity.class);
                    intent.putExtra("postid",post.getPostId());
                    intent.putExtra("publisherid",post.getPostId());
                    mContext.startActivity(intent);

                }
            });
            holder.comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(mContext, CommentsActivity.class);
                    intent.putExtra("postid",post.getPostId());
                    intent.putExtra("publisherid",post.getPostId());
                    mContext.startActivity(intent);

                }
            });
        }catch (Exception e){
            Log.d("PostError", "PostError: "+post.getPostImage());
            Log.d("PostError", "PostError: "+mContext);
            Log.d("PostError", "PostError: "+post.getDescription());
            Log.d("PostError", "PostError: "+e.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView image_profile,post_image,like,comment,save;
        public TextView username,likes,publisher,discription,comments,title,email;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image_profile=itemView.findViewById(R.id.image_profile);
            post_image=itemView.findViewById(R.id.post_image);
            like=itemView.findViewById(R.id.like);
            likes=itemView.findViewById(R.id.likes);
            comment=itemView.findViewById(R.id.comment);
            save=itemView.findViewById(R.id.bookmark);
            discription=itemView.findViewById(R.id.description);
            title=itemView.findViewById(R.id.title);
            comments=itemView.findViewById(R.id.comments);
            username=itemView.findViewById(R.id.username);
            email=itemView.findViewById(R.id.email);
        }
    }

    private void getComments(String postid,TextView comments){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comments.setText("View All "+ snapshot.getChildrenCount()+" Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private  void isLiked(String postId,ImageView imageView){
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                }else{
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void nrLikes(TextView likes,String postId){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likes.setText(snapshot.getChildrenCount()+" likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void publisherInfo(ImageView image_profile,TextView userName,TextView email,TextView publisher,String userId){
//        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users").child(userId);
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                User user=snapshot.getValue(User.class);
//                if(user!=null) {
//                    try{
//                        Glide.with(mContext).load(user.getImageUrl()).into(image_profile);
//                        userName.setText(user.getUserName());
//                        publisher.setText(user.getUserName());
//                        email.setText(user.getEmail());
//                    }catch (Exception e){
//                        Log.d("PostError", "PostError: "+user.getImageUrl());
//                        Log.d("PostError", "PostError: "+mContext);
//                        Log.d("PostError", "PostError: "+image_profile);
//                        Log.d("PostError", "PostError: "+e.getMessage());
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
private void publisherInfo(ImageView image_profile, TextView userName, TextView email, TextView publisher, String userId) {
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
    reference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            User user = snapshot.getValue(User.class);
            if (user != null) {
                try {
                    Glide.with(mContext).load(user.getImageUrl()).into(image_profile);
                    userName.setText(user.getUserName());
                    publisher.setText(user.getUserName());
                    email.setText(user.getEmail());
                } catch (Exception e) {
                    Log.d("PostError", "Error loading user data: " + e.getMessage());
                }
            } else {
                // Handle the case where user data is not available or not properly set in the post
                // You can hide or display default values here
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            // Handle onCancelled
        }
    });
}

}
