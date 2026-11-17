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
        URL url = new URL("http://127.0.0.1:5000" + path);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(10000);
        c.setReadTimeout(10000);
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json");
        c.getOutputStream().write(data.toString().getBytes("UTF-8"));

        InputStream in = c.getResponseCode() < 400
                ? c.getInputStream() : c.getErrorStream();

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
                            status.setText("Тестовый код: " +
                                    result.optString("demo_code") +
                                    "\nЭто код только для FableGram Demo.");
                        } else {
                            status.setText("Аккаунт создан!\nID: " +
                                    result.optString("fg_id"));
                        }
                    } else {
                        status.setText(result.optString("error", "Ошибка"));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        status.setText("Ошибка соединения: " + e.getMessage()));
            }
        });
    }

    private EditText field(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextColor(Color.BLACK);
        e.setHintTextColor(Color.GRAY);
        e.setSingleLine(true);
        e.setInputType(type);
        return e;
    }

    private void add(LinearLayout layout, android.view.View view) {
        layout.addView(view, new LinearLayout.LayoutParams(
                -1, -2));
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 30, 24, 20);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("FableGram Demo");
        title.setTextSize(27);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        add(root, title);

        TextView info = new TextView(this);
        info.setText("Тестовая регистрация. Не является входом в Telegram.");
        info.setTextColor(Color.DKGRAY);
        info.setGravity(Gravity.CENTER);
        add(root, info);

        number = field("Номер DEMO-000001", 1);
        code = field("Тестовый код", 1);
        username = field("Имя пользователя", 1);
        password = field("Пароль", 129);

        add(root, number);
        add(root, code);
        add(root, username);
        add(root, password);

        Button request = new Button(this);
        request.setText("Получить тестовый код");
        add(root, request);

        Button register = new Button(this);
        register.setText("Создать аккаунт FableGram");
        add(root, register);

        status = new TextView(this);
        status.setTextColor(Color.BLACK);
        status.setTextSize(16);
        status.setPadding(0, 20, 0, 0);
        add(root, status);

        request.setOnClickListener(v -> {
            try {
                JSONObject data = new JSONObject();
                data.put("number", number.getText().toString().trim());
                runRequest("/api/demo/request", data, true);
            } catch (Exception e) {
                status.setText("Ошибка данных");
            }
        });

        register.setOnClickListener(v -> {
            try {
                JSONObject data = new JSONObject();
                data.put("number", number.getText().toString().trim());
                data.put("code", code.getText().toString().trim());
                data.put("username", username.getText().toString().trim());
                data.put("password", password.getText().toString());
                runRequest("/api/demo/register", data, false);
            } catch (Exception e) {
                status.setText("Ошибка данных");
            }
        });

        setContentView(root);
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
