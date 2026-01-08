package com.nadavsprung.hackathon;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {

    private SecondFragment secondFragment;
    private ThirdFragment thirdFragment;
    private FourthFragment fourthFragment;

    public MainPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new FirstFragment();
            case 1: 
                if (secondFragment == null) {
                    secondFragment = new SecondFragment();
                }
                return secondFragment;
            case 2:
                if (thirdFragment == null) {
                    thirdFragment = new ThirdFragment();
                }
                return thirdFragment;
            case 3:
                if (fourthFragment == null) {
                    fourthFragment = new FourthFragment();
                }
                return fourthFragment;
            default: return new FirstFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}

