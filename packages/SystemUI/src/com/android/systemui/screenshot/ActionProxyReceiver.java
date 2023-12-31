/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.screenshot;

import static com.android.systemui.screenshot.ScreenshotController.ACTION_TYPE_EDIT;
import static com.android.systemui.screenshot.ScreenshotController.ACTION_TYPE_SHARE;
import static com.android.systemui.screenshot.ScreenshotController.ACTION_TYPE_VIEW;
import static com.android.systemui.screenshot.ScreenshotController.EXTRA_ACTION_INTENT;
import static com.android.systemui.screenshot.ScreenshotController.EXTRA_DISALLOW_ENTER_PIP;
import static com.android.systemui.screenshot.ScreenshotController.EXTRA_ID;
import static com.android.systemui.screenshot.ScreenshotController.EXTRA_SMART_ACTIONS_ENABLED;
import static com.android.systemui.statusbar.phone.CentralSurfaces.SYSTEM_DIALOG_REASON_SCREENSHOT;

import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.RemoteAnimationAdapter;
import android.view.WindowManagerGlobal;

import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.settings.DisplayTracker;
import com.android.systemui.shared.system.ActivityManagerWrapper;

import javax.inject.Inject;

/**
 * Receiver to proxy the share or edit intent, used to clean up the notification and send
 * appropriate signals to the system (ie. to dismiss the keyguard if necessary).
 */
public class ActionProxyReceiver extends BroadcastReceiver {
    private static final String TAG = "ActionProxyReceiver";

    private final ActivityManagerWrapper mActivityManagerWrapper;
    private final ScreenshotSmartActions mScreenshotSmartActions;
    private final DisplayTracker mDisplayTracker;
    private final ActivityStarter mActivityStarter;

    @Inject
    public ActionProxyReceiver(ActivityManagerWrapper activityManagerWrapper,
            ScreenshotSmartActions screenshotSmartActions,
            DisplayTracker displayTracker,
            ActivityStarter activityStarter) {
        mActivityManagerWrapper = activityManagerWrapper;
        mScreenshotSmartActions = screenshotSmartActions;
        mDisplayTracker = displayTracker;
        mActivityStarter = activityStarter;
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        Runnable startActivityRunnable = () -> {
            mActivityManagerWrapper.closeSystemWindows(SYSTEM_DIALOG_REASON_SCREENSHOT);

            PendingIntent actionIntent = intent.getParcelableExtra(EXTRA_ACTION_INTENT);
            ActivityOptions opts = ActivityOptions.makeBasic();
            opts.setDisallowEnterPictureInPictureWhileLaunching(
                    intent.getBooleanExtra(EXTRA_DISALLOW_ENTER_PIP, false));
            opts.setPendingIntentBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED);
            try {
                actionIntent.send(context, 0, null, null, null, null, opts.toBundle());
                if (intent.getBooleanExtra(ScreenshotController.EXTRA_OVERRIDE_TRANSITION, false)) {
                    RemoteAnimationAdapter runner = new RemoteAnimationAdapter(
                            ScreenshotController.SCREENSHOT_REMOTE_RUNNER, 0, 0);
                    try {
                        WindowManagerGlobal.getWindowManagerService()
                                .overridePendingAppTransitionRemote(runner,
                                        mDisplayTracker.getDefaultDisplayId());
                    } catch (Exception e) {
                        Log.e(TAG, "Error overriding screenshot app transition", e);
                    }
                }
            } catch (PendingIntent.CanceledException e) {
                Log.e(TAG, "Pending intent canceled", e);
            }

        };

        mActivityStarter.executeRunnableDismissingKeyguard(startActivityRunnable, null,
                true /* dismissShade */, true /* afterKeyguardGone */,
                true /* deferred */);

        if (intent.getBooleanExtra(EXTRA_SMART_ACTIONS_ENABLED, false)) {
            String action = intent.getAction();
            String actionType;
            if (Intent.ACTION_VIEW.equals(action)) {
                actionType = ACTION_TYPE_VIEW;
            } else if (Intent.ACTION_EDIT.equals(action)) {
                actionType = ACTION_TYPE_EDIT;
            } else {
                actionType = ACTION_TYPE_SHARE;
            }
            mScreenshotSmartActions.notifyScreenshotAction(
                    intent.getStringExtra(EXTRA_ID), actionType, false, null);
        }
    }
}
