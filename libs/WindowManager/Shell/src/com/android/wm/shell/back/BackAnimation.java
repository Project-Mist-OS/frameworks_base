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

package com.android.wm.shell.back;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.window.BackEvent;

import com.android.wm.shell.common.annotations.ExternalThread;

/**
 * Interface for external process to get access to the Back animation related methods.
 */
@ExternalThread
public interface BackAnimation {

    /**
     * Called when a {@link MotionEvent} is generated by a back gesture.
     *
     * @param touchX the X touch position of the {@link MotionEvent}.
     * @param touchY the Y touch position of the {@link MotionEvent}.
     * @param velocityX the X velocity computed from the {@link MotionEvent}.
     * @param velocityY the Y velocity computed from the {@link MotionEvent}.
     * @param keyAction the original {@link KeyEvent#getAction()} when the event was dispatched to
     *               the process. This is forwarded separately because the input pipeline may mutate
     *               the {#event} action state later.
     * @param swipeEdge the edge from which the swipe begins.
     */
    void onBackMotion(
            float touchX,
            float touchY,
            float velocityX,
            float velocityY,
            int keyAction,
            @BackEvent.SwipeEdge int swipeEdge);

    /**
     * Sets whether the back gesture is past the trigger threshold or not.
     */
    void setTriggerBack(boolean triggerBack);

    /**
     * Sets whether the back long swipe gesture is past the trigger threshold or not.
     */
    void setTriggerLongSwipe(boolean triggerLongSwipe);

    /**
     * Sets the threshold values that define edge swipe behavior.<br>
     * <br>
     * <h1>How does {@code nonLinearFactor} work?</h1>
     * <pre>
     *     screen              screen              screen
     *     width               width               width
     *    |——————|            |————————————|      |————————————————————|
     *           A     B                   A                   B  C    A
     *  1 +——————+—————+    1 +————————————+    1 +————————————+———————+
     *    |     /      |      |          —/|      |            | —————/|
     *    |    /       |      |        —/  |      |           ——/      |
     *    |   /        |      |      —/    |      |        ——/ |       |
     *    |  /         |      |    —/      |      |     ——/    |       |
     *    | /          |      |  —/        |      |  ——/       |       |
     *    |/           |      |—/          |      |—/          |       |
     *  0 +————————————+    0 +————————————+    0 +————————————+———————+
     *                 B                   B                   B
     * </pre>
     * Three devices with different widths (smaller, equal, and wider) relative to the progress
     * threshold are shown in the graphs.<br>
     * - A is the width of the screen<br>
     * - B is the progress threshold (horizontal swipe distance where progress is linear)<br>
     * - C equals B + (A - B) * nonLinearFactor<br>
     * <br>
     * If A is less than or equal to B, {@code progress} for the swipe distance between:<br>
     * - [0, A] will scale linearly between [0, 1].<br>
     * If A is greater than B, {@code progress} for swipe distance between:<br>
     * - [0, B] will scale linearly between [0, B / C]<br>
     * - (B, A] will scale non-linearly and reach 1.
     *
     * @param linearDistance up to this distance progress continues linearly. B in the graph above.
     * @param maxDistance distance at which the progress will be 1f. A in the graph above.
     * @param nonLinearFactor This value is used to calculate the target if the screen is wider
     *                        than the progress threshold.
     */
    void setSwipeThresholds(float linearDistance, float maxDistance, float nonLinearFactor);

    /**
     * Sets the system bar listener to control the system bar color.
     * @param customizer the controller to control system bar color.
     */
    void setStatusBarCustomizer(StatusBarCustomizer customizer);
}
