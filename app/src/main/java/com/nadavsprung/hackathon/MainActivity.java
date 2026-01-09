package com.nadavsprung.hackathon;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabLayoutMediator mediator;
    private MainPagerAdapter pagerAdapter;
    private FirebaseAuth mAuth;
    public static String selectedSubject = ""; // Shared subject variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();
            
            // Check if user is authenticated
            if (mAuth.getCurrentUser() == null) {
                // User not logged in, redirect to login
                Intent intent = new Intent(this, GoogleloginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }

            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            tabLayout = findViewById(R.id.tab_layout);
            viewPager = findViewById(R.id.view_pager);

            if (tabLayout == null || viewPager == null) {
                android.widget.Toast.makeText(this, "שגיאה בטעינת הממשק", android.widget.Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            try {
                pagerAdapter = new MainPagerAdapter(this);
                viewPager.setAdapter(pagerAdapter);
                viewPager.setOffscreenPageLimit(3);

                mediator = new TabLayoutMediator(tabLayout, viewPager,
                        new TabLayoutMediator.TabConfigurationStrategy() {
                            @Override
                            public void onConfigureTab(TabLayout.Tab tab, int position) {
                                switch (position) {
                                    case 0: tab.setText("נושאים"); break;
                                    case 1: tab.setText("העלה חומרים"); break;
                                    case 2: tab.setText("מבחנים"); break;
                                    case 3: tab.setText("לוח תוצאות"); break;
                                }
                                // אופציונלי: אייקונים
                                // if (position == 0) tab.setIcon(R.drawable.ic_home);
                            }
                        });
                mediator.attach();
            } catch (Exception e) {
                android.widget.Toast.makeText(this, "שגיאה בהגדרת הטאבים: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "שגיאה בטעינת המסך הראשי: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (mediator != null) mediator.detach();
        super.onDestroy();
    }
}
