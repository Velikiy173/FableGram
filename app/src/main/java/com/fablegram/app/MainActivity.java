package com.fablegram.app;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(1);
        layout.setPadding(32, 48, 32, 32);
        layout.setBackgroundColor(Color.rgb(245,247,255));

        TextView title = new TextView(this);
        title.setText("FableGram");
        title.setTextSize(32);
        title.setTextColor(Color.rgb(40,70,180));
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Твой новый мессенджер");
        subtitle.setTextSize(16);
        layout.addView(subtitle);

        Button login = new Button(this);
        login.setText("Войти");
        layout.addView(login);

        Button register = new Button(this);
        register.setText("Регистрация");
        layout.addView(register);

        setContentView(layout);
    }
}
