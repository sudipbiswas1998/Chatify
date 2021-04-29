package com.sudip.chatify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sudip.chatify.databinding.ActivityRegisterBinding;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    FirebaseAuth auth;
    ProgressDialog dialog;
    Boolean aBoolean;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create your profile");

        dialog = new ProgressDialog(this);
        dialog.setMessage("Registering...");
        dialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();


        aBoolean = true;

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 45);
            }
        });

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
                if(aBoolean== true) {
                    dialog.show();
                    auth.createUserWithEmailAndPassword(binding.regEmail.getText().toString(), binding.regPass.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = auth.getCurrentUser();
                                        profileSetup();
                                        Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(RegisterActivity.this);
                                                dlgAlert.setMessage("Please verify your email address using the link sent to your email address to login to your new account");
                                                dlgAlert.setTitle("Chatify");
                                                dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                    }
                                                });
                                                dlgAlert.setCancelable(false);
                                                dlgAlert.create().show();
                                            }
                                        });
                                    } else {
                                        dialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                                        Log.d("msg", "createUserWithEmail:failure", task.getException());
                                        Log.d("email", binding.regEmail.getText().toString());
                                        Log.d("password", binding.regPass.getText().toString());
                                    }
                                }
                            });
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!= null){
            if(data.getData() != null){
                binding.imageView.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }

    public void registerUser(){
        if(binding.regName.getText().toString().isEmpty()){
            binding.regName.setError("Full name is required");
            binding.regName.requestFocus();
            aBoolean = false;
            return;
        }
        if(binding.regEmail.getText().toString().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(binding.regEmail.getText().toString()).matches()){
            binding.regEmail.setError("Valid email address required");
            binding.regEmail.requestFocus();
            aBoolean = false;
            return;
        }
        if(binding.regPass.getText().toString().isEmpty() || binding.regPass.getText().toString().length()<6){
            binding.regPass.setError("Min password length should be 6 characters");
            binding.regPass.requestFocus();
            aBoolean = false;
            return;
        }
        else{
            aBoolean = true;
        }
    }
    public void profileSetup(){
        String name = binding.regName.getText().toString();
        if(selectedImage != null){
            StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
            reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                String uid = auth.getUid();
                                String email = auth.getCurrentUser().getEmail();
                                String name = binding.regName.getText().toString();

                                User user = new User(uid, name, email, imageUrl);

                                database.getReference()
                                        .child("users")
                                        .child(uid)
                                        .setValue(user);
                            }
                        });
                    }
                }
            });

        }else{
            String uid = auth.getUid();
            String email = auth.getCurrentUser().getEmail();

            User user = new User(uid, name, email, "No Image");

            database.getReference()
                    .child("users")
                    .child(uid)
                    .setValue(user);
        }
    }

}