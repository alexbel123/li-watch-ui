package dev.liwatch.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LocaleManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.Locale;

/** Offline Wear OS sample. The launcher intent has no data inputs. */
public final class MainActivity extends Activity {
    private static final int MUTED = 0xffb9c2d5;
    private static final int ACCENT = 0xffb6caff;
    private static final String PREFERENCES = "ui_preferences";
    private static final String LANGUAGE = "language";

    @Override protected void attachBaseContext(Context context) {
        if (Build.VERSION.SDK_INT >= 33) {
            super.attachBaseContext(context);
            return;
        }
        String stored = context.getSharedPreferences(PREFERENCES, MODE_PRIVATE).getString(LANGUAGE, "");
        if ("ru".equals(stored) || "en".equals(stored)) {
            Configuration config = new Configuration(context.getResources().getConfiguration());
            config.setLocales(LocaleList.forLanguageTags(stored));
            context = context.createConfigurationContext(config);
        }
        super.attachBaseContext(context);
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        boolean round = getResources().getConfiguration().isScreenRound();
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setBackgroundColor(Color.BLACK);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int side = dp(round ? 18 : 16);
        int top = dp(round ? 24 : 20);
        int bottom = dp(round ? 34 : 20);
        content.setPadding(side, top, side, bottom);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets safe = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                content.setPadding(Math.max(side, safe.left), Math.max(top, safe.top),
                    Math.max(side, safe.right), Math.max(bottom, safe.bottom));
            }
            return insets;
        });
        scroll.addView(content);

        TextView badge = text(this, 10, ACCENT, R.string.demo);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(9), dp(3), dp(9), dp(3));
        badge.setBackground(surface(this, 0xff242b3f, 16));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(-2, -2);
        badgeParams.gravity = Gravity.CENTER_HORIZONTAL;
        content.addView(badge, badgeParams);

        TextView rangeTitle = text(this, 20, Color.WHITE, R.string.range);
        rangeTitle.setGravity(Gravity.CENTER);
        rangeTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        rangeTitle.setAccessibilityHeading(true);
        add(content, rangeTitle, 6);

        RangeView range = new RangeView(this);
        range.setRange(180, 740);
        add(content, range, 8);

        LinearLayout climate = new LinearLayout(this);
        climate.setGravity(Gravity.CENTER);
        climate.setPadding(dp(6), dp(8), dp(6), dp(8));
        climate.setBackground(surface(this, 0xff171a23, 21));
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_temperature);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(18), dp(18));
        iconParams.setMarginEnd(dp(7));
        climate.addView(icon, iconParams);
        LinearLayout cabin = new LinearLayout(this);
        cabin.setOrientation(LinearLayout.VERTICAL);
        TextView title = text(this, 11, MUTED, R.string.cabin);
        cabin.addView(title);
        TextView temperature = text(this, 17, Color.WHITE, 0);
        temperature.setText(getString(R.string.temperature, NumberFormat.getNumberInstance(locale()).format(21.5)));
        cabin.addView(temperature);
        climate.addView(cabin);
        add(content, climate, 7);

        Button language = new Button(this);
        language.setText(R.string.language);
        language.setTextSize(13);
        language.setAllCaps(false);
        language.setTextColor(ACCENT);
        language.setMinHeight(dp(48));
        language.setPadding(dp(5), dp(7), dp(5), dp(7));
        language.setBackground(new android.graphics.drawable.RippleDrawable(
            android.content.res.ColorStateList.valueOf(0x30ffffff), surface(this, 0xff242b3f, 25), null));
        language.setOnClickListener(v -> chooseLanguage());
        add(content, language, 9);

        TextView footer = text(this, 11, MUTED, R.string.demo_data);
        footer.setGravity(Gravity.CENTER);
        add(content, footer, 8);
        setContentView(scroll);
    }

    private void chooseLanguage() {
        boolean russian = "ru".equals(locale().getLanguage());
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(R.string.language)
            .setSingleChoiceItems(new String[]{getString(R.string.language_ru), getString(R.string.language_en)},
                russian ? 0 : 1, (dialog, choice) -> {
                    dialog.dismiss();
                    String tag = choice == 0 ? "ru" : "en";
                    if (Build.VERSION.SDK_INT >= 33) {
                        getSystemService(LocaleManager.class).setApplicationLocales(LocaleList.forLanguageTags(tag));
                    } else {
                        getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit().putString(LANGUAGE, tag).apply();
                        recreate();
                    }
                })
            .setNegativeButton(R.string.close, null).show();
    }

    private Locale locale() { return getResources().getConfiguration().getLocales().get(0); }
    private int dp(int value) { return RangeView.dp(this, value); }
    private void add(LinearLayout parent, View child, int margin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(margin);
        parent.addView(child, params);
    }
    static GradientDrawable surface(Context context, int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(RangeView.dp(context, radius));
        return drawable;
    }
    static TextView text(Context context, int size, int color, int resource) {
        TextView view = new TextView(context);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setIncludeFontPadding(false);
        if (resource != 0) view.setText(resource);
        return view;
    }
}
