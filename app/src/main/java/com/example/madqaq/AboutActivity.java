package com.example.madqaq;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        TextView tvAppName = findViewById(R.id.tvAppName);
        TextView tvVersion = findViewById(R.id.tvVersion);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvDeveloper = findViewById(R.id.tvDeveloper);
        
        tvAppName.setText("مدقق بيانات");
        tvVersion.setText("الإصدار: 1.0");
        tvDescription.setText("تطبيق لفحص حسابات تسجيل الدخول عبر إرسال ملفات TXT إلى روابط محددة، مع عرض النتائج بشكل مباشر.");
        tvDeveloper.setText("المطور: حافظ العزي");
    }
}
