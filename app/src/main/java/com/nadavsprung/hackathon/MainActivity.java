package com.nadavsprung.hackathon;

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

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabLayoutMediator mediator;
    private MainPagerAdapter pagerAdapter;
    public static String selectedSubject = ""; // Shared subject variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);

        mediator = new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0: tab.setText("נושאים"); break;
                            case 1: tab.setText("הכנה למבחן"); break;
                            case 2: tab.setText("מבחנים"); break;
                            case 3: tab.setText("סטטיסטיקות"); break;
                        }
                        // אופציונלי: אייקונים
                        // if (position == 0) tab.setIcon(R.drawable.ic_home);
                    }
                });
        mediator.attach();
    }

    public void navigateToChatbot(String subject) {
        // Store subject and switch to SecondFragment (index 1)
        selectedSubject = subject;
        viewPager.setCurrentItem(1, true);
        
        // Update the fragment if it exists
        if (pagerAdapter != null) {
            SecondFragment secondFragment = pagerAdapter.getSecondFragment();
            if (secondFragment != null) {
                secondFragment.setSubject(subject);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mediator != null) mediator.detach();
        super.onDestroy();
    }
}
