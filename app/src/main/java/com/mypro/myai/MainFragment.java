package com.mypro.myai;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainFragment extends Fragment {
    public static final String TAG = "MainFragment";
    private AutoCompleteTextView acJenisKelamin;
    private TextInputEditText etUmur, etKolesterol, etSistol, etDiastol, etBMI, etHeartRate, etGlukosa;
    private Button btnSubmit;
    private NestedScrollView scrollView;
    private String modelPath = "model_prediksi.tflite";
    private Interpreter tflite;
    private Handler uiHandler;
    private ExecutorService executorService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Inisialisasi views
        scrollView = view.findViewById(R.id.scroll_view);
        acJenisKelamin = view.findViewById(R.id.ac_jenisKelamin);
        etUmur = view.findViewById(R.id.et_umur);
        etKolesterol = view.findViewById(R.id.et_kolesterol);
        etSistol = view.findViewById(R.id.et_sistol);
        etDiastol = view.findViewById(R.id.et_diastol);
        etBMI = view.findViewById(R.id.et_bmi);
        etHeartRate = view.findViewById(R.id.et_heart_rate);
        etGlukosa = view.findViewById(R.id.et_glukosa);
        btnSubmit = view.findViewById(R.id.btn_simpan);

        initDropdownValue();

        try {
            //Inisialisasi tensorflow
            tflite = new Interpreter(loadModelFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Inisialisasi untuk Background Thread
        executorService = Executors.newSingleThreadExecutor();
        //Inisialisasi UI Handler untuk menerima respon dari Background Thread
        uiHandler = new Handler(Looper.getMainLooper());

        btnSubmit.setOnClickListener(v -> {
            if (isValidInput()) {
                //Mendapatkan input user dan mengubahnya ke bentuk numerik (float)
                float gender = acJenisKelamin.getText().toString().equals("Laki-laki") ? 1 : 0;
                float age = Float.parseFloat(etUmur.getText().toString());
                float cholesterol = Float.parseFloat(etKolesterol.getText().toString());
                float systolic = Float.parseFloat(etSistol.getText().toString());
                float diastolic = Float.parseFloat(etDiastol.getText().toString());
                float bmi = Float.parseFloat(etBMI.getText().toString());
                float heartRate = Float.parseFloat(etHeartRate.getText().toString());
                float glucose = Float.parseFloat(etGlukosa.getText().toString());

                // Menjalankan model di Background Thread
                executorService.execute(() -> {
                    //Mulai prediksi dan mendapatkan hasilnya
                    float result = doInference(gender, age, cholesterol, systolic, diastolic, bmi, heartRate, glucose);

                    // Perbarui UI dari Thread
                    uiHandler.post(() -> {
                        clearForm();
                        view.clearFocus();
                        scrollView.fullScroll(NestedScrollView.FOCUS_UP);

                        MainActivity mainActivity = ((MainActivity) requireActivity());
                        mainActivity.updateToolbarTitle("Result");

                        //Mengirimkan data result ke ResultFragment
                        ResultFragment resultFragment = new ResultFragment();
                        Bundle arguments = new Bundle();
                        arguments.putFloat( ResultFragment.RESULT , result);
                        resultFragment.setArguments(arguments);

                        //Menampilkan resultFragment
                        mainActivity.showFragment(resultFragment, ResultFragment.TAG);
                    });
                });
            }
        });

    }

    //Method untuk membuka model tflite
    private MappedByteBuffer loadModelFile() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(requireActivity().getAssets().openFd(modelPath).getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = requireActivity().getAssets().openFd(modelPath).getStartOffset();
        long declaredLength = requireActivity().getAssets().openFd(modelPath).getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    //Method untuk memprediksi data input
    private float doInference(float gender, float age, float cholesterol, float systolic, float diastolic, float bmi, float heartRate, float glucose) {
        //Inisialisasi input shape
        float[][] input = new float[1][8];
        //Memasukkan input data
        input[0][0] = gender;
        input[0][1] = age;
        input[0][2] = cholesterol;
        input[0][3] = systolic;
        input[0][4] = diastolic;
        input[0][5] = bmi;
        input[0][6] = heartRate;
        input[0][7] = glucose;

        //Initialize output shape
        float[][] output = new float[1][1];
        //Menjalankan prediksi
        tflite.run(input, output);
        //Return hasil
        return output[0][0];
    }

    //Method untuk inisialisasi dropdown pada jenis kelamin
    private void initDropdownValue() {
        String[] genders = {"Laki-laki", "Perempuan"};
        acJenisKelamin.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, genders));
    }

    //Method for memvalidasi input pengguna agar terisi semua
    private boolean isValidInput() {
        if (acJenisKelamin.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Data Jenis Kelamin harus diisi", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etUmur.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Data Umur harus diisi", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etKolesterol.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Data Total Kolesterol harus diisi", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etSistol.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Data Sistol harus diisi", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etDiastol.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Data Diastol harus diisi", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etBMI.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Data BMI harus diisi", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etHeartRate.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Data Total Denyut Jantung harus diisi", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etGlukosa.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Data Total Glukosa harus diisi", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //Method untuk menghapus semua input pada editText
    public void clearForm(){
        acJenisKelamin.setText("");
        etUmur.setText("");
        etKolesterol.setText("");
        etSistol.setText("");
        etDiastol.setText("");
        etBMI.setText("");
        etHeartRate.setText("");
        etGlukosa.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Tutup tugas yang sedang berjalan untuk mencegah adanya kebocoran memori
        if (tflite != null) {
            tflite.close();
        }
        executorService.shutdown();
    }
}