package com.example.madqaq;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ResultsActivity extends AppCompatActivity {
    
    private ListView lvSuccess, lvFailed;
    private MaterialButton btnSaveSuccess, btnSaveFailed, btnCopySuccess, btnCopyFailed, btnRetryFailed;
    private TextView tvSuccessCount, tvFailedCount;
    private TabLayout tabLayout;
    private ArrayAdapter<String> successAdapter, failedAdapter;
    private List<String> successResults = new ArrayList<>();
    private List<String> failedResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        
        initializeViews();
        loadResults();
        setupListeners();
    }
    
    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        lvSuccess = findViewById(R.id.lvSuccess);
        lvFailed = findViewById(R.id.lvFailed);
        btnSaveSuccess = findViewById(R.id.btnSaveSuccess);
        btnSaveFailed = findViewById(R.id.btnSaveFailed);
        btnCopySuccess = findViewById(R.id.btnCopySuccess);
        btnCopyFailed = findViewById(R.id.btnCopyFailed);
        btnRetryFailed = findViewById(R.id.btnRetryFailed);
        tvSuccessCount = findViewById(R.id.tvSuccessCount);
        tvFailedCount = findViewById(R.id.tvFailedCount);
        tabLayout = findViewById(R.id.tabLayout);
        
        successAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, successResults);
        failedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, failedResults);
        lvSuccess.setAdapter(successAdapter);
        lvFailed.setAdapter(failedAdapter);
    }
    
    private void loadResults() {
        // Load from SharedPreferences or pass via Intent
        successResults.add("test@email.com:password123 - ✅ نجاح");
        failedResults.add("failed@email.com:wrongpass - ❌ فشل");
        
        tvSuccessCount.setText("النجاح: " + successResults.size());
        tvFailedCount.setText("الفشل: " + failedResults.size());
    }
    
    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                lvSuccess.setVisibility(tab.getPosition() == 0 ? android.view.View.VISIBLE : android.view.View.GONE);
                lvFailed.setVisibility(tab.getPosition() == 1 ? android.view.View.VISIBLE : android.view.View.GONE);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        
        btnSaveSuccess.setOnClickListener(v -> saveToFile(successResults, "success"));
        btnSaveFailed.setOnClickListener(v -> saveToFile(failedResults, "failed"));
        btnCopySuccess.setOnClickListener(v -> copyToClipboard(successResults));
        btnCopyFailed.setOnClickListener(v -> copyToClipboard(failedResults));
        btnRetryFailed.setOnClickListener(v -> retryFailed());
    }
    
    private void saveToFile(List<String> results, String type) {
        try {
            String fileName = "Madqaq_" + type + "_" + 
                    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".txt";
            
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);
            
            FileWriter writer = new FileWriter(file);
            for (String line : results) {
                writer.write(line + "\n");
            }
            writer.close();
            
            Toast.makeText(this, "تم حفظ الملف في: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "خطأ في حفظ الملف: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void copyToClipboard(List<String> results) {
        StringBuilder sb = new StringBuilder();
        for (String line : results) {
            sb.append(line).append("\n");
        }
        
        android.content.ClipboardManager clipboard = 
                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("results", sb.toString());
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(this, "تم نسخ " + results.size() + " نتيجة", Toast.LENGTH_SHORT).show();
    }
    
    private void retryFailed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putStringArrayListExtra("retry_list", (ArrayList<String>) failedResults);
        startActivity(intent);
        Toast.makeText(this, "جاري إعادة محاولة العناصر الفاشلة", Toast.LENGTH_SHORT).show();
    }
}
