package com.example.rent_it.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.rent_it.LoginActivity;
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

public class ProfileFragment extends Fragment {
    ImageView image_profile;
    TextView posts,followers,following ,fullname,bio,username,email;
    Button log_out;
    FirebaseUser firebaseUser;
    String profile_id;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences prefs=getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profile_id=prefs.getString("profileid","none");
        image_profile=view.findViewById(R.id.image_profile);
        bio=view.findViewById(R.id.bio);
        email=view.findViewById(R.id.email);
        username=view.findViewById(R.id.username);
        fullname=view.findViewById(R.id.fullname);
        posts=view.findViewById(R.id.posts);
        followers=view.findViewById(R.id.followers);
        following=view.findViewById(R.id.following);
        log_out=view.findViewById(R.id.log_out);

        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut(); // Sign out the user

//                SharedPreferences.Editor editor = prefs.edit();// Clear the SharedPreferences
//                editor.clear();
//                editor.apply(); // or editor.commit() if you want immediate write

                Intent intent = new Intent(getContext(), LoginActivity.class); // Replace with your sign-up or login activity class
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the task stack
                startActivity(intent);
                getActivity().finish();
            }
        });
        userInfo(firebaseUser);
        getFollowers();
        getNrPosts();
        return view;
    }
//    private  void userInfo(FirebaseUser user){
//        Log.d("DataChange", "userInfo: "+profile_id);
//        String u =user.getUid();
//        Log.d("DataChange", "userInfo: "+u);
//        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users").child(u);
//        Log.d("DataChange", "userInfo: "+reference);
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (getContext()==null){
//                    return;
//                }
//               try {
//                   User user =snapshot.getValue(User.class);
//                   Glide.with(getContext()).load(user.getImageUrl()).into(image_profile);
//                   username.setText(user.getUserName());
//                   bio.setText(user.getBio());
//                   email.setText(user.getEmail());
//                   fullname.setText("@"+user.getFullName());
//               }catch (Exception e){
//                   Log.d("DataChange", "onDataChange: "+e);
//               }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
private void userInfo(FirebaseUser user) {
    Log.d("onDataChange", "userInfo: " + profile_id);
    String u = user.getUid();
    Log.d("onDataChange", "userInfo: " + u);
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profile_id);
    Log.d("onDataChange", "userInfo: " + reference);
    reference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (getContext() == null) {
                return;
            }
            try {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        String imageUrl = user.getImageUrl();
                        if (imageUrl != null) {
                            Glide.with(getContext()).load(imageUrl).into(image_profile);
                            username.setText(user.getUserName());
                            bio.setText(user.getBio());
                            email.setText(user.getEmail());
                            fullname.setText("@" + user.getFullName());
                        } else {
                            Log.d("DataChange", "onDataChange: Image URL is null");
                        }
                    } else {
                        Log.d("DataChange", "onDataChange: User object is null");
                    }
                } else {
                    Log.d("DataChange", "onDataChange: Snapshot does not exist");
                }
            } catch (Exception e) {
                Log.e("DataChange", "onDataChange: Error", e);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.e("DataChange", "onCancelled: Database error", error.toException());
        }
    });
}


    private  void getFollowers(){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Follow").child(firebaseUser.getUid()).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference reference1= FirebaseDatabase.getInstance().getReference("Follow").child(firebaseUser.getUid()).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void getNrPosts(){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i=0;
                for (DataSnapshot snap: snapshot.getChildren()){
                    Post post =snap.getValue(Post.class);
                    if(post.getPublisher().equals(profile_id)){
                        i++;
                    }
                }
                posts.setText(""+i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}