/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.base.adapter2.animations

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View


class ScaleInAnimation @JvmOverloads constructor(private val mFrom: Float = DEFAULT_SCALE_FROM) :
    BaseAnimation {

    override fun getAnimators(view: View): Array<Animator> {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", mFrom, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", mFrom, 1f)
        return arrayOf(scaleX, scaleY)
    }

    companion object {

        private const val DEFAULT_SCALE_FROM = .5f
    }
}
