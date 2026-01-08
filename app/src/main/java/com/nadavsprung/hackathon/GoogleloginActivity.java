package com.nadavsprung.hackathon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleloginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton, googleLoginButton;
    private TextView forgotPassword, registerLink;
    private ProgressBar progressBar; // מומלץ להוסיף ב-Layout

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    // הגדרה של ה-Launcher החדש להחלפת onActivityResult
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlelogin);

        mAuth = FirebaseAuth.getInstance();

        // הגדרת Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // וודא שקיים ב-google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        googleLoginButton = findViewById(R.id.googleLoginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        registerLink = findViewById(R.id.registerLink);
        // progressBar = findViewById(R.id.progressBar); // שחרר את ההערה אם הוספת ל-XML
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> emailPasswordLogin());
        googleLoginButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent); // שימוש ב-Launcher החדש
        });

        forgotPassword.setOnClickListener(v -> resetPassword());
        registerLink.setOnClickListener(v -> showRegisterDialog());
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        navigateToMainActivity();
                    } else {
                        Toast.makeText(this, "שגיאה בחיבור ל-Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void emailPasswordLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("אימייל לא תקין");
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("סיסמה חייבת להכיל לפחות 6 תווים");
            return;
        }

        showLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        navigateToMainActivity();
                    } else {
                        Toast.makeText(this, "שגיאה: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetPassword() {
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "הכנס אימייל תקין לאיפוס", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "הוראות לאיפוס נשלחו למייל", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        loginButton.setEnabled(!isLoading);
        googleLoginButton.setEnabled(!isLoading);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showRegisterDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_register, null);
        builder.setView(dialogView);

        EditText etRegisterEmail = dialogView.findViewById(R.id.et_register_email);
        EditText etRegisterPassword = dialogView.findViewById(R.id.et_register_password);
        EditText etRegisterPasswordConfirm = dialogView.findViewById(R.id.et_register_password_confirm);
        Button btnRegister = dialogView.findViewById(R.id.btn_register);
        ProgressBar progressBar = dialogView.findViewById(R.id.progress_bar_register);

        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        btnRegister.setOnClickListener(v -> {
            String email = etRegisterEmail.getText().toString().trim();
            String password = etRegisterPassword.getText().toString().trim();
            String passwordConfirm = etRegisterPasswordConfirm.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etRegisterEmail.setError("אימייל לא תקין");
                return;
            }

            if (password.length() < 6) {
                etRegisterPassword.setError("סיסמה חייבת להכיל לפחות 6 תווים");
                return;
            }

            if (!password.equals(passwordConfirm)) {
                etRegisterPasswordConfirm.setError("סיסמאות לא תואמות");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "הרשמה הושלמה בהצלחה!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            navigateToMainActivity();
                        } else {
                            Toast.makeText(this, "שגיאה בהרשמה: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            navigateToMainActivity();
        }
    }
}