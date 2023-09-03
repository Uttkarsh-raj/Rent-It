package com.example.rent_it;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    Uri imageUrl;
    String myUrl="";
    StorageTask uploadTask;
    StorageReference storageReference;

    ImageView close,image_added;
    TextView post;
    EditText description,title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close=findViewById(R.id.close);
        image_added=findViewById(R.id.image_added);
        post=findViewById(R.id.post);
        description=findViewById(R.id.description);
        title=findViewById(R.id.title);
        storageReference = FirebaseStorage.getInstance().getReference("posts");

        image_added.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PostActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();


            }
        });
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mime= MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.setCancelable(true);
        progressDialog.show();
        SharedPreferences prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        String profile_id = prefs.getString("profileid", "none");


        if (imageUrl != null) {
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUrl));
            uploadTask = fileReference.putFile(imageUrl);
            Log.d("uploadTask", "Upload Task :" + uploadTask);
            uploadTask.continueWithTask(new Continuation() {

                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                        String postid = reference.push().getKey();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("postid", postid);
                        hashMap.put("postImage", myUrl);
                        hashMap.put("description", description.getText().toString());
                        hashMap.put("title", title.getText().toString());
                        hashMap.put("publisher", profile_id);
                        hashMap.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());

                        reference.child(postid).setValue(hashMap);
                        progressDialog.dismiss();
                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No Image Selected!", Toast.LENGTH_SHORT).show();
        }
    }


    //    private  void uploadImage(){
//
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Posting");
//        progressDialog.setCancelable(false); // Prevent users from canceling the dialog
//        progressDialog.show();
//
//        if(imageUrl!=null){
//            StorageReference fileReference =storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUrl));
//            uploadTask=fileReference.putFile(imageUrl);
//            uploadTask.continueWithTask(new Continuation() {
//                @Override
//                public Object then(@NonNull Task task) throws Exception {
//                    if (!task.isComplete()) {
//                        throw task.getException();
//                    }
//                    return fileReference.getDownloadUrl();
//                }
//            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if(task.isSuccessful()){
//                        Uri downloadUri=task.getResult();
//                        myUrl=downloadUri.toString();
//                        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts");
//                        String postid=reference.push().getKey();
//                        HashMap<String,Object> hashMap=new HashMap<>();
//                        hashMap.put("postid",postid);
//                        hashMap.put("postimage",myUrl);
//                        hashMap.put("description",description.getText().toString());
//                        hashMap.put("title",title.getText().toString());
//                        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
//
//                        reference.child(postid).setValue(hashMap);
//                        progressDialog.dismiss();
//                        startActivity(new Intent(PostActivity.this,MainActivity.class));
//                        finish();
//                    }else {
//                        Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }else {
//            Toast.makeText(this, "No Image Selected!", Toast.LENGTH_SHORT).show();
//        }
//
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUrl = data.getData();
            Log.d("ImageURL", "Image URL: " + imageUrl.toString()); // Log the image URL
            Glide.with(this).load(imageUrl).into(image_added);
        }
    }

}