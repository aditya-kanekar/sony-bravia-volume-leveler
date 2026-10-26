package com.adityak.leveler;

import android.content.Context;
import android.media.audiofx.DynamicsProcessing;
import android.media.audiofx.DynamicsProcessing.Limiter;
import android.media.audiofx.DynamicsProcessing.MbcBand;

/** Leveling presets: a single full-band compressor followed by a safety limiter. */
final class Presets {
    static final int OFF = 0;
    static final int LIGHT = 1;
    static final int STRONG = 2;

    static final String[] NAMES = {"Off", "Light", "Strong"};

    private static final String PREFS = "leveler";
    private static final String KEY_PRESET = "preset";

    private Presets() {}

    static int load(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_PRESET, OFF);
    }

    static void save(Context c, int preset) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_PRESET, preset).apply();
    }

    static DynamicsProcessing.Config config(int preset) {
        // threshold dB, ratio, makeup gain dB, limiter ceiling dB
        float threshold, ratio, makeup, ceiling;
        if (preset == STRONG) {
            threshold = -38f; ratio = 5f; makeup = 12f; ceiling = -6f;
        } else {
            threshold = -26f; ratio = 2.5f; makeup = 5f; ceiling = -3f;
        }

        // Sony's implementation rejects VARIANT_FAVOR_TIME_RESOLUTION.
        DynamicsProcessing.Config.Builder b = new DynamicsProcessing.Config.Builder(
                DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                2,            // channels
                false, 0,     // pre-EQ
                true, 1,      // multi-band compressor, one band covering everything
                false, 0,     // post-EQ
                true);        // limiter
        DynamicsProcessing.Config cfg = b.build();

        cfg.setMbcBandAllChannelsTo(0, new MbcBand(
                true, 20000f,   // in use, cutoff
                15f, 400f,      // attack ms, release ms
                ratio, threshold,
                8f,             // knee width dB
                -90f, 1f,       // noise gate threshold, expander ratio (disabled)
                0f, makeup));   // pre-gain, post-gain

        cfg.setLimiterAllChannelsTo(new Limiter(
                true, true, 0,  // in use, enabled, link group
                1f, 80f,        // attack ms, release ms
                10f, ceiling,   // ratio, threshold
                0f));           // post-gain
        return cfg;
    }
}
