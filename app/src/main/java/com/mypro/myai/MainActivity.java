package com.mypro.myai;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showFragment(new MainFragment(), MainFragment.TAG);

        setBackPressCallback();
    }

    //Method untuk menampilkan fragmen dalam aktivitas
    public void showFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }

    public void updateToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    //Method untuk menangani tombol kembali
    private void setBackPressCallback() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //periksa apakah ada fragmen di backstack
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    //Tutup fragmen saat ini
                    getSupportFragmentManager().popBackStack();

                    updateToolbarTitle(getString(R.string.app_name));
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}