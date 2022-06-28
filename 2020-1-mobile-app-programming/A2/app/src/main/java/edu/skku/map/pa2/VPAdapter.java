package edu.skku.map.pa2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class VPAdapter extends FragmentPagerAdapter{
    int mNumOfTabs;
    String id_val;

    public VPAdapter(@NonNull FragmentManager fm, int numOfTabs, String id_val) {
        super(fm);
        this.mNumOfTabs = numOfTabs;
        this.id_val = id_val;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                fragment1 tab1 = new fragment1();
                return tab1;
            case 1:
                fragment2 tab2 = new fragment2();
                Bundle bundle = new Bundle(1);
                bundle.putString("id_val", id_val);
                tab2.setArguments(bundle);
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
