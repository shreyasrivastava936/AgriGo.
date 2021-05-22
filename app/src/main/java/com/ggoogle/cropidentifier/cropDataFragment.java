package com.ggoogle.cropidentifier;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;



public class cropDataFragment extends Fragment {

    View retView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        retView = inflater.inflate(R.layout.cropdatalayout, container, false);

        return retView;
    }
}







