package com.example.chatapp.Login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.MainActivity;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "LOGIN_OPTIONS_TAG";
    private static final int RC_SIGN_IN = 9001;
    EditText edtEmail, edtPassword;
    TextView tvClickToSignUp;
    Button btnSignIn;
    ImageView ivGoogle, ivFacebook, ivPasswordVisibility;
    CheckBox cbRememberPassword;
    SharedPreferences sharedPreferences;

    ProgressDialog dialog;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mUserReference;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions mGoogleSignInOptions;
    private boolean isEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        setControl();
        setEvent();
    }

    //Hàm khởi tạo
    public void setControl() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        tvClickToSignUp = findViewById(R.id.tvClickToSignUp);
        cbRememberPassword = findViewById(R.id.cbRememberPassword);
        sharedPreferences = getSharedPreferences("Password", MODE_PRIVATE);
        btnSignIn = findViewById(R.id.btnSignIn);
        ivGoogle = findViewById(R.id.ivGoogle);
        ivFacebook = findViewById(R.id.ivFacebook);
        ivPasswordVisibility = findViewById(R.id.ivPasswordVisibility);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        // Đặt thông tin Dialog khi đăng nhập
        dialog = new ProgressDialog(SignInActivity.this);
        dialog.setTitle("Đăng nhập");
        dialog.setMessage("Đang xác thực, vui lòng đợi!");

        //Cấu hình đăng nhập Google
        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions);
    }

    //Hàm xử lý sử kiện
    public void setEvent() {
        ivPasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEnabled) {
                    ivPasswordVisibility.setImageResource(R.drawable.icon_eye);
                    edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    isEnabled = false;
                } else {
                    ivPasswordVisibility.setImageResource(R.drawable.icon_eye_off);
                    edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isEnabled = true;
                }
            }
        });
        readPassword();
        // Xử lý khi ấn nút đăng nhập
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtEmail.getText().toString().isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Vui lòng nhập Email", Toast.LENGTH_SHORT).show();
                } else if (edtPassword.getText().toString().isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Vui lòng nhập Mật khẩu", Toast.LENGTH_SHORT).show();
                } else {
                    signInAction();
                }
            }
        });
        //Lưu thông tin dăng nhập
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
//            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
//            startActivity(intent);
        }
        // Ấn chữ đăng nhập sẽ chuyển sang màn hình đăng nhập
        tvClickToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        //Đăng nhập bằng Google
        ivGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                signIn();
                            }
                        });
            }
        });

    }

    private void signInAction() {
        dialog.show();
        mAuth.fetchSignInMethodsForEmail(edtEmail.getText().toString().trim())
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            List<String> signInMethods = result.getSignInMethods();
                            if (signInMethods != null && signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                                mAuth.signInWithEmailAndPassword(edtEmail.getText().toString().trim(), edtPassword.getText().toString().trim())
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    dialog.dismiss();
                                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    Toast.makeText(SignInActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                                    if (cbRememberPassword.isChecked()) {
                                                        writePassword();
                                                    } else {
                                                        removeSharedPreferences();
                                                    }
                                                } else {
                                                    dialog.dismiss();
                                                    Toast.makeText(SignInActivity.this, "Mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                dialog.dismiss();
                                Toast.makeText(SignInActivity.this, "Email không tồn tại trên hệ thống", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            dialog.dismiss();
                            Toast.makeText(SignInActivity.this, "Đăng nhập thất bại, vui lòng kiểm tra lại Email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void writePassword() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Email",edtEmail.getText().toString().trim());
        editor.putString("Password",edtPassword.getText().toString().trim());
        editor.putBoolean("Checked", true);
        editor.commit();
    }

    private void readPassword() {
        edtEmail.setText(sharedPreferences.getString("Email", ""));
        edtPassword.setText(sharedPreferences.getString("Password",""));
        cbRememberPassword.setChecked(sharedPreferences.getBoolean("Checked", false));
    }

    private void removeSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("Email");
        editor.remove("Password");
        editor.remove("Checked");
        editor.commit();
    }

    private void signIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Sign in suceess, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            firebaseDatabase.getReference().child("Users").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        FirebaseUser mUser = mAuth.getCurrentUser();
                                        User user = new User();
                                        String profilePic = "", describe="", statusActivity = "Online", gender ;
                                        user.setUserID(mAuth.getUid());
                                        user.setUserName(mUser.getDisplayName());
                                        user.setEmail(mUser.getEmail());
                                        user.setProfilePic(mUser.getPhotoUrl().toString());
                                        user.setPassword("");
                                        user.setDescribe("");
                                        user.setGender("");
                                        user.setStatusActivity("Online");
                                        firebaseDatabase.getReference().child("Users").child(mAuth.getUid()).setValue(user);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.w(TAG, "loadPost:onCancelled", error.toException());
                                }
                            });
                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(SignInActivity.this, "Sign in with Google", Toast.LENGTH_SHORT).show();

                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }
}