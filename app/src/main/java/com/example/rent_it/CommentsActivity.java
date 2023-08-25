package com.example.rent_it;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rent_it.Adapter.CommentAdapter;
import com.example.rent_it.Model.Comment;
import com.example.rent_it.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    EditText addComment;
    ImageView image_profile;
    TextView post;

    String postid;
    String publisherid;

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

//        Toolbar toolbar=findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("Comments");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList=new ArrayList<>();
        commentAdapter=new CommentAdapter(this,commentList);
        recyclerView.setAdapter(commentAdapter);


        addComment=findViewById(R.id.add_comment);
        image_profile=findViewById(R.id.image_profile);
        post=findViewById(R.id.post);
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        Intent intent=getIntent();
        postid=intent.getStringExtra("postid");//check for error if any here postId
        publisherid=intent.getStringExtra("publisherid");//check for error if any here postId
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(addComment.getText().toString().equals("")){
                    Toast.makeText(CommentsActivity.this, "You can't send empty Comments.", Toast.LENGTH_SHORT).show();
                }else{
                    addComment();
                }
            }
        });

        getImage();
        readComments();
    }
//    private void addComment(){
//        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users").child(postid);
//
//        HashMap<String,Object>hashMap=new HashMap<>();
//        hashMap.put("comment",addComment.getText().toString());
//        hashMap.put("publisher",firebaseUser.getUid());
//        reference.push().setValue(hashMap);
//        addComment.setText("");
//    }
private void addComment() {
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid); // Corrected reference

    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put("comment", addComment.getText().toString());
    hashMap.put("publisher", firebaseUser.getUid());
    reference.push().setValue(hashMap);
    addComment.setText("");
}


    private void getImage(){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("CommentError", "DataSnapshot value: " + snapshot.getValue());
                Log.d("CommentError", "DataSnapshot value: " + firebaseUser.getUid());
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    Glide.with(getApplicationContext()).load(user.getImageUrl()).into(image_profile);
                } else {
                    Log.d("CommentError", "User object is null");
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void readComments(){
//        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Comments").child(postid);
//
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                commentList.clear();
//                for (DataSnapshot snapshot1: snapshot.getChildren()){
//                    Comment comment=snapshot1.getValue(Comment.class);
//                    commentList.add(comment);
//                }
//                commentAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
private void readComments() {
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

    reference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            commentList.clear(); // Clear the list before adding new comments
            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                Comment comment = snapshot1.getValue(Comment.class);
                commentList.add(comment);
            }
            commentAdapter.notifyDataSetChanged(); // Notify the adapter after adding comments
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            // Handle onCancelled
        }
    });
}


}