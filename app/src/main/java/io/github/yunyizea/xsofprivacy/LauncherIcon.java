package io.github.yunyizea.xsofprivacy;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * 桌面图标显隐控制。
 *
 * 桌面入口由 manifest 里的 activity-alias LauncherEntry 承载，所以「隐藏图标」只是
 * 把该 alias 置为 disabled：SettingsActivity 本身仍带 MODULE_SETTINGS 入口，依然能
 * 从 LSPosed 管理器打开设置页把图标恢复回来，不会把自己锁在外面。
 *
 * 状态以系统的组件状态为唯一真相源（不落 SharedPreferences）：组件启用状态由
 * PackageManagerService 持久化，清 App 数据也不会丢，用 adb 改过之后回到设置页
 * 显示的也是桌面实际表现。
 */
final class LauncherIcon {

    /** 与 AndroidManifest.xml 中 activity-alias 的 name 保持一致。 */
    private static final String ALIAS_CLASS =
            "io.github.yunyizea.xsofprivacy.LauncherEntry";

    private LauncherIcon() {}

    private static ComponentName alias(Context ctx) {
        return new ComponentName(ctx.getPackageName(), ALIAS_CLASS);
    }

    static boolean isHidden(Context ctx) {
        try {
            int state = ctx.getPackageManager().getComponentEnabledSetting(alias(ctx));
            return state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 切换桌面图标显隐。
     *
     * DONT_KILL_APP 是必需的：改组件状态默认会连带杀掉本进程，设置页会当场闪退。
     *
     * @return true 表示系统已接受新的组件状态；false 表示写入失败，调用方应回滚 UI。
     */
    static boolean setHidden(Context ctx, boolean hidden) {
        try {
            int state = hidden
                    ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            ctx.getPackageManager().setComponentEnabledSetting(
                    alias(ctx), state, PackageManager.DONT_KILL_APP);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
