package com.example.rent_it;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rent_it.Model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    ProgressDialog progressDialog;
    private static final int REQ_ONE_TAP = 2;
    private boolean showOneTapUI = true;

    private static final int AC_SIGN_IN=9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button signIn=(Button) findViewById(R.id.btn_signIn);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);
        mFirebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading....");
        progressDialog.setMessage("Signing you in.... Please wait...");
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn(){
        Intent signInIntent=mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,AC_SIGN_IN);
    }

    @Override
    protected  void  onStart(){
        super.onStart();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if(mFirebaseUser!=null){
            Log.d(TAG,"User Already logged in...");
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==AC_SIGN_IN){
            Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account=task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }catch (ApiException e){
                Log.w(TAG,"Google Sign in failed....",e);
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "FirebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Sign In with Credentials Success");
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

                    // Check if the user already exists in the database
                    reference.orderByChild("email").equalTo(user.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // User already exists, no need to add again
                                progressDialog.dismiss();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                // User doesn't exist, add user data
                                try {
                                    Log.d("onDataChange", "onDataChange: "+user.getEmail());
                                    String userId = reference.push().getKey();
                                    User userData = new User(userId, user.getDisplayName().split(" ")[0], user.getDisplayName(), user.getEmail(), user.getPhotoUrl().toString(), "Hey there! I am looking for a tenant");
                                    reference.child(userId).setValue(userData);
                                    SharedPreferences prefs=getSharedPreferences("PREFS", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    Log.d("onDataChange", "onDataChange: "+userId);
                                    editor.clear();
                                    editor.apply();
                                    editor.putString("profileid",userId);
                                    editor.apply();
                                    Log.d("onDataChange", "onDataChange: "+prefs.getString("profileid","none"));
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }catch (Exception e){
                                    Log.d("LoginError", "LoginError: "+e.getMessage());
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle onCancelled
                        }
                    });
                } else {
                    Log.d("Sign In with Credentials", "Sign In with Credentials: Failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Sign In with Credentials: Failure", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


//    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
//
//        Log.d(TAG,"FirebaseAuthWithGoogle:"+acct.getId());
//        AuthCredential credential= GoogleAuthProvider.getCredential(acct.getIdToken(),null);
//        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()){
//                    Log.d(TAG,"Sign In with Credentials Success");
//                    FirebaseUser user= mFirebaseAuth.getCurrentUser();
//
//
//                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
//                    String userId = reference.push().getKey();
//                    HashMap<String, Object> hashMap = new HashMap<>();
//                    hashMap.put("id", userId);
//                    hashMap.put("imageUrl", user.getPhotoUrl());
//                    hashMap.put("fullName", user.getDisplayName());
//                    hashMap.put("userName", user.getDisplayName().split(" ")[0]);
//                    hashMap.put("bio", "Hey there! I am looking for a tenant");
//                    hashMap.put("email", user.getEmail());
//
//                    reference.child(userId).setValue(hashMap);
//                    progressDialog.dismiss();
//                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
//                    startActivity(intent);
//                }else{
//                    Log.d(TAG,"Sign In with Credentials: Failure",task.getException());
//                    Toast.makeText(LoginActivity.this, "Sign In with Credentials: Failure", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//    }
//
//    private void uploadImage() {
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Posting");
//        progressDialog.setCancelable(true);
//        progressDialog.show();
//
//        if (imageUrl != null) {
//            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUrl));
//            uploadTask = fileReference.putFile(imageUrl);
//            Log.d("uploadTask", "Upload Task :" + uploadTask);
//            uploadTask.continueWithTask(new Continuation() {
//
//                @Override
//                public Object then(@NonNull Task task) throws Exception {
//                    if (!task.isSuccessful()) {
//                        throw task.getException();
//                    }
//                    return fileReference.getDownloadUrl();
//                }
//            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if (task.isSuccessful()) {
//                        Uri downloadUri = task.getResult();
//                        myUrl = downloadUri.toString();
//                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
//                        String postid = reference.push().getKey();
//                        HashMap<String, Object> hashMap = new HashMap<>();
//                        hashMap.put("postid", postid);
//                        hashMap.put("postImage", myUrl);
//                        hashMap.put("description", description.getText().toString());
//                        hashMap.put("title", title.getText().toString());
//                        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
//                        hashMap.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
//
//                        reference.child(postid).setValue(hashMap);
//                        progressDialog.dismiss();
//                        startActivity(new Intent(PostActivity.this, MainActivity.class));
//                        finish();
//                    } else {
//                        Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(PostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        } else {
//            Toast.makeText(this, "No Image Selected!", Toast.LENGTH_SHORT).show();
//        }
//    }

}