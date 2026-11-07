package io.github.yunyizea.xsofprivacy;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

/** Launcher and LSPosed settings entry. Intents never change preferences. */
public final class SettingsActivity extends Activity {
    private Switch detailedLogging;
    private Switch hideLauncherIcon;
    /** 程序化回填开关时置位，避免把 UI 同步当成用户操作再写一次组件状态。 */
    private boolean syncing;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_settings);
        android.view.View root = findViewById(R.id.settings_root);
        int padding = getResources().getDimensionPixelSize(R.dimen.settings_padding);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(padding + insets.getSystemWindowInsetLeft(),
                    padding + insets.getSystemWindowInsetTop(),
                    padding + insets.getSystemWindowInsetRight(),
                    padding + insets.getSystemWindowInsetBottom());
            return insets;
        });
        root.requestApplyInsets();
        detailedLogging = findViewById(R.id.detailed_logging);
        detailedLogging.setChecked(LoggingSettings.readLocal(this));
        detailedLogging.setOnCheckedChangeListener((button, enabled) ->
                LoggingSettings.preferences(this).edit()
                        .putBoolean(LoggingSettings.ENABLED, enabled).apply());

        hideLauncherIcon = findViewById(R.id.hide_launcher_icon);
        hideLauncherIcon.setChecked(LauncherIcon.isHidden(this));
        hideLauncherIcon.setOnCheckedChangeListener((button, hidden) -> {
            if (syncing) return;
            if (LauncherIcon.setHidden(this, hidden)) {
                Toast.makeText(this,
                        hidden ? "桌面图标已隐藏，可从 LSPosed 管理器重新打开本页"
                                : "桌面图标已恢复",
                        Toast.LENGTH_SHORT).show();
            } else {
                // 写入失败就把开关拨回去，别让 UI 和系统状态对不上
                syncing = true;
                button.setChecked(!hidden);
                syncing = false;
                Toast.makeText(this, "修改桌面图标失败，请重试", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        detailedLogging.setChecked(LoggingSettings.readLocal(this));
        // 用户可能在 adb 或 LSPosed 里改过组件状态，回到本页以系统状态为准
        syncing = true;
        hideLauncherIcon.setChecked(LauncherIcon.isHidden(this));
        syncing = false;
    }
}
