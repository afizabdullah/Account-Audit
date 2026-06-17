package com.example.madqaq;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import okhttp3.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etLoginUrl, etDelay;
    private MaterialButton btnTestConnection, btnSelectFile, btnStart, btnPause, btnResume, btnStop;
    private Button btnResults, btnAbout, btnWebDashboard;
    private TextView tvFileName, tvFileSize, tvLineCount, tvStatus, tvProgress;
    private LinearProgressIndicator progressBar;
    private WebView webViewLog;
    private MaterialCardView cardFileInfo;
    private Switch switchAutoScroll;

    private Uri selectedFileUri;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private SharedPreferences prefs;
    private OkHttpClient client;
    private List<String> successResults = new ArrayList<>();
    private int currentLine = 0;
    private int totalLines = 0;
    private StringBuilder logBuilder = new StringBuilder();

    private static final int FILE_SELECT_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("MadqaqPrefs", MODE_PRIVATE);
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        initializeViews();
        setupClickListeners();
        setupWebView();
    }

    private void initializeViews() {
        etLoginUrl = findViewById(R.id.etLoginUrl);
        etDelay = findViewById(R.id.etDelay);
        btnTestConnection = findViewById(R.id.btnTestConnection);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnStart = findViewById(R.id.btnStart);
        btnPause = findViewById(R.id.btnPause);
        btnResume = findViewById(R.id.btnResume);
        btnStop = findViewById(R.id.btnStop);
        btnResults = findViewById(R.id.btnResults);
        btnAbout = findViewById(R.id.btnAbout);
        btnWebDashboard = findViewById(R.id.btnWebDashboard);
        tvFileName = findViewById(R.id.tvFileName);
        tvFileSize = findViewById(R.id.tvFileSize);
        tvLineCount = findViewById(R.id.tvLineCount);
        tvStatus = findViewById(R.id.tvStatus);
        tvProgress = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);
        webViewLog = findViewById(R.id.webViewLog);
        cardFileInfo = findViewById(R.id.cardFileInfo);
        switchAutoScroll = findViewById(R.id.switchAutoScroll);
    }

    private void setupClickListeners() {
        btnStart.setOnClickListener(v -> startChecking());
    }

    private void setupWebView() {
        webViewLog.getSettings().setJavaScriptEnabled(true);
        webViewLog.setWebViewClient(new WebViewClient());
        updateWebView();
    }

    private void updateWebView() {
        String html = "<html><head><style>" +
                "body { background: #1a1a2e; color: #00ff00; font-family: monospace; padding: 10px; }" +
                ".log-line { margin: 2px 0; padding: 4px; border-bottom: 1px solid #333; }" +
                ".success { color: #00ff00; } .failed { color: #ff4444; } .info { color: #4488ff; }" +
                "</style></head><body>" + logBuilder.toString() + "</body></html>";
        webViewLog.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    // هذه هي الدالة المعدلة التي تحل مشكلة الانهيار (Crash)
    private void addLog(final String message, final String type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String color = type.equals("success") ? "success" : type.equals("failed") ? "failed" : "info";
                logBuilder.append("<div class='log-line ").append(color).append("'>")
                        .append(getCurrentTime()).append(" - ").append(message).append("</div>");
                
                updateWebView();

                if (switchAutoScroll != null && switchAutoScroll.isChecked()) {
                    webViewLog.loadUrl("javascript:window.scrollTo(0, document.body.scrollHeight);");
                }
            }
        });
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private void startChecking() {
        // يمكنك هنا استدعاء العمليات الخاصة بك
    }
}
