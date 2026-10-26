package com.adityak.leveler;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Small non-focusable overlay, styled like the TV's quick settings, showing the current preset. */
final class LevelerPanel {
    private static final long AUTO_HIDE_MS = 5000;
    private static final int ACCENT = 0xFF7FD1AE;

    private final Context context;
    private final WindowManager wm;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable autoHide = this::hide;
    private final LinearLayout root;
    private final TextView[] chips = new TextView[Presets.NAMES.length];
    private boolean showing;

    LevelerPanel(Context context) {
        this.context = context;
        wm = context.getSystemService(WindowManager.class);

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(28), dp(24), dp(28), dp(22));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xE6202124);
        bg.setCornerRadius(dp(20));
        root.setBackground(bg);

        root.addView(text("Volume Leveler", 20, Color.WHITE, true));
        TextView sub = text("Evens out loud ads and quiet dialogue", 13, 0xFFB0B4BA, false);
        sub.setPadding(0, dp(4), 0, dp(18));
        root.addView(sub);

        LinearLayout row = new LinearLayout(context);
        for (int i = 0; i < chips.length; i++) {
            chips[i] = text(Presets.NAMES[i], 16, Color.WHITE, true);
            chips[i].setGravity(Gravity.CENTER);
            chips[i].setMinWidth(dp(96));
            chips[i].setPadding(dp(16), dp(10), dp(16), dp(10));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.leftMargin = dp(10);
            row.addView(chips[i], lp);
        }
        root.addView(row);

        TextView hint = text("◀ ▶  change     OK  done", 12, 0xFF8A8F96, false);
        hint.setPadding(0, dp(16), 0, 0);
        root.addView(hint);
    }

    boolean isShowing() {
        return showing;
    }

    void show(int preset) {
        for (int i = 0; i < chips.length; i++) {
            GradientDrawable d = new GradientDrawable();
            d.setCornerRadius(dp(22));
            if (i == preset) {
                d.setColor(ACCENT);
                chips[i].setTextColor(0xFF10261C);
            } else {
                d.setStroke(dp(1), 0x66FFFFFF);
                chips[i].setTextColor(Color.WHITE);
            }
            chips[i].setBackground(d);
        }

        if (!showing) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.END | Gravity.BOTTOM;
            lp.x = dp(48);
            lp.y = dp(48);
            wm.addView(root, lp);
            showing = true;
        }
        handler.removeCallbacks(autoHide);
        handler.postDelayed(autoHide, AUTO_HIDE_MS);
    }

    void hide() {
        handler.removeCallbacks(autoHide);
        if (showing) {
            wm.removeView(root);
            showing = false;
        }
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(context);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        return t;
    }

    private int dp(int v) {
        return Math.round(v * context.getResources().getDisplayMetrics().density);
    }
}
