package com.nadavsprung.hackathon;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SubjectPagerAdapter extends FragmentStateAdapter {

    private String subject;
    private SubjectChatFragment chatFragment;
    private SubjectSummariesFragment summariesFragment;
    private SubjectQAFragment qaFragment;

    public SubjectPagerAdapter(@NonNull FragmentActivity fa, String subject) {
        super(fa);
        this.subject = subject;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("subject", subject);
        
        switch (position) {
            case 0:
                if (chatFragment == null) {
                    chatFragment = new SubjectChatFragment();
                    chatFragment.setArguments(bundle);
                }
                return chatFragment;
            case 1:
                if (summariesFragment == null) {
                    summariesFragment = new SubjectSummariesFragment();
                    summariesFragment.setArguments(bundle);
                }
                return summariesFragment;
            case 2:
                if (qaFragment == null) {
                    qaFragment = new SubjectQAFragment();
                    qaFragment.setArguments(bundle);
                }
                return qaFragment;
            default:
                Fragment fragment = new SubjectChatFragment();
                fragment.setArguments(bundle);
                return fragment;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
