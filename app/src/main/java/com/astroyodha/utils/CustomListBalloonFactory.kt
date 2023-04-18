/*
 * Copyright (C) 2019 skydoves
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

package com.astroyodha.utils

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.astroyodha.R
import com.skydoves.balloon.*

/**
 * CustomListBalloonFactory: to show popup dialog set value as per your requirement
 */
class CustomListBalloonFactory : Balloon.Factory() {

    override fun create(context: Context, lifecycle: LifecycleOwner?): Balloon {
        return Balloon.Builder(context)
                .setLayout(R.layout.layout_custom_list)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(250)
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setArrowColorResource(R.color.shadow_color)
                .setArrowPosition(0.5f)
                .setArrowSize(10)
                .setTextSize(12f)
                .setCornerRadius(6f)
                .setMarginRight(12)
                .setElevation(6)
                .setBackgroundColorResource(R.color.white)
                .setBalloonAnimation(BalloonAnimation.FADE)
                .setIsVisibleOverlay(false)
                .setOverlayColorResource(R.color.grey)
                .setOverlayPadding(12.5f)
                .setDismissWhenShowAgain(true)
                .setDismissWhenTouchOutside(true)
                .setDismissWhenOverlayClicked(false)
                .setLifecycleOwner(lifecycle)
                .build()
    }
}
