package com.fablegram.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.text.*;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout root;
    int theme = 0;
    int currentChat = -1;
    boolean english = false;

    String[] names = {
        "FableGram Support",
        "Alex",
        "Друзья",
        "Новости FableGram",
        "Иван"
    };

    String[] previews = {
        "Добро пожаловать в FableGram!",
        "Привет!",
        "Заходите в чат",
        "Новости приложения",
        "Как дела?"
    };

    HashMap<Integer, String> wallpapers = new HashMap<>();
    HashMap<Integer, ArrayList<String>> messages = new HashMap<>();

    int[] builtInColors = {
        0xFFE8EAF6,
        0xFFD7F5E8,
        0xFFFFE8D6,
        0xFFE1F0FF
    };

    int bg, fg, accent, secondary;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        SharedPreferences p = getSharedPreferences("fable", MODE_PRIVATE);
        theme = p.getInt("theme", 0);
        english = p.getBoolean("english", false);

        for (int i = 0; i < names.length; i++) {
            messages.put(i, new ArrayList<String>());
            messages.get(i).add(previews[i]);
        }

        colors();
        showHome();
    }

    void save() {
        getSharedPreferences("fable", MODE_PRIVATE)
            .edit()
            .putInt("theme", theme)
            .putBoolean("english", english)
            .apply();
    }

    String tr(String ru, String en) {
        return english ? en : ru;
    }

    void colors() {
        if (theme == 1) {
            bg = 0xFF101318;
            fg = 0xFFFFFFFF;
            secondary = 0xFF252B34;
            accent = 0xFF6C9EFF;
        } else {
            bg = 0xFFF5F6FA;
            fg = 0xFF17191F;
            secondary = 0xFFFFFFFF;
            accent = 0xFF5688E8;
        }
    }

    int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density);
    }

    GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }

    TextView button(String value, Runnable action) {
        TextView t = text(value, 16, fg);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(14), dp(12), dp(14), dp(12));
        t.setBackground(shape(secondary, 16));
        t.setOnClickListener(v -> action.run());
        return t;
    }

    LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    void base() {
        colors();
        root = column();
        root.setBackgroundColor(bg);
        setContentView(root);
    }

    void toolbar(String title, boolean back) {
        LinearLayout bar = row();
        bar.setPadding(dp(14), dp(12), dp(14), dp(12));

        if (back) {
            TextView b = text("‹", 32, fg);
            b.setPadding(dp(8), 0, dp(18), 0);
            b.setOnClickListener(v -> showHome());
            bar.addView(b);
        }

        TextView name = text(title, 22, fg);
        name.setTypeface(null, Typeface.BOLD);

        bar.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(bar);
    }

    void showHome() {
        currentChat = -1;
        base();

        toolbar("FableGram", false);

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setHint(tr("Поиск", "Search"));
        search.setPadding(dp(16), 0, dp(16), 0);
        search.setBackground(shape(secondary, 18));

        LinearLayout.LayoutParams sp =
            new LinearLayout.LayoutParams(-1, dp(48));
        sp.setMargins(dp(14), dp(5), dp(14), dp(12));
        root.addView(search, sp);

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = column();

        for (int i = 0; i < names.length; i++) {
            final int index = i;

            LinearLayout item = row();
            item.setPadding(dp(14), dp(13), dp(14), dp(13));

            TextView avatar = text(
                names[i].substring(0, 1).toUpperCase(),
                23, Color.WHITE
            );
            avatar.setGravity(Gravity.CENTER);
            avatar.setBackground(shape(accent, 50));

            LinearLayout.LayoutParams ap =
                new LinearLayout.LayoutParams(dp(50), dp(50));
            ap.setMargins(0, 0, dp(13), 0);
            item.addView(avatar, ap);

            LinearLayout info = column();

            TextView n = text(names[i], 17, fg);
            n.setTypeface(null, Typeface.BOLD);

            TextView preview = text(previews[i], 13, 0xFF888888);

            info.addView(n);
            info.addView(preview);

            item.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
            item.setOnClickListener(v -> showChat(index));
            list.addView(item);
        }

        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout bottom = row();
        bottom.setPadding(dp(8), dp(10), dp(8), dp(10));
        bottom.setBackgroundColor(secondary);

        String[] tabs = {
            tr("Чаты", "Chats"),
            tr("Звонки", "Calls"),
            tr("Контакты", "Contacts"),
            tr("Настройки", "Settings")
        };

        for (int i = 0; i < tabs.length; i++) {
            final int index = i;
            TextView tab = text(tabs[i], 12, fg);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(dp(4), dp(8), dp(4), dp(8));

            bottom.addView(tab, new LinearLayout.LayoutParams(0, -2, 1));

            tab.setOnClickListener(v -> {
                if (index == 3) {
                    showSettings();
                } else {
                    Toast.makeText(
                        this,
                        tr("Раздел пока в разработке",
                           "Section is under development"),
                        Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }

        root.addView(bottom);
    }

    void showChat(int id) {
        currentChat = id;
        base();

        toolbar(names[id], true);

        FrameLayout frame = new FrameLayout(this);

        ImageView wallpaper = new ImageView(this);
        wallpaper.setScaleType(ImageView.ScaleType.CENTER_CROP);
        frame.addView(wallpaper, new FrameLayout.LayoutParams(-1, -1));

        applyWallpaper(wallpaper, id);

        ScrollView scroll = new ScrollView(this);
        LinearLayout messageList = column();
        messageList.setPadding(dp(12), dp(12), dp(12), dp(12));

        ArrayList<String> chatMessages = messages.get(id);

        for (String message : chatMessages) {
            TextView bubble = text(message, 16, fg);
            bubble.setPadding(dp(13), dp(10), dp(13), dp(10));
            bubble.setBackground(shape(secondary, 16));

            LinearLayout.LayoutParams mp =
                new LinearLayout.LayoutParams(-2, -2);
            mp.setMargins(0, dp(5), 0, dp(5));

            messageList.addView(bubble, mp);
        }

        scroll.addView(messageList);
        frame.addView(scroll, new FrameLayout.LayoutParams(-1, -1));

        root.addView(frame, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout composer = row();
        composer.setPadding(dp(8), dp(8), dp(8), dp(8));
        composer.setBackgroundColor(secondary);

        TextView wallpaperButton = text("▧", 24, accent);
        wallpaperButton.setGravity(Gravity.CENTER);
        wallpaperButton.setPadding(dp(8), dp(8), dp(8), dp(8));
        wallpaperButton.setOnClickListener(v -> wallpaperMenu());
        composer.addView(wallpaperButton);

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(16);
        input.setHint(tr("Сообщение...", "Message..."));
        input.setTextColor(fg);
        input.setHintTextColor(0xFF888888);
        input.setPadding(dp(12), 0, dp(12), 0);
        input.setBackground(shape(bg, 24));

        composer.addView(input, new LinearLayout.LayoutParams(0, dp(46), 1));

        TextView send = text("➤", 23, accent);
        send.setGravity(Gravity.CENTER);
        send.setPadding(dp(12), dp(8), dp(12), dp(8));

        send.setOnClickListener(v -> {
            String value = input.getText().toString().trim();

            if (value.isEmpty()) return;

            messages.get(id).add(value);
            input.setText("");
            showChat(id);
        });

        composer.addView(send);
        root.addView(composer);

        input.setOnEditorActionListener((v, actionId, event) -> {
            send.performClick();
            return true;
        });

        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    void applyWallpaper(ImageView image, int id) {
        String value = wallpapers.get(id);

        if (value == null) {
            image.setBackgroundColor(
                theme == 1 ? 0xFF202630 : 0xFFE8EDF5
            );
            return;
        }

        if (value.startsWith("color:")) {
            try {
                image.setBackgroundColor(
                    Color.parseColor(value.substring(6))
                );
            } catch (Exception e) {
                image.setBackgroundColor(bg);
            }
            return;
        }

        try {
            image.setImageURI(Uri.parse(value));
        } catch (Exception e) {
            image.setBackgroundColor(bg);
        }
    }

    void wallpaperMenu() {
        String[] options = {
            tr("Выбрать из галереи", "Choose from gallery"),
            tr("Встроенные обои", "Built-in wallpapers"),
            tr("Убрать обои", "Remove wallpaper")
        };

        new AlertDialog.Builder(this)
            .setTitle(tr("Обои чата", "Chat wallpaper"))
            .setItems(options, (d, which) -> {
                if (which == 0) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("image/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, 100);
                } else if (which == 1) {
                    chooseBuiltInWallpaper();
                } else {
                    wallpapers.remove(currentChat);
                    showChat(currentChat);
                }
            })
            .show();
    }

    void chooseBuiltInWallpaper() {
        String[] options = {
            tr("Светло-голубые", "Light blue"),
            tr("Мятные", "Mint"),
            tr("Персиковые", "Peach"),
            tr("Голубые", "Blue")
        };

        new AlertDialog.Builder(this)
            .setTitle(tr("Выбери фон", "Choose background"))
            .setItems(options, (d, which) -> {
                int color = builtInColors[which];
                wallpapers.put(
                    currentChat,
                    "color:#" + Integer.toHexString(color)
                );
                showChat(currentChat);
            })
            .show();
    }

    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 &&
            resultCode == RESULT_OK &&
            data != null) {

            Uri uri = data.getData();

            try {
                getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (Exception ignored) {}

            wallpapers.put(currentChat, uri.toString());
            showChat(currentChat);
        }
    }

    void showSettings() {
        base();
        toolbar(tr("Настройки", "Settings"), true);

        TextView themeTitle = text(
            tr("Оформление", "Appearance"), 18, fg
        );
        themeTitle.setPadding(dp(16), dp(20), dp(16), dp(8));
        root.addView(themeTitle);

        root.addView(button(
            theme == 1
                ? tr("Тёмная тема ✓", "Dark theme ✓")
                : tr("Светлая тема ✓", "Light theme ✓"),
            () -> {
                theme = theme == 1 ? 0 : 1;
                save();
                showSettings();
            }
        ));

        TextView languageTitle = text(
            tr("Язык", "Language"), 18, fg
        );
        languageTitle.setPadding(dp(16), dp(20), dp(16), dp(8));
        root.addView(languageTitle);

        root.addView(button(
            english ? "English" : "Русский",
            () -> {
                english = !english;
                save();
                showSettings();
            }
        ));

        TextView about = text("FableGram\nVersion 1.0", 15, 0xFF888888);
        about.setPadding(dp(16), dp(30), dp(16), dp(10));
        root.addView(about);
    }
            }
