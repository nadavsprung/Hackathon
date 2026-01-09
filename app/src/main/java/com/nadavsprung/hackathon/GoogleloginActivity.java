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
                            errorMessage = "האפליקציה לא מוגדרת כראוי. בדוק את ה-SHA-1 fingerprint ב-Firebase Console";
                        } else if (statusCode == 10) {
                            // DEVELOPER_ERROR - Configuration issue with OAuth client
                            errorMessage = "שגיאת הגדרה: ה-OAuth client לא מוגדר כראוי. בדוק ב-Firebase Console:\n" +
                                    "1. ה-SHA-1 fingerprint נוסף לאפליקציה\n" +
                                    "2. OAuth client מוגדר\n" +
                                    "3. שם החבילה תואם";
                        } else if (statusCode == 7) {
                            errorMessage = "שגיאת רשת. בדוק את החיבור לאינטרנט";
                        } else if (statusCode == 8) {
                            errorMessage = "שגיאה ב-Google Play Services. אנא עדכן את Google Play Services";
                        } else if (statusCode == 16) {
                            errorMessage = "האפליקציה לא מאומתת. בדוק את ה-SHA-1 fingerprint ב-Firebase Console";
                        } else if (statusCode == 13) {
                            errorMessage = "שגיאת פנימית. נסה שוב מאוחר יותר";
                        }
                        String finalMessage = errorMessage;
                        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                            finalMessage += "\nפרטים: " + e.getMessage();
                        }
                        finalMessage += " (קוד: " + statusCode + ")";
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
                                // DEVELOPER_ERROR - Configuration issue with OAuth client
                                errorMessage = "שגיאת הגדרה (קוד 10): ה-OAuth client לא מוגדר כראוי. בדוק ב-Firebase Console:\n" +
                                        "1. ה-SHA-1 fingerprint נוסף\n" +
                                        "2. OAuth client מוגדר\n" +
                                        "3. שם החבילה תואם";
                            } else if (statusCode == 7) {
                                errorMessage = "שגיאת רשת. בדוק את החיבור לאינטרנט";
                            } else if (statusCode == 8) {
                                errorMessage = "שגיאה ב-Google Play Services. עדכן את Google Play Services";
                            } else if (statusCode == 16) {
                                errorMessage = "האפליקציה לא מאומתת. בדוק את ה-SHA-1 fingerprint ב-Firebase Console";
                            } else if (statusCode == 13) {
                                errorMessage = "שגיאת פנימית. נסה שוב מאוחר יותר";
                            }
                            String finalMsg = errorMessage;
                            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                                finalMsg += "\nפרטים: " + e.getMessage();
                            }
                            Toast.makeText(this, finalMsg + " (קוד: " + statusCode + ")", Toast.LENGTH_LONG).show();
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

        // הגדרת Google Sign-In - Temporarily disable if not configured
        try {
            String clientId = getString(R.string.default_web_client_id);
            if (clientId == null || clientId.isEmpty()) {
                // Silently disable Google Sign-In if not configured
                if (googleLoginButton != null) {
                    googleLoginButton.setVisibility(View.GONE);
                }
                mGoogleSignInClient = null;
            } else {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(clientId)
                        .requestEmail()
                        .requestProfile()
                        .build();

                mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            }
        } catch (Exception e) {
            // Silently disable Google Sign-In if there's an error
            if (googleLoginButton != null) {
                googleLoginButton.setVisibility(View.GONE);
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
        try {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        showLoading(false);
                        try {
                            if (task.isSuccessful()) {
                                // Verify user is logged in
                                if (mAuth.getCurrentUser() != null) {
                                    Toast.makeText(this, "התחברת בהצלחה!", Toast.LENGTH_SHORT).show();
                                    // Small delay to ensure everything is set up
                                    emailInput.postDelayed(() -> navigateToMainActivity(), 300);
                                } else {
                                    Toast.makeText(this, "שגיאה: המשתמש לא מחובר", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                String errorMessage = "שגיאה בהתחברות";
                                Exception exception = task.getException();
                                if (exception != null && exception.getMessage() != null) {
                                    String msg = exception.getMessage().toLowerCase();
                                    if (msg.contains("no user record") || msg.contains("user not found") || msg.contains("there is no user")) {
                                        errorMessage = "חשבון לא נמצא. האם תרצה להירשם?";
                                        // Offer to create account
                                        emailInput.postDelayed(() -> offerRegistration(email, password), 100);
                                        return;
                                    } else if (msg.contains("wrong password") || msg.contains("invalid password") || msg.contains("password is invalid")) {
                                        errorMessage = "סיסמה שגויה";
                                        passwordInput.setError("סיסמה שגויה");
                                        passwordInput.requestFocus();
                                    } else if (msg.contains("network") || msg.contains("connection") || msg.contains("unreachable")) {
                                        errorMessage = "שגיאת רשת. בדוק את החיבור לאינטרנט";
                                    } else if (msg.contains("too many requests")) {
                                        errorMessage = "יותר מדי ניסיונות. נסה שוב מאוחר יותר";
                                    } else {
                                        errorMessage = "שגיאה: " + exception.getMessage();
                                    }
                                }
                                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "שגיאה בעיבוד התוצאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        String errorMsg = "שגיאה בהתחברות";
                        if (e != null && e.getMessage() != null) {
                            String msg = e.getMessage().toLowerCase();
                            if (msg.contains("network") || msg.contains("connection")) {
                                errorMsg = "שגיאת רשת. בדוק את החיבור לאינטרנט";
                            } else {
                                errorMsg = "שגיאה: " + e.getMessage();
                            }
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        if (e != null) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            showLoading(false);
            Toast.makeText(this, "שגיאה בהתחלת התחברות: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
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
        try {
            // Verify user is actually logged in before navigating
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "שגיאה: המשתמש לא מחובר", Toast.LENGTH_LONG).show();
                return;
            }
            
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "שגיאה בניווט: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
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
                        try {
                            if (task.isSuccessful()) {
                                // Verify user is created and logged in
                                if (mAuth.getCurrentUser() != null) {
                                    Toast.makeText(this, "הרשמה הושלמה בהצלחה!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    // Small delay to ensure everything is set up
                                    etRegisterEmail.postDelayed(() -> navigateToMainActivity(), 300);
                                } else {
                                    Toast.makeText(this, "שגיאה: המשתמש לא נוצר", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                String errorMsg = "שגיאה בהרשמה";
                                Exception exception = task.getException();
                                if (exception != null && exception.getMessage() != null) {
                                    String msg = exception.getMessage().toLowerCase();
                                    if (msg.contains("already exists") || msg.contains("already in use") || msg.contains("email-already-in-use")) {
                                        errorMsg = "חשבון עם אימייל זה כבר קיים. נסה להתחבר";
                                    } else if (msg.contains("weak password") || msg.contains("password")) {
                                        errorMsg = "הסיסמה חלשה מדי. בחר סיסמה חזקה יותר (לפחות 6 תווים)";
                                    } else if (msg.contains("network") || msg.contains("connection") || msg.contains("unreachable")) {
                                        errorMsg = "שגיאת רשת. בדוק את החיבור לאינטרנט";
                                    } else if (msg.contains("invalid email")) {
                                        errorMsg = "כתובת אימייל לא תקינה";
                                    } else {
                                        errorMsg = "שגיאה: " + exception.getMessage();
                                    }
                                }
                                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "שגיאה בעיבוד הרשמה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        String errorMsg = "שגיאה בהרשמה";
                        if (e != null && e.getMessage() != null) {
                            String msg = e.getMessage().toLowerCase();
                            if (msg.contains("network") || msg.contains("connection")) {
                                errorMsg = "שגיאת רשת. בדוק את החיבור לאינטרנט";
                            } else {
                                errorMsg = "שגיאה: " + e.getMessage();
                            }
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        if (e != null) {
                            e.printStackTrace();
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