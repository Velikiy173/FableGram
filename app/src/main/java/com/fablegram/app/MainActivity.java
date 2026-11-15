
package com.fablegram.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.net.Uri;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.text.*;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout root, content;
    int theme = 0;
    int currentChat = -1;

    String[] names = {
        "FableGram Support",
        "Alex",
        "Друзья",
        "Новости FableGram",
        "Иван"
    };

    String[] messages = {
        "Добро пожаловать!",
        "Привет 👋",
        "Как дела?",
        "Обновление доступно",
        "Увидимся!"
    };

    String[] emojis = {"🛡️", "👤", "👥", "⭐", "🙂"};

    HashMap<Integer, String> wallpapers = new HashMap<>();

    int bg, fg, secondary, accent;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        theme = getPreferences(0).getInt("theme", 0);

        for (int i = 0; i < names.length; i++) {
            String saved = getPreferences(0).getString("wall_" + i, "");
            if (!saved.isEmpty()) wallpapers.put(i, saved);
        }

        showHome();
    }

    void colors() {
        if (theme == 1) {
            bg = Color.rgb(24, 25, 28);
            fg = Color.WHITE;
            secondary = Color.LTGRAY;
            accent = Color.rgb(65, 145, 230);
        } else if (theme == 2) {
            bg = Color.rgb(235, 242, 255);
            fg = Color.rgb(30, 65, 130);
            secondary = Color.rgb(90, 110, 145);
            accent = Color.rgb(70, 115, 220);
        } else {
            bg = Color.WHITE;
            fg = Color.rgb(25, 35, 45);
            secondary = Color.GRAY;
            accent = Color.rgb(45, 145, 220);
        }
    }

    TextView text(String s, int size, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }

    GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    void base() {
        colors();
        root = new LinearLayout(this);
        root.setOrientation(1);
        root.setBackgroundColor(bg);
        setContentView(root);
    }

    void toolbar(String title, boolean back) {
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(16, 12, 16, 12);
        bar.setBackgroundColor(accent);

        if (back) {
            TextView b = text("‹", 34, Color.WHITE);
            bar.addView(b);
            b.setPadding(0, 0, 18, 0);
            b.setOnClickListener(v -> showHome());
        }

        TextView name = text(title, 21, Color.WHITE);
        name.setTypeface(null, Typeface.BOLD);
        bar.addView(name, new LinearLayout.LayoutParams(0, -2, 1));

        root.addView(bar);
    }

    void showHome() {
        currentChat = -1;
        base();
        toolbar("FableGram", false);

        LinearLayout actions = new LinearLayout(this);
        actions.setPadding(12, 10, 12, 10);
        actions.setGravity(Gravity.CENTER_VERTICAL);

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setHint("Поиск");
        search.setBackground(shape(theme == 1 ?
            Color.rgb(45, 46, 50) : Color.rgb(240, 243, 247), 30));
        search.setPadding(16, 8, 16, 8);
        actions.addView(search, new LinearLayout.LayoutParams(0, 48, 1));

        TextView mode = text(" ◐ ", 25, fg);
        actions.addView(mode);
        mode.setOnClickListener(v -> {
            theme = (theme + 1) % 3;
            getPreferences(0).edit().putInt("theme", theme).apply();
            showHome();
        });

        root.addView(actions);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(1);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        for (int i = 0; i < names.length; i++) {
            final int id = i;

            LinearLayout item = new LinearLayout(this);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(14, 12, 14, 12);

            TextView avatar = text(emojis[i], 25, fg);
            avatar.setGravity(Gravity.CENTER);
            avatar.setBackground(shape(theme == 1 ?
                Color.rgb(55, 65, 85) : Color.rgb(225, 238, 255), 60));
            item.addView(avatar, new LinearLayout.LayoutParams(52, 52));

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(1);
            info.setPadding(13, 0, 0, 0);

            TextView n = text(names[i], 17, fg);
            n.setTypeface(null, Typeface.BOLD);
            info.addView(n);

            TextView m = text(messages[i], 14, secondary);
            info.addView(m);

            item.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

            content.addView(item);
            View line = new View(this);
            line.setBackgroundColor(theme == 1 ?
                Color.rgb(55, 55, 60) : Color.rgb(235, 235, 235));
            content.addView(line, new LinearLayout.LayoutParams(-1, 1));

            item.setOnClickListener(v -> showChat(id));
        }

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER);
        bottom.setPadding(8, 12, 8, 12);
        bottom.setBackgroundColor(bg);

        String[] tabs = {"Чаты", "Контакты", "Звонки", "Настройки"};
        for (String tab : tabs) {
            TextView t = text(tab, 13, accent);
            t.setGravity(Gravity.CENTER);
            bottom.addView(t, new LinearLayout.LayoutParams(0, 42, 1));
            t.setOnClickListener(v ->
                Toast.makeText(this, tab, Toast.LENGTH_SHORT).show());
        }

        root.addView(bottom);
    }

    void showChat(int id) {
        currentChat = id;
        base();
        toolbar(names[id], true);

        FrameLayout frame = new FrameLayout(this);
        root.addView(frame, new LinearLayout.LayoutParams(-1, 0, 1));

        ImageView wallpaper = new ImageView(this);
        wallpaper.setScaleType(ImageView.ScaleType.CENTER_CROP);

        String uri = wallpapers.get(id);
        if (uri != null) {
            try {
                wallpaper.setImageURI(Uri.parse(uri));
            } catch (Exception ignored) {}
        } else {
            wallpaper.setBackgroundColor(theme == 1 ?
                Color.rgb(35, 38, 43) : Color.rgb(225, 237, 245));
        }

        frame.addView(wallpaper, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout messages = new LinearLayout(this);
        messages.setOrientation(1);
        messages.setPadding(12, 18, 12, 12);

        TextView welcome = text(messagesFor(id), 16, fg);
        welcome.setPadding(14, 12, 14, 12);
        welcome.setBackground(shape(theme == 1 ?
            Color.rgb(55, 58, 65) : Color.WHITE, 18));
        messages.addView(welcome);

        FrameLayout.LayoutParams mp = new FrameLayout.LayoutParams(
            -1, -1);
        frame.addView(messages, mp);

        LinearLayout composer = new LinearLayout(this);
        composer.setGravity(Gravity.CENTER_VERTICAL);
        composer.setPadding(8, 8, 8, 8);
        composer.setBackgroundColor(bg);

        Button photo = new Button(this);
        photo.setText("Обои");
        composer.addView(photo);
        photo.setOnClickListener(v -> chooseWallpaper());

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Сообщение...");
        composer.addView(input, new LinearLayout.LayoutParams(0, 52, 1));

        Button send = new Button(this);
        send.setText("➤");
        composer.addView(send);

        send.setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                TextView bubble = text(value, 16, fg);
                bubble.setPadding(14, 12, 14, 12);
                bubble.setBackground(shape(theme == 1 ?
                    Color.rgb(55, 85, 115) : Color.rgb(205, 240, 215), 18));
                messages.addView(bubble);
                input.setText("");
            }
        });

        root.addView(composer);
    }

    String messagesFor(int id) {
        return "Чат: " + names[id] +
            "\n\nЭто демонстрационный экран FableGram.";
    }

    void chooseWallpaper() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK &&
            data != null && currentChat >= 0) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {}

            wallpapers.put(currentChat, uri.toString());
            getPreferences(0).edit()
                .putString("wall_" + currentChat, uri.toString())
                .apply();

            showChat(currentChat);
        }
    }
}

