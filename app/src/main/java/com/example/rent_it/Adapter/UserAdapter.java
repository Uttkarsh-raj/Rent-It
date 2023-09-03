package com.example.rent_it.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rent_it.Fragment.ProfileFragment;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mcontext;
    private List<User> mUser;
    private FirebaseUser firebaseUser;
    public UserAdapter(Context mcontext,List<User> mUser){
        this.mcontext=mcontext;
        this.mUser=mUser;
    }
    public  class ViewHolder extends  RecyclerView.ViewHolder{
        public TextView username;
        public CircleImageView image_profile;
        public TextView fullname;
        public TextView email;
        public Button btn_follow;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            username=itemView.findViewById(R.id.username);
            fullname=itemView.findViewById(R.id.fullname);
            email=itemView.findViewById(R.id.email);
            image_profile=itemView.findViewById(R.id.image_profile);
            btn_follow=itemView.findViewById(R.id.follow);
        }
    }
    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mcontext).inflate(R.layout.user_item,parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int i) {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
//        if (i < 0 || i >= mUser.size()) {
//            return; // Skip binding for out-of-bounds index
//        }
        final User user=mUser.get(i);
        if (user == null) {
            Log.d("UserAdapter", "User at index " + i + " is null.");
            return; // Skip binding for null user
        }
        Log.d("UserAdapter", "User: " + user.getId());
        Log.d("UserAdapter", "User: " + user.getUserName());
        holder.btn_follow.setVisibility(View.VISIBLE);
        holder.username.setText("@"+user.getUserName());
        holder.fullname.setText(user.getFullName());
        holder.email.setText(user.getEmail());
        Glide.with(mcontext).load(user.getImageUrl()).into(holder.image_profile);
        isFollowing(user.getId(),holder.btn_follow);
        if(user.getId()!=null && user.getId().equals(firebaseUser.getUid())){
            holder.btn_follow.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SharedPreferences.Editor editor= mcontext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
//                editor.putString("profileid", user.getId());
//                editor.apply();
                ((FragmentActivity)mcontext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .addToBackStack(null)
                        .commit();

//                ((FragmentActivity)mcontext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();
            }
        });

        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.btn_follow.getText().toString().equals("follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following").child(user.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId()).child("followers").child(firebaseUser.getUid()).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following").child(user.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId()).child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });
    }

//    private void isFollowing(String userId,Button button){
//        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following");
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
////                if(snapshot.child(userId).exists()){
////                    button.setText("following");
////                }else{
////                    button.setText("follow");
////                }
//                if (userId != null) { // Add this null check
//                    if(snapshot.child(userId).exists()){
//                        button.setText("following");
//                    } else {
//                        button.setText("follow");
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
private void isFollowing(String userId, Button button) {
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following");
    reference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (userId != null) {
                if (snapshot.child(userId).exists()) {
                    Log.d("UserAdapter", "isFollowing: User " + userId + " is following.");
                    button.setText("following");
                } else {
                    Log.d("UserAdapter", "isFollowing: User " + userId + " is not following.");
                    button.setText("follow");
                }
            } else {
                Log.d("UserAdapter", "isFollowing: userId is null.");
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.e("UserAdapter", "isFollowing: DatabaseError: " + error.getMessage());
        }
    });
}

    @Override
    public int getItemCount() {
        return mUser.size();
    }
}
