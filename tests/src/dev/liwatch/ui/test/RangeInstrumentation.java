package dev.liwatch.ui.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import dev.liwatch.ui.RangeView;

/** Measure real Android views at round-watch widths and verify the installed manifest. */
public final class RangeInstrumentation extends Instrumentation {
    @Override public void onCreate(Bundle arguments) { super.onCreate(arguments); start(); }

    @Override public void onStart() {
        Bundle result = new Bundle();
        Context target = getTargetContext();
        try {
            int flags = PackageManager.GET_PERMISSIONS | PackageManager.GET_ACTIVITIES
                | PackageManager.GET_SERVICES | PackageManager.GET_RECEIVERS | PackageManager.GET_PROVIDERS;
            PackageInfo pkg = target.getPackageManager().getPackageInfo(target.getPackageName(), flags);
            require(pkg.requestedPermissions == null || pkg.requestedPermissions.length == 0, "App requests permissions");
            require(pkg.services == null && pkg.receivers == null && pkg.providers == null, "Unexpected component");
            require(pkg.activities.length == 1 && pkg.activities[0].exported, "Unexpected launcher configuration");

            int[] count = {0};
            Throwable[] failure = {null};
            runOnMainSync(() -> {
                try {
                    for (int width : new int[]{400, 450}) for (String lang : new String[]{"ru", "en"})
                        for (float scale : new float[]{1f, 1.3f}) {
                            Configuration config = new Configuration(target.getResources().getConfiguration());
                            config.setLocales(LocaleList.forLanguageTags(lang));
                            config.screenLayout = (config.screenLayout & ~Configuration.SCREENLAYOUT_ROUND_MASK)
                                | Configuration.SCREENLAYOUT_ROUND_YES;
                            config.densityDpi = 340;
                            config.fontScale = scale;
                            Context context = target.createConfigurationContext(config);
                            int side = Math.round(18 * context.getResources().getDisplayMetrics().density);
                            int available = width - 2 * side;
                            int[] readings = {0, 80, 740, 1000, 9999};
                            for (int value : readings) {
                                RangeView range = new RangeView(context);
                                range.setRange(value, value);
                                range.measure(View.MeasureSpec.makeMeasureSpec(available, View.MeasureSpec.EXACTLY),
                                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                                range.layout(0, 0, available, range.getMeasuredHeight());
                                for (int index = 0; index < 2; index++) {
                                    LinearLayout cell = (LinearLayout) range.getChildAt(index);
                                    LinearLayout pair = (LinearLayout) cell.getChildAt(0);
                                    View icon = pair.getChildAt(0);
                                    TextView text = (TextView) pair.getChildAt(1);
                                    require(text.getText().toString().equals(value + "\u00a0" + (lang.equals("ru") ? "км" : "km")), "Wrong range unit");
                                    require(text.getLineCount() == 1 && text.getLayout().getEllipsisCount(0) == 0, "Range wraps or truncates");
                                    require(text.getLayout().getLineWidth(0) <= text.getWidth() + 0.5f,
                                        "Range clips at " + width + "px, font " + scale + ", " + lang + ": " + value);
                                    require(Math.abs(icon.getTop() + icon.getHeight() / 2f - pair.getHeight() / 2f) <= 1f,
                                        "Icon is not vertically centered");
                                }
                                count[0]++;
                            }
                        }
                } catch (Throwable err) { failure[0] = err; }
            });
            if (failure[0] != null) throw failure[0];
            result.putString("stream", "PASS: " + count[0] + " round layouts in RU/EN; no permissions or background components\n");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable error) {
            result.putString("stream", "FAIL: " + error.getClass().getSimpleName() + ": " + error.getMessage() + "\n");
            finish(Activity.RESULT_CANCELED, result);
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
