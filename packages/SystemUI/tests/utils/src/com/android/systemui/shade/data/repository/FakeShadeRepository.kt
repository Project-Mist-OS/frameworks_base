/*
 * Copyright (C) 2022 The Android Open Source Project
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
 *
 */

package com.android.systemui.shade.data.repository

import com.android.systemui.shade.domain.model.ShadeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Fake implementation of [ShadeRepository] */
class FakeShadeRepository : ShadeRepository {

    private val _shadeModel = MutableStateFlow(ShadeModel())
    override val shadeModel: Flow<ShadeModel> = _shadeModel

    private val _qsExpansion = MutableStateFlow(0f)
    override val qsExpansion = _qsExpansion

    private val _udfpsTransitionToFullShadeProgress = MutableStateFlow(0f)
    override val udfpsTransitionToFullShadeProgress = _udfpsTransitionToFullShadeProgress

    fun setShadeModel(model: ShadeModel) {
        _shadeModel.value = model
    }

    override fun setQsExpansion(qsExpansion: Float) {
        _qsExpansion.value = qsExpansion
    }

    override fun setUdfpsTransitionToFullShadeProgress(progress: Float) {
        _udfpsTransitionToFullShadeProgress.value = progress
    }
}
