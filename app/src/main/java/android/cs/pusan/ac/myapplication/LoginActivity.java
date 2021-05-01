package android.cs.pusan.ac.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // [START declare_auth]
    private FirebaseAuth firebaseAuth;
    // [END declare_auth]

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button emailLoginBtn;
    private Button emailSignupBtn;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = (EditText) findViewById(R.id.text_login_id);
        editTextPassword = (EditText) findViewById(R.id.text_login_password);

        emailLoginBtn = (Button)findViewById(R.id.login_button);
        emailSignupBtn = (Button)findViewById(R.id.register_button);


        emailLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                if(email.length()==0 || password.length()==0){
                    Toast.makeText(LoginActivity.this, "Enter email or password",Toast.LENGTH_SHORT).show();
                    return;
                }
                loginUser(email,password);
            }
        });

        emailSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class ));
            }
        });

//        mAuthListener = new FirebaseAuth.AuthStateListener() {   //이거 없어도 일단 작동은 함
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if(user != null) {
//                    //User is signed in
//                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
//                    startActivity(intent);
//                    finish();
//                }else{
//                    //User is signed out
//                    Intent intent = new Intent(LoginActivity.this,LoginActivity.class);
//                    startActivity(intent);
//
//                }
//            }
//        };

    }

    private void loginUser(String email, String password) {
        // [START sign_in_with_email]
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(firebaseAuth.getCurrentUser().isEmailVerified()){
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            }else{
                                Toast.makeText(LoginActivity.this, "please verify your email", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }
                    }
                });
        // [END sign_in_with_email]
    }

//    private void updateUI(FirebaseUser user) {
//        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
//    }

    @Override
    public void onStart(){
        super.onStart();

        // 활동을 초기화할 때 사용자가 현재 로그인되어 있는지 확인합니다. -> 제대로 작동안함
//        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener != null){
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }


}