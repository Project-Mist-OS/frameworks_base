/**
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.bluetooth;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.testing.AndroidTestingRunner;
import android.testing.TestableLooper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.filters.SmallTest;

import com.android.internal.logging.UiEventLogger;
import com.android.settingslib.bluetooth.LocalBluetoothLeBroadcast;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastSender;
import com.android.systemui.SysuiTestCase;
import com.android.systemui.media.dialog.MediaOutputDialogFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

@SmallTest
@RunWith(AndroidTestingRunner.class)
@TestableLooper.RunWithLooper(setAsMainLooper = true)
public class BroadcastDialogTest extends SysuiTestCase {

    private static final String CURRENT_BROADCAST_APP = "Music";
    private static final String SWITCH_APP = "System UI";
    private static final String TEST_PACKAGE = "com.android.systemui";
    private final LocalBluetoothManager mLocalBluetoothManager = mock(LocalBluetoothManager.class);
    private final LocalBluetoothProfileManager mLocalBluetoothProfileManager = mock(
            LocalBluetoothProfileManager.class);
    private final LocalBluetoothLeBroadcast mLocalBluetoothLeBroadcast = mock(
            LocalBluetoothLeBroadcast.class);
    private final BroadcastSender mBroadcastSender = mock(BroadcastSender.class);
    private BroadcastDialog mBroadcastDialog;
    private View mDialogView;
    private TextView mTitle;
    private TextView mSubTitle;
    private Button mSwitchBroadcastAppButton;
    private Button mChangeOutputButton;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mLocalBluetoothManager.getProfileManager()).thenReturn(mLocalBluetoothProfileManager);
        when(mLocalBluetoothProfileManager.getLeAudioBroadcastProfile()).thenReturn(null);
        mBroadcastDialog = new BroadcastDialog(mContext, mock(MediaOutputDialogFactory.class),
                mLocalBluetoothManager, CURRENT_BROADCAST_APP, TEST_PACKAGE,
                mock(UiEventLogger.class), mBroadcastSender);
        mBroadcastDialog.show();
        mDialogView = mBroadcastDialog.mDialogView;
    }

    @After
    public void tearDown() {
        mBroadcastDialog.dismiss();
    }

    @Test
    public void onCreate_withCurrentApp_titleIsCurrentAppName() {
        mTitle = mDialogView.requireViewById(R.id.dialog_title);

        assertThat(mTitle.getText().toString()).isEqualTo(mContext.getString(
                R.string.bt_le_audio_broadcast_dialog_title, CURRENT_BROADCAST_APP));
    }

    @Test
    public void onCreate_withCurrentApp_subTitleIsSwitchAppName() {
        mSubTitle = mDialogView.requireViewById(R.id.dialog_subtitle);

        assertThat(mSubTitle.getText()).isEqualTo(
                mContext.getString(R.string.bt_le_audio_broadcast_dialog_sub_title, SWITCH_APP));
    }

    @Test
    public void onCreate_withCurrentApp_switchBtnIsSwitchAppName() {
        mSwitchBroadcastAppButton = mDialogView.requireViewById(R.id.switch_broadcast);

        assertThat(mSwitchBroadcastAppButton.getText().toString()).isEqualTo(
                mContext.getString(R.string.bt_le_audio_broadcast_dialog_switch_app, SWITCH_APP));
    }

    @Test
    public void onClick_withChangeOutput_dismissBroadcastDialog() {
        mChangeOutputButton = mDialogView.requireViewById(R.id.change_output);
        mChangeOutputButton.performClick();

        assertThat(mBroadcastDialog.isShowing()).isFalse();
    }

    @Test
    public void onClick_withSwitchBroadcast_stopCurrentBroadcast() {
        when(mLocalBluetoothProfileManager.getLeAudioBroadcastProfile()).thenReturn(
                mLocalBluetoothLeBroadcast);
        mSwitchBroadcastAppButton = mDialogView.requireViewById(R.id.switch_broadcast);
        mSwitchBroadcastAppButton.performClick();

        verify(mLocalBluetoothLeBroadcast).stopLatestBroadcast();
    }

    @Test
    public void onClick_withSwitchBroadcast_stopCurrentBroadcastFailed() {
        mSwitchBroadcastAppButton = mDialogView.requireViewById(R.id.switch_broadcast);
        mSwitchBroadcastAppButton.performClick();

        assertThat(mSwitchBroadcastAppButton.getText().toString()).isEqualTo(
                mContext.getString(R.string.bt_le_audio_broadcast_dialog_switch_app, SWITCH_APP));
    }

    @Test
    public void handleLeBroadcastStopped_withBroadcastProfileNull_doRefreshButton() {
        when(mLocalBluetoothProfileManager.getLeAudioBroadcastProfile()).thenReturn(null);
        mSwitchBroadcastAppButton = mDialogView.requireViewById(R.id.switch_broadcast);

        mBroadcastDialog.handleLeBroadcastStopped();

        assertThat(mSwitchBroadcastAppButton.getText().toString()).isEqualTo(
                mContext.getString(R.string.bt_le_audio_broadcast_dialog_switch_app, SWITCH_APP));
    }

    @Test
    public void handleLeBroadcastStopped_withBroadcastProfile_doStartBroadcast() {
        when(mLocalBluetoothProfileManager.getLeAudioBroadcastProfile()).thenReturn(
                mLocalBluetoothLeBroadcast);

        mBroadcastDialog.handleLeBroadcastStopped();

        verify(mLocalBluetoothLeBroadcast).startBroadcast(eq(SWITCH_APP), any());
    }

    @Test
    public void handleLeBroadcastMetadataChanged_withNotLaunchFlag_doNotDismissDialog() {

        mBroadcastDialog.handleLeBroadcastMetadataChanged();

        assertThat(mBroadcastDialog.isShowing()).isTrue();
    }

    @Test
    public void handleLeBroadcastMetadataChanged_withLaunchFlag_dismissDialog() {

        mBroadcastDialog.handleLeBroadcastStarted();
        mBroadcastDialog.handleLeBroadcastMetadataChanged();

        assertThat(mBroadcastDialog.isShowing()).isFalse();
    }
}
