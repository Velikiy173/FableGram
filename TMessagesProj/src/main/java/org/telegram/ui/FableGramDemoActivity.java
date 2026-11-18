package org.telegram.ui;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FableGramDemoActivity extends Activity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private EditText number, code, username, password;
    private TextView status;

    private JSONObject post(String path, JSONObject data) throws Exception {
        URL url = new URL("http://127.0.0.1:5001" + path);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(10000);
        c.setReadTimeout(10000);
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = c.getOutputStream()) {
            os.write(data.toString().getBytes("UTF-8"));
        }

        int response = c.getResponseCode();
        InputStream in = response < 400 ? c.getInputStream() : c.getErrorStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int n;

        while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
        in.close();
        c.disconnect();
        return new JSONObject(out.toString("UTF-8"));
    }

    private void runRequest(String path, JSONObject data, boolean showCode) {
        status.setText("Подключение...");
        executor.execute(() -> {
            try {
                JSONObject result = post(path, data);
                runOnUiThread(() -> {
                    if (result.optBoolean("success")) {
                        if (showCode) {
                            status.setText("Запрос обработан.\nПроверь Demo-бота: там будет тестовый код.");
                        } else {
                            status.setText("Аккаунт FableGram создан!\nID: " + result.optString("fg_id"));
                        }
                    } else {
                        status.setText(result.optString("error", "Ошибка"));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> status.setText("Ошибка соединения: " + e.getMessage()));
            }
        });
    }

    private EditText field(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(type);
        e.setSingleLine(true);
        e.setTextSize(16);
        return e;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 40, 28, 24);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("FableGram");
        title.setTextSize(30);
        title.setTextColor(Color.rgb(30, 136, 229));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView info = new TextView(this);
        info.setText("Демо-регистрация\nЭто отдельный тестовый аккаунт FableGram, не вход в Telegram.");
        info.setTextSize(15);
        info.setTextColor(Color.DKGRAY);
        info.setGravity(Gravity.CENTER);
        root.addView(info);

        number = field("Номер DEMO-000001", 1);
        code = field("Тестовый код из Demo-бота", 1);
        username = field("Имя пользователя", 1);
        password = field("Пароль (минимум 6 символов)", 129);

        root.addView(number);
        root.addView(code);
        root.addView(username);
        root.addView(password);

        Button request = new Button(this);
        request.setText("Получить тестовый код");
        root.addView(request);

        Button register = new Button(this);
        register.setText("Создать аккаунт FableGram");
        root.addView(register);

        status = new TextView(this);
        status.setTextColor(Color.DKGRAY);
        status.setTextSize(15);
        status.setGravity(Gravity.CENTER);
        root.addView(status);

        request.setOnClickListener(v -> {
            try {
                JSONObject j = new JSONObject();
                j.put("number", number.getText().toString().trim());
                runRequest("/api/demo/request", j, true);
            } catch (Exception e) {
                status.setText("Ошибка данных");
            }
        });

        register.setOnClickListener(v -> {
            try {
                JSONObject j = new JSONObject();
                j.put("number", number.getText().toString().trim());
                j.put("code", code.getText().toString().trim());
                j.put("username", username.getText().toString().trim());
                j.put("password", password.getText().toString());
                runRequest("/api/demo/register", j, false);
            } catch (Exception e) {
                status.setText("Ошибка данных");
            }
        });

        setContentView(root);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
