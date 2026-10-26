package com.adityak.leveler;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/** Opens the leveler panel on the remote's TV button and drives it while it is showing. */
public class RemoteKeyService extends AccessibilityService {
    // Shortly after boot the TV's audio hardware is still coming up; attaching the effect then is risky.
    private static final long BOOT_SETTLE_MS = 120_000;
    private static final long BOOT_DELAY_MS = 20_000;

    private LevelerPanel panel;

    @Override
    protected void onServiceConnected() {
        panel = new LevelerPanel(this);
        // The system rebinds this service at boot, so it doubles as the boot hook.
        if (Presets.load(this) == Presets.OFF) return;
        long delay = SystemClock.elapsedRealtime() < BOOT_SETTLE_MS ? BOOT_DELAY_MS : 0;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            int preset = Presets.load(this);
            if (preset != Presets.OFF) LevelerService.select(this, preset);
        }, delay);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        boolean up = event.getAction() == KeyEvent.ACTION_UP;

        if (!panel.isShowing()) {
            if (code != KeyEvent.KEYCODE_TV) return false;
            if (up) panel.show(Presets.load(this));
            return true;
        }

        switch (code) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (down) {
                    int step = code == KeyEvent.KEYCODE_DPAD_LEFT ? -1 : 1;
                    int preset = Math.max(Presets.OFF,
                            Math.min(Presets.STRONG, Presets.load(this) + step));
                    if (preset != Presets.load(this)) LevelerService.select(this, preset);
                    panel.show(preset);
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_TV:
                if (up) panel.hide();
                return true;
            default:
                // Any other button (Home, Netflix, etc.) closes the panel and works as usual.
                panel.hide();
                return false;
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {
        if (panel != null) panel.hide();
    }
}
