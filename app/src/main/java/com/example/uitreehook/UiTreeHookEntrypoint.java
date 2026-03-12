package com.example.uitreehook;

import android.app.AndroidAppHelper;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class UiTreeHookEntrypoint implements IXposedHookZygoteInit {

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        GlobalInstance.modulePath = startupParam.modulePath;

        hookFrameworkViews();
        hookAccessibilityManager();
    }

    private void hookFrameworkViews() {
        // 1. Hook View 的 setImportantForAccessibility
        try {
            XposedHelpers.findAndHookMethod(View.class, "setImportantForAccessibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    int mode = (Integer) param.args[0];
                    // IMPORTANT_FOR_ACCESSIBILITY_NO = 2
                    // IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS = 4
                    if (mode == 2 || mode == 4) {
                        String currentPackage = AndroidAppHelper.currentPackageName();
                        
                        // 过滤掉系统核心进程或自身，防止全局 Hook 产生不可预期的影响
                        if ("android".equals(currentPackage) || "com.android.systemui".equals(currentPackage)) {
                            return; 
                        }

                        // 强制改为 IMPORTANT_FOR_ACCESSIBILITY_YES (1) 
                        param.args[0] = 1;
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("Failed to hook View.setImportantForAccessibility: " + t.getMessage());
        }

        // 2. Hook AccessibilityNodeInfo 的 setImportantForAccessibility
        try {
            XposedHelpers.findAndHookMethod(AccessibilityNodeInfo.class, "setImportantForAccessibility", boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            boolean isImportant = (Boolean) param.args[0];
                            if (!isImportant) {
                                // 强制改为 true
                                param.args[0] = true;
                            }
                        }
                    });
        } catch (Throwable t) {
            XposedBridge.log("Failed to hook AccessibilityNodeInfo.setImportantForAccessibility: " + t.getMessage());
        }
    }

    private void hookAccessibilityManager() {
        // 3. Hook AccessibilityManager.isEnabled to force it to return true
        try {
            XposedHelpers.findAndHookMethod(AccessibilityManager.class, "isEnabled", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(true);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("Failed to hook AccessibilityManager.isEnabled: " + t.getMessage());
        }

        // 4. Hook AccessibilityManager.isTouchExplorationEnabled to force it to return true
        try {
            XposedHelpers.findAndHookMethod(AccessibilityManager.class, "isTouchExplorationEnabled",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(true);
                        }
                    });
        } catch (Throwable t) {
            XposedBridge.log("Failed to hook AccessibilityManager.isTouchExplorationEnabled: " + t.getMessage());
        }
    }
}