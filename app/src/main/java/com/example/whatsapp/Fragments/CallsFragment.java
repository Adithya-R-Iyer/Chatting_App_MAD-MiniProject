package com.example.whatsapp.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.whatsapp.R;
import com.example.whatsapp.databinding.FragmentCallsBinding;

public class CallsFragment extends Fragment {

    FragmentCallsBinding binding;

    public CallsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding =  FragmentCallsBinding.inflate(inflater, container, false);



        return binding.getRoot();
    }
}