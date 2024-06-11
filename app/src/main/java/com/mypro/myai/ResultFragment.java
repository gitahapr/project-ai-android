package com.mypro.myai;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class ResultFragment extends Fragment {
    public static final String TAG = "ResultFragment";

    public static final String RESULT = "result";

    private TextView tvResult;
    private Button btnBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout untuk fragment ini
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvResult = view.findViewById(R.id.tv_result);
        btnBack = view.findViewById(R.id.btn_kembali);

        //Mendapatkan data result dari MainFragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            float result = arguments.getFloat(RESULT);
            if (result > 0.5) {
                tvResult.setText("TINGGI");
            } else {
                tvResult.setText("RENDAH");
            }
        }

        btnBack.setOnClickListener(v -> {
            //Tutup fragment yang dijalankan
            getParentFragmentManager().popBackStack();
            ((MainActivity) requireActivity()).updateToolbarTitle(getString(R.string.app_name));
        });
    }
}