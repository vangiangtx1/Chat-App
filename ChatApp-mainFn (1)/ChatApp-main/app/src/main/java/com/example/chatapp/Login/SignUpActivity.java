package com.example.chatapp.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class    SignUpActivity extends AppCompatActivity {
    ImageView ivSignUpPasswordVisible, ivSignUpConfirmPasswordVisible;
    Button btnSignUp;
    RadioButton signUpRadMan, signUpRadWoman;
    EditText edtUserName, edtPassword, edtEmail, edtConfirmPassword;
    TextView tvClickToSignIn;
    ProgressDialog dialog;
     FirebaseAuth mAuth;
    DatabaseReference mUserReference;
    FirebaseStorage storage;
    StorageReference mStorageReference;
    String profilePic, describe="", statusActivity = "Offline", gender;
    String userID, userName, email, password;
    private boolean isEnabled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setControl();
        setEvent();
    }
    public void setControl(){
        btnSignUp = findViewById(R.id.btnSignUp);
        signUpRadMan = findViewById(R.id.signUpRadMan);
        signUpRadWoman = findViewById(R.id.signUpRadWoman);
        tvClickToSignIn = findViewById(R.id.tvClickToSignIn);
        mAuth = FirebaseAuth.getInstance();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        edtUserName = findViewById(R.id.edtUserName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        ivSignUpPasswordVisible = findViewById(R.id.ivSignUpPasswordVisible);
        ivSignUpConfirmPasswordVisible = findViewById(R.id.ivSignUpConfirmPasswordVisible);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        storage = FirebaseStorage.getInstance();
        mStorageReference = storage.getReference().child("profilePic/default_avatar.png");

        dialog = new ProgressDialog(SignUpActivity.this);
        dialog.setTitle("Đang tạo tài khoản");
        dialog.setMessage("Chúng tôi đang tạo tài khoản cho bạn");
    }
    public void setEvent(){
        ivSignUpPasswordVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEnabled) {
                    ivSignUpPasswordVisible.setImageResource(R.drawable.icon_eye);
                    edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    isEnabled = false;
                } else {
                    ivSignUpPasswordVisible.setImageResource(R.drawable.icon_eye_off);
                    edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isEnabled = true;
                }
            }
        });

        ivSignUpConfirmPasswordVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEnabled) {
                    ivSignUpConfirmPasswordVisible.setImageResource(R.drawable.icon_eye);
                    edtConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    isEnabled = false;
                } else {
                    ivSignUpConfirmPasswordVisible.setImageResource(R.drawable.icon_eye_off);
                    edtConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isEnabled = true;
                }
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtUserName.getText().toString().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Vui lòng nhập Tên người dùng", Toast.LENGTH_SHORT).show();
                } else if (edtEmail.getText().toString().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Vui lòng nhập Email", Toast.LENGTH_SHORT).show();
                } else if (!signUpRadMan.isChecked() && !signUpRadWoman.isChecked()) {
                    Toast.makeText(SignUpActivity.this, "Vui lòng chọn Giới tính của bạn", Toast.LENGTH_SHORT).show();
                } else if (edtPassword.getText().toString().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Vui lòng nhập Mật khẩu", Toast.LENGTH_SHORT).show();
                } else if (edtConfirmPassword.getText().toString().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Vui lòng nhập Xác nhận mật khẩu", Toast.LENGTH_SHORT).show();
                } else if (!edtConfirmPassword.getText().toString().trim().equals(edtPassword.getText().toString().trim())) {
                    Toast.makeText(SignUpActivity.this, "Xác nhận mật khẩu không chính xác, vui lòng kiểm tra lại", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.show();
                    mAuth.fetchSignInMethodsForEmail(edtEmail.getText().toString().trim()).
                            addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    if (task.isSuccessful()) {
                                        SignInMethodQueryResult result = task.getResult();
                                        boolean emailExists = result.getSignInMethods().size() > 0;
                                        if (emailExists) {
                                            dialog.dismiss();
                                            Toast.makeText(SignUpActivity.this, "Email đã được sử dụng, vui lòng sử dụng email khác", Toast.LENGTH_SHORT).show();
                                        } else {
                                            mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString().trim(), edtPassword.getText().toString().trim())
                                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                            dialog.dismiss();
                                                            if (task.isSuccessful()) {
                                                                userID = task.getResult().getUser().getUid();
                                                                userName = edtUserName.getText().toString().trim();
                                                                email = edtEmail.getText().toString().trim();
                                                                password = edtPassword.getText().toString().trim();

                                                                mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                    @Override
                                                                    public void onSuccess(Uri uri) {
                                                                        profilePic = uri.toString();
                                                                        if (signUpRadMan.isChecked())
                                                                            gender = signUpRadMan.getText().toString();
                                                                        else gender = signUpRadWoman.getText().toString();
                                                                        User user = new User(profilePic,userName,email,password,userID,describe,gender, statusActivity);
                                                                        mUserReference.child(userID).setValue(user);
                                                                    }
                                                                });
                                                                Toast.makeText(SignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                                                startActivity(intent);
                                                            } else {
                                                                Toast.makeText(SignUpActivity.this, "Lỗi khi kiểm tra email", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                }
                            });
                }
            }
        });

        // Ấn chữ đăng nhập
        tvClickToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvClickToSignIn.clearFocus();
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }
}