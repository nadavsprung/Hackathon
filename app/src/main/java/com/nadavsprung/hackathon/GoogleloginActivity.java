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
                    if (data == null) {
                        Toast.makeText(this, "שגיאה: לא התקבלו נתונים מהתחברות Google", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null && account.getIdToken() != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        } else {
                            Toast.makeText(this, "שגיאה: לא ניתן לקבל מידע מהחשבון", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ApiException e) {
                        String errorMessage = "שגיאה בהתחברות עם Google";
                        int statusCode = e.getStatusCode();
                        if (statusCode == 12500) {
                            errorMessage = "האפליקציה לא מוגדרת כראוי. אנא בדוק את ההגדרות ב-Firebase Console";
                        } else if (statusCode == 10) {
                            errorMessage = "החשבון לא נמצא";
                        } else if (statusCode == 7) {
                            errorMessage = "שגיאת רשת. בדוק את החיבור לאינטרנט";
                        } else if (statusCode == 8) {
                            errorMessage = "שגיאה ב-Google Play Services. אנא עדכן את Google Play Services";
                        } else if (statusCode == 16) {
                            errorMessage = "האפליקציה לא מאומתת. בדוק את ה-SHA-1 fingerprint ב-Firebase Console";
                        }
                        String finalMessage = errorMessage;
                        if (e.getMessage() != null) {
                            finalMessage += " (קוד שגיאה: " + statusCode + ")";
                        }
                        Toast.makeText(this, finalMessage, Toast.LENGTH_LONG).show();
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    // Sometimes errors are returned as RESULT_CANCELED, try to check for errors
                    Intent data = result.getData();
                    if (data != null) {
                        try {
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            // If we got here without exception, but result was canceled, it was likely user cancel
                            Toast.makeText(this, "בוטל על ידי המשתמש", Toast.LENGTH_SHORT).show();
                        } catch (ApiException e) {
                            // There was an actual error
                            String errorMessage = "שגיאה בהתחברות עם Google";
                            int statusCode = e.getStatusCode();
                            if (statusCode == 12500) {
                                errorMessage = "האפליקציה לא מוגדרת כראוי. בדוק את ה-SHA-1 fingerprint ב-Firebase Console";
                            } else if (statusCode == 10) {
                                errorMessage = "החשבון לא נמצא";
                            } else if (statusCode == 7) {
                                errorMessage = "שגיאת רשת. בדוק את החיבור לאינטרנט";
                            } else if (statusCode == 8) {
                                errorMessage = "שגיאה ב-Google Play Services. עדכן את Google Play Services";
                            } else if (statusCode == 16) {
                                errorMessage = "האפליקציה לא מאומתת. בדוק את ה-SHA-1 fingerprint ב-Firebase Console";
                            }
                            Toast.makeText(this, errorMessage + " (קוד: " + statusCode + ")", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // User actually canceled
                        Toast.makeText(this, "התחברות Google בוטלה", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "שגיאה לא ידועה בהתחברות Google (קוד תוצאה: " + result.getResultCode() + ")", Toast.LENGTH_LONG).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlelogin);

        mAuth = FirebaseAuth.getInstance();

        initializeViews();

        // הגדרת Google Sign-In
        try {
            String clientId = getString(R.string.default_web_client_id);
            if (clientId == null || clientId.isEmpty()) {
                Toast.makeText(this, "שגיאה בהגדרת Google Sign-In: חסר Client ID", Toast.LENGTH_LONG).show();
                if (googleLoginButton != null) {
                    googleLoginButton.setEnabled(false);
                }
            } else {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(clientId)
                        .requestEmail()
                        .requestProfile()
                        .build();

                mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            }
        } catch (Exception e) {
            Toast.makeText(this, "שגיאה בהגדרת Google Sign-In: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (googleLoginButton != null) {
                googleLoginButton.setEnabled(false);
            }
            mGoogleSignInClient = null;
        }

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
            if (mGoogleSignInClient != null) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent); // שימוש ב-Launcher החדש
            } else {
                Toast.makeText(this, "Google Sign-In לא זמין. אנא בדוק את ההגדרות", Toast.LENGTH_LONG).show();
            }
        });

        forgotPassword.setOnClickListener(v -> resetPassword());
        registerLink.setOnClickListener(v -> showRegisterDialog());
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            Toast.makeText(this, "שגיאה: לא ניתן לקבל טוקן מאימות", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "התחברת בהצלחה!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    } else {
                        String errorMessage = "שגיאה בחיבור ל-Firebase";
                        if (task.getException() != null) {
                            errorMessage += ": " + task.getException().getMessage();
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void emailPasswordLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("אימייל לא תקין");
            emailInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("סיסמה חייבת להכיל לפחות 6 תווים");
            passwordInput.requestFocus();
            return;
        }

        showLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "התחברת בהצלחה!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    } else {
                        String errorMessage = "שגיאה בהתחברות";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            String msg = task.getException().getMessage().toLowerCase();
                            if (msg.contains("no user record") || msg.contains("user not found")) {
                                errorMessage = "חשבון לא נמצא. האם תרצה להירשם?";
                                // Offer to create account
                                offerRegistration(email, password);
                                return;
                            } else if (msg.contains("wrong password") || msg.contains("invalid password")) {
                                errorMessage = "סיסמה שגויה";
                                passwordInput.setError("סיסמה שגויה");
                                passwordInput.requestFocus();
                            } else if (msg.contains("network") || msg.contains("connection")) {
                                errorMessage = "שגיאת רשת. בדוק את החיבור לאינטרנט";
                            } else {
                                errorMessage = "שגיאה: " + task.getException().getMessage();
                            }
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void offerRegistration(String email, String password) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("חשבון לא נמצא")
                .setMessage("חשבון עם האימייל הזה לא קיים. האם תרצה להירשם?")
                .setPositiveButton("כן, הירשם", (dialog, which) -> {
                    // Create account
                    showLoading(true);
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, task -> {
                                showLoading(false);
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "הרשמה הושלמה בהצלחה!", Toast.LENGTH_SHORT).show();
                                    navigateToMainActivity();
                                } else {
                                    String errorMsg = "שגיאה בהרשמה";
                                    if (task.getException() != null) {
                                        String msg = task.getException().getMessage().toLowerCase();
                                        if (msg.contains("already exists") || msg.contains("already in use")) {
                                            errorMsg = "חשבון עם אימייל זה כבר קיים. נסה להתחבר";
                                        } else if (msg.contains("weak password")) {
                                            errorMsg = "הסיסמה חלשה מדי. בחר סיסמה חזקה יותר";
                                        } else {
                                            errorMsg = "שגיאה: " + task.getException().getMessage();
                                        }
                                    }
                                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void resetPassword() {
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "הכנס אימייל תקין לאיפוס", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "הוראות לאיפוס נשלחו למייל", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMsg = "שגיאה בשליחת אימייל לאיפוס";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            String msg = task.getException().getMessage().toLowerCase();
                            if (msg.contains("user not found")) {
                                errorMsg = "חשבון עם אימייל זה לא נמצא";
                            } else if (msg.contains("network") || msg.contains("connection")) {
                                errorMsg = "שגיאת רשת. בדוק את החיבור לאינטרנט";
                            } else {
                                errorMsg = "שגיאה: " + task.getException().getMessage();
                            }
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
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
                            String errorMsg = "שגיאה בהרשמה";
                            if (task.getException() != null) {
                                String msg = task.getException().getMessage();
                                if (msg != null) {
                                    msg = msg.toLowerCase();
                                    if (msg.contains("already exists") || msg.contains("already in use")) {
                                        errorMsg = "חשבון עם אימייל זה כבר קיים. נסה להתחבר";
                                    } else if (msg.contains("weak password")) {
                                        errorMsg = "הסיסמה חלשה מדי. בחר סיסמה חזקה יותר";
                                    } else if (msg.contains("network") || msg.contains("connection")) {
                                        errorMsg = "שגיאת רשת. בדוק את החיבור לאינטרנט";
                                    } else {
                                        errorMsg = "שגיאה: " + task.getException().getMessage();
                                    }
                                }
                            }
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        Toast.makeText(this, "שגיאה בהרשמה: " + e.getMessage(), Toast.LENGTH_LONG).show();
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