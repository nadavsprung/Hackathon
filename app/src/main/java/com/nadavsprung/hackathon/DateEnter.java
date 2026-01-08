package com.nadavsprung.hackathon;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class DateEnter extends AppCompatActivity {

    // הגדרת משתנים לרכיבי המסך
    private EditText etLongText;
    private ImageView ivPreview;
    private TextView tvFileName;

    // משתנים לשמירת המידע שנבחר (URI)
    private Uri selectedImageUri = null;
    private Uri selectedFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_enter);

        // חיבור הרכיבים מה-XML לקוד
        etLongText = findViewById(R.id.et_long_text);
        Button btnUploadImage = findViewById(R.id.btn_upload_image);
        ivPreview = findViewById(R.id.iv_preview);
        Button btnUploadFile = findViewById(R.id.btn_upload_file);
        tvFileName = findViewById(R.id.tv_file_name);
        Button btnSubmit = findViewById(R.id.btn_submit);

        // --- הגדרת הבוחרים (Launchers) ---

        // 1. בוחר תמונות
        ActivityResultLauncher<String> imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivPreview.setVisibility(View.VISIBLE);
                        ivPreview.setImageURI(uri); // מציג את התמונה למשתמש
                    }
                }
        );

        // 2. בוחר קבצים
        ActivityResultLauncher<String> filePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedFileUri = uri;
                        tvFileName.setText("קובץ נבחר: " + uri.getLastPathSegment());
                    }
                }
        );

        // --- הגדרת כפתורים ---

        // לחיצה לבחירת תמונה
        btnUploadImage.setOnClickListener(v -> imagePicker.launch("image/*"));

        // לחיצה לבחירת קובץ (אפשר לשנות ל- "application/pdf" אם רוצים רק PDF)
        btnUploadFile.setOnClickListener(v -> filePicker.launch("*/*"));

        // לחיצה על "שלח"
        btnSubmit.setOnClickListener(v -> {
            String textContent = etLongText.getText().toString();

            if (textContent.isEmpty()) {
                etLongText.setError("אנא כתוב משהו");
                return;
            }

            // כאן תבצע את השמירה האמיתית (שרת/מסד נתונים)
            // לצורך הדוגמה נציג הודעה:
            Toast.makeText(this, "המידע נשמר! \nתמונה: " + (selectedImageUri != null) + "\nקובץ: " + (selectedFileUri != null), Toast.LENGTH_LONG).show();
        });
    }
}
