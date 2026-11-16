package dev.liwatch.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Displays two independent range readings; accepts values as data and has no data source. */
public final class RangeView extends LinearLayout {
    private final RangeCell electric;
    private final RangeCell fuel;

    public RangeView(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setBaselineAligned(false);
        electric = new RangeCell(context, R.drawable.ic_electric, R.string.electric);
        fuel = new RangeCell(context, R.drawable.ic_fuel, R.string.fuel);
        addView(electric, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        LayoutParams second = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        second.setMarginStart(dp(context, 5));
        addView(fuel, second);
    }

    public void setRange(int electricKm, int fuelKm) {
        electric.setReading(electricKm);
        fuel.setReading(fuelKm);
    }

    static int dp(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    private static final class RangeCell extends LinearLayout {
        private final TextView reading;
        private final int label;

        RangeCell(Context context, int icon, int label) {
            super(context);
            this.label = label;
            setOrientation(VERTICAL);
            setPadding(dp(context, 5), dp(context, 9), dp(context, 5), dp(context, 8));
            setBackground(MainActivity.surface(context, 0xff1e2330, 17));
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);

            LinearLayout pair = new LinearLayout(context);
            pair.setOrientation(HORIZONTAL);
            pair.setGravity(Gravity.CENTER_VERTICAL);
            pair.setBaselineAligned(false);
            pair.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            ImageView image = new ImageView(context);
            image.setImageResource(icon);
            LayoutParams iconParams = new LayoutParams(dp(context, 13), dp(context, 13));
            iconParams.setMarginEnd(dp(context, 2));
            pair.addView(image, iconParams);
            reading = new TextView(context);
            reading.setGravity(Gravity.CENTER);
            reading.setMaxLines(1);
            reading.setHorizontallyScrolling(false);
            reading.setIncludeFontPadding(false);
            reading.setTextColor(Color.WHITE);
            reading.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            reading.setAutoSizeTextTypeUniformWithConfiguration(9, 22, 1, TypedValue.COMPLEX_UNIT_SP);
            pair.addView(reading, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
            addView(pair, new LayoutParams(LayoutParams.MATCH_PARENT, dp(context, 28)));

            TextView name = MainActivity.text(context, 12, 0xffb9c2d5, label);
            name.setGravity(Gravity.CENTER);
            name.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(name, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }

        void setReading(int km) {
            String value = Integer.toString(km);
            SpannableString text = new SpannableString(value + "\u00a0" + getResources().getString(R.string.distance_unit));
            text.setSpan(new RelativeSizeSpan(0.65f), value.length(), text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            reading.setText(text);
            setContentDescription(getResources().getString(R.string.range_description, getResources().getString(label), text));
        }
    }
}
