package com.nadavsprung.hackathon;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class SubjectDetailActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabLayoutMediator mediator;
    private SubjectPagerAdapter pagerAdapter;
    private String selectedSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subject_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectedSubject = getIntent().getStringExtra("subject");
        if (selectedSubject == null || selectedSubject.isEmpty()) {
            selectedSubject = "נושא";
        }

        setTitle(selectedSubject);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        pagerAdapter = new SubjectPagerAdapter(this, selectedSubject);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(2);

        mediator = new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0: tab.setText("AI Chat"); break;
                            case 1: tab.setText("סיכומים"); break;
                            case 2: tab.setText("שאלות ותשובות"); break;
                        }
                    }
                });
        mediator.attach();
    }

    @Override
    protected void onDestroy() {
        if (mediator != null) mediator.detach();
        super.onDestroy();
    }
}
