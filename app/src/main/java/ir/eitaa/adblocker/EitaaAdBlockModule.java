package ir.eitaa.adblocker;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;

/**
 * Modern LSPosed Module to disable advertisements in Eitaa (ir.eitaa.messenger).
 */
public class EitaaAdBlockModule extends XposedModule {

    private static final String TAG = "EitaaAdBlocker";
    private static final String TARGET_PACKAGE = "ir.eitaa.messenger";

    @Override
    public void onPackageReady(@NonNull PackageReadyParam param) {
        if (!TARGET_PACKAGE.equals(param.getPackageName())) {
            return;
        }

        log(Log.INFO, TAG, "EitaaAdBlocker active for " + param.getPackageName());
        ClassLoader cl = param.getClassLoader();

        // 1. Disable dialog ads, mini-app ads, sponsored messages, search ads in MessagesController
        hookMessagesController(cl);

        // 2. Disable channel floating ad button and top pinned ad banner in ChatActivity
        hookChatActivity(cl);

        // 3. Disable BotAdView (top channel/bot ad banner)
        hookBotAdView(cl);

        // 4. Disable media viewer video ads in VideoAds
        hookVideoAds(cl);

        // 5. Disable horizontal ads in SimpleAdsList (Trends, Members, Profile)
        hookSimpleAdsList(cl);

        // 6. Disable ad loaders in ProfileActivity, ChatUsersActivity, TrendsWebScreen
        hookOtherAdLoaders(cl);

        log(Log.INFO, TAG, "All Eitaa ad-blocking hooks registered successfully!");
    }

    private void hookMessagesController(ClassLoader cl) {
        try {
            Class<?> clazz = Class.forName("org.eitaa.messenger.MessagesController", false, cl);

            // 1. checkDialogAd(boolean) -> no-op
            hookNoOp(clazz, "checkDialogAd");

            // 2. checkMiniAppAd(boolean) -> no-op
            hookNoOp(clazz, "checkMiniAppAd");

            // 3. addPinnedAdToDialogs() -> no-op
            hookNoOp(clazz, "addPinnedAdToDialogs");

            // 4. addRandomAdToDialogs() -> no-op
            hookNoOp(clazz, "addRandomAdToDialogs");

            // 5. addMiniAppAd() -> no-op
            hookNoOp(clazz, "addMiniAppAd");

            // 6. getChannelPinAd(...) -> return -1L
            Method getChannelPinAd = findMethod(clazz, "getChannelPinAd");
            if (getChannelPinAd != null) {
                hook(getChannelPinAd).intercept(chain -> Long.valueOf(-1L));
                log(Log.INFO, TAG, "Hooked MessagesController.getChannelPinAd -> -1L");
            }

            // 7. internalChannelAd(...) -> return -1
            Method internalChannelAd = findMethod(clazz, "internalChannelAd");
            if (internalChannelAd != null) {
                hook(internalChannelAd).intercept(chain -> Integer.valueOf(-1));
                log(Log.INFO, TAG, "Hooked MessagesController.internalChannelAd -> -1");
            }

            // 8. searchAd(...) -> return false
            Method searchAd = findMethod(clazz, "searchAd");
            if (searchAd != null) {
                hook(searchAd).intercept(chain -> Boolean.FALSE);
                log(Log.INFO, TAG, "Hooked MessagesController.searchAd -> false");
            }

            // 9. getSponsoredMessages(long) -> return null
            Method getSponsoredMessages = findMethod(clazz, "getSponsoredMessages");
            if (getSponsoredMessages != null) {
                hook(getSponsoredMessages).intercept(chain -> null);
                log(Log.INFO, TAG, "Hooked MessagesController.getSponsoredMessages -> null");
            }

            // 10. isSponsoredDisabled() -> return true
            Method isSponsoredDisabled = findMethod(clazz, "isSponsoredDisabled");
            if (isSponsoredDisabled != null) {
                hook(isSponsoredDisabled).intercept(chain -> Boolean.TRUE);
                log(Log.INFO, TAG, "Hooked MessagesController.isSponsoredDisabled -> true");
            }
        } catch (Throwable t) {
            log(Log.WARN, TAG, "Failed to hook MessagesController: " + t.getMessage());
        }
    }

    private void hookChatActivity(ClassLoader cl) {
        try {
            Class<?> clazz = Class.forName("org.eitaa.ui.ChatActivity", false, cl);
            // setupAd(Context) -> no-op (removes floating circle ad button)
            hookNoOp(clazz, "setupAd");
            // loadPinnedAd() -> no-op (removes top pinned ad banner)
            hookNoOp(clazz, "loadPinnedAd");
        } catch (Throwable t) {
            log(Log.WARN, TAG, "Failed to hook ChatActivity: " + t.getMessage());
        }
    }

    private void hookBotAdView(ClassLoader cl) {
        try {
            Class<?> clazz = Class.forName("org.eitaa.ui.bots.BotAdView", false, cl);
            Method setMethod = findMethod(clazz, "set");
            if (setMethod != null) {
                hook(setMethod).intercept(chain -> {
                    Object thisObj = chain.getThisObject();
                    if (thisObj instanceof View) {
                        ((View) thisObj).setVisibility(View.GONE);
                    }
                    return null;
                });
                log(Log.INFO, TAG, "Hooked BotAdView.set -> GONE");
            }
        } catch (Throwable t) {
            log(Log.WARN, TAG, "Failed to hook BotAdView: " + t.getMessage());
        }
    }

    private void hookVideoAds(ClassLoader cl) {
        try {
            Class<?> clazz = Class.forName("org.eitaa.messenger.video.VideoAds", false, cl);
            hookNoOp(clazz, "load");
            hookNoOp(clazz, "show");
        } catch (Throwable t) {
            log(Log.WARN, TAG, "Failed to hook VideoAds: " + t.getMessage());
        }
    }

    private void hookSimpleAdsList(ClassLoader cl) {
        try {
            Class<?> clazz = Class.forName("org.eitaa.ui.Components.SimpleAdsList", false, cl);
            Method showAds = findMethod(clazz, "showAds");
            if (showAds != null) {
                hook(showAds).intercept(chain -> {
                    Object thisObj = chain.getThisObject();
                    if (thisObj instanceof View) {
                        ((View) thisObj).setVisibility(View.GONE);
                    }
                    return null;
                });
                log(Log.INFO, TAG, "Hooked SimpleAdsList.showAds -> GONE");
            }
        } catch (Throwable t) {
            log(Log.WARN, TAG, "Failed to hook SimpleAdsList: " + t.getMessage());
        }
    }

    private void hookOtherAdLoaders(ClassLoader cl) {
        try {
            Class<?> clazz = Class.forName("org.eitaa.ui.ProfileActivity", false, cl);
            hookNoOp(clazz, "loadAds");
        } catch (Throwable ignored) {}

        try {
            Class<?> clazz = Class.forName("org.eitaa.ui.ChatUsersActivity", false, cl);
            hookNoOp(clazz, "loadAds");
        } catch (Throwable ignored) {}

        try {
            Class<?> clazz = Class.forName("org.eitaa.ui.webContainer.screens.TrendsWebScreen", false, cl);
            hookNoOp(clazz, "loadAds");
        } catch (Throwable ignored) {}
    }

    private void hookNoOp(Class<?> clazz, String methodName) {
        Method m = findMethod(clazz, methodName);
        if (m != null) {
            hook(m).intercept(chain -> null);
            log(Log.INFO, TAG, "Hooked " + clazz.getSimpleName() + "." + methodName + " -> no-op");
        }
    }

    private Method findMethod(Class<?> clazz, String name) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                m.setAccessible(true);
                return m;
            }
        }
        return null;
    }
}
