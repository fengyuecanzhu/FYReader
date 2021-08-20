package xyz.fycz.myreader.base.adapter2.animations

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import xyz.fycz.myreader.base.adapter2.animations.BaseAnimation


class SlideInRightAnimation : BaseAnimation {


    override fun getAnimators(view: View): Array<Animator> =
        arrayOf(ObjectAnimator.ofFloat(view, "translationX", view.rootView.width.toFloat(), 0f))
}
