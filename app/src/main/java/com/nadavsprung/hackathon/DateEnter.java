package com.nadavsprung.hackathon;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
        Button btn_pop_up_groups = findViewById(R.id.btn_pop_up_groups);
        btn_pop_up_groups.setOnClickListener(v -> showCodeDialog());
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

    private void showCodeDialog() {
        // 1. יצירת ה-Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. ניפוח (Inflate) העיצוב שיצרנו בשלב הקודם
        // חשוב מאוד: אנו משתמשים ב-popupView כדי למצוא רכיבים בתוכו
        View popupView = LayoutInflater.from(this).inflate(R.layout.pop_up_groups, null);

        // 3. מציאת הרכיבים בתוך העיצוב המותאם
        EditText etCode = popupView.findViewById(R.id.et_group_code);
        Button btnEnter = popupView.findViewById(R.id.btn_enter_code);

        // הגדרת ה-View לדיאלוג
        builder.setView(popupView);

        // יצירת הדיאלוג והצגתו
        AlertDialog dialog = builder.create();

        // (אופציונלי) עושה את הרקע שקוף כדי שהפינות המעוגלות יראו טוב, אם יש
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 4. מה קורה כשלוחצים על כפתור האנטר
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputCode = etCode.getText().toString().trim();
                String correctCode = "123456"; // הקוד הנכון (אפשר להביא ממסד נתונים)

                if (inputCode.equals(correctCode)) {
                    // הקוד נכון!
                    Toast.makeText(DateEnter.this, "קוד התקבל בהצלחה!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss(); // סגירת החלון

                    // כאן תוסיף את הקוד למעבר מסך או הצגת התוכן
                    // למשל: openGroupActivity();

                } else {
                    // הקוד שגוי
                    etCode.setError("קוד שגוי, נסה שוב");
                }
            }
        });

        // הצגת הדיאלוג בפועל
        dialog.show();
    }
}
