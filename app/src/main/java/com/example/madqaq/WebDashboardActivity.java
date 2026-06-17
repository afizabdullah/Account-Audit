package com.example.madqaq;

import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.text.SimpleDateFormat;
import java.util.*;

public class WebDashboardActivity extends AppCompatActivity {
    
    private WebView webView;
    private Handler handler = new Handler();
    private Runnable updateRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_dashboard);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        
        startAutoUpdate();
    }
    
    private void startAutoUpdate() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateDashboard();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateRunnable);
    }
    
    private void updateDashboard() {
        String html = generateDashboardHtml();
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }
    
    private String generateDashboardHtml() {
        return "<!DOCTYPE html><html><head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                "body { background: #1a1a2e; color: #ffffff; font-family: 'Segoe UI', Tahoma, sans-serif; padding: 20px; }" +
                ".header { text-align: center; padding: 20px; background: linear-gradient(135deg, #0f3460, #16213e); border-radius: 15px; margin-bottom: 20px; }" +
                ".header h1 { color: #00ff00; font-size: 24px; }" +
                ".header p { color: #888888; font-size: 14px; margin-top: 5px; }" +
                ".stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 15px; margin-bottom: 20px; }" +
                ".stat-card { background: #16213e; border-radius: 12px; padding: 20px; text-align: center; border: 1px solid #0f3460; }" +
                ".stat-card .value { font-size: 28px; font-weight: bold; margin-bottom: 5px; }" +
                ".stat-card .label { font-size: 12px; color: #888888; text-transform: uppercase; }" +
                ".success { color: #00ff00; } .failed { color: #ff4444; } .info { color: #4488ff; } .warning { color: #ff9800; }" +
                ".progress-container { background: #16213e; border-radius: 12px; padding: 20px; margin-bottom: 20px; }" +
                ".progress-bar { height: 20px; background: #333333; border-radius: 10px; overflow: hidden; margin-top: 10px; }" +
                ".progress-fill { height: 100%; background: linear-gradient(90deg, #00ff00, #4488ff); border-radius: 10px; transition: width 0.5s; }" +
                ".log-section { background: #16213e; border-radius: 12px; padding: 20px; }" +
                ".log-section h3 { color: #00ff00; margin-bottom: 15px; }" +
                ".log-entry { padding: 8px; border-bottom: 1px solid #333333; font-family: monospace; font-size: 12px; }" +
                ".log-entry:last-child { border-bottom: none; }" +
                "</style></head><body>" +
                "<div class='header'>" +
                "<h1>📊 لوحة التحكم</h1>" +
                "<p>" + getCurrentDateTime() + "</p>" +
                "</div>" +
                "<div class='stats-grid'>" +
                "<div class='stat-card'><div class='value info'>75%</div><div class='label'>نسبة الإنجاز</div></div>" +
                "<div class='stat-card'><div class='value info'>150</div><div class='label'>إجمالي العمليات</div></div>" +
                "<div class='stat-card'><div class='value success'>120</div><div class='label'>ناجحة</div></div>" +
                "<div class='stat-card'><div class='value failed'>30</div><div class='label'>فاشلة</div></div>" +
                "<div class='stat-card'><div class='value info'>00:05:23</div><div class='label'>الوقت المنقضي</div></div>" +
                "<div class='stat-card'><div class='value warning'>00:01:45</div><div class='label'>الوقت المتبقي</div></div>" +
                "</div>" +
                "<div class='progress-container'>" +
                "<h3>📈 تقدم العملية</h3>" +
                "<div class='progress-bar'><div class='progress-fill' style='width: 75%'></div></div>" +
                "<p style='margin-top: 10px; color: #888888;'>120 من 150 حساب</p>" +
                "</div>" +
                "<div class='log-section'>" +
                "<h3>📝 سجل العمليات المباشر</h3>" +
                "<div class='log-entry success'>✅ نجاح - test@email.com</div>" +
                "<div class='log-entry failed'>❌ فشل - failed@email.com (401)</div>" +
                "<div class='log-entry info'>ℹ️ جاري معالجة: user@email.com</div>" +
                "<div class='log-entry warning'>⚠️ تأخير في الاتصال</div>" +
                "</div>" +
                "</body></html>";
    }
    
    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }
}
