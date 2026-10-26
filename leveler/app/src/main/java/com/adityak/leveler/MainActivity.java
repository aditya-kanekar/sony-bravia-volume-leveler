package com.adityak.leveler;

import android.app.Activity;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView statusView;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(96, 64, 96, 64);

        TextView title = new TextView(this);
        title.setText("Volume Leveler");
        title.setTextSize(34);
        root.addView(title);

        statusView = new TextView(this);
        statusView.setTextSize(18);
        statusView.setPadding(0, 24, 0, 40);
        root.addView(statusView);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        for (int i = 0; i < Presets.NAMES.length; i++) {
            final int preset = i;
            Button b = new Button(this);
            b.setText(Presets.NAMES[i]);
            b.setTextSize(22);
            b.setMinWidth(260);
            b.setOnClickListener(v -> select(preset));
            row.addView(b);
            if (i == Presets.load(this)) b.post(b::requestFocus);
        }
        root.addView(row);

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void select(int preset) {
        LevelerService.select(this, preset);
        handler.postDelayed(this::refresh, 500);
    }

    private void refresh() {
        boolean supported = false;
        for (AudioEffect.Descriptor d : AudioEffect.queryEffects()) {
            if (AudioEffect.EFFECT_TYPE_DYNAMICS_PROCESSING.equals(d.type)) supported = true;
        }
        statusView.setText("Preset: " + Presets.NAMES[Presets.load(this)]
                + "\n" + LevelerService.status
                + "\nDynamics processing on this TV: " + (supported ? "yes" : "NOT FOUND"));
    }
}
