/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.systemui.statusbar.phone.fragment.dagger;

import android.view.View;

import com.android.systemui.R;
import com.android.systemui.battery.BatteryMeterView;
import com.android.systemui.dagger.qualifiers.RootView;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.PhoneStatusBarView;
import com.android.systemui.statusbar.phone.PhoneStatusBarViewController;
import com.android.systemui.statusbar.phone.fragment.CollapsedStatusBarFragment;
import com.android.systemui.statusbar.policy.Clock;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/** Dagger module for {@link StatusBarFragmentComponent}. */
@Module
public interface StatusBarFragmentModule {

    String LIGHTS_OUT_NOTIF_VIEW = "lights_out_notif_view";
    String OPERATOR_NAME_VIEW = "operator_name_view";

    /** */
    @Provides
    @RootView
    @StatusBarFragmentScope
    static PhoneStatusBarView providePhoneStatusBarView(
            CollapsedStatusBarFragment collapsedStatusBarFragment) {
        return (PhoneStatusBarView) collapsedStatusBarFragment.getView();
    }

    /** */
    @Provides
    @StatusBarFragmentScope
    static BatteryMeterView provideBatteryMeterView(@RootView PhoneStatusBarView view) {
        return view.findViewById(R.id.battery);
    }

    /** */
    @Provides
    @StatusBarFragmentScope
    @Named(LIGHTS_OUT_NOTIF_VIEW)
    static View provideLightsOutNotifView(@RootView PhoneStatusBarView view) {
        return view.findViewById(R.id.notification_lights_out);
    }

    /** */
    @Provides
    @StatusBarFragmentScope
    @Named(OPERATOR_NAME_VIEW)
    static View provideOperatorNameView(@RootView PhoneStatusBarView view) {
        return view.findViewById(R.id.operator_name);
    }

    /** */
    @Provides
    @StatusBarFragmentScope
    static Clock provideClock(@RootView PhoneStatusBarView view) {
        return view.findViewById(R.id.clock);
    }

    /** */
    @Provides
    @StatusBarFragmentScope
    static PhoneStatusBarViewController providePhoneStatusBarViewController(
            PhoneStatusBarViewController.Factory phoneStatusBarViewControllerFactory,
            @RootView PhoneStatusBarView phoneStatusBarView,
            NotificationPanelViewController notificationPanelViewController) {
        return phoneStatusBarViewControllerFactory.create(
                phoneStatusBarView,
                notificationPanelViewController.getStatusBarTouchEventHandler());
    }
}
