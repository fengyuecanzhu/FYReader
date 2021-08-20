package xyz.fycz.myreader.base.adapter2.animations

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import xyz.fycz.myreader.base.adapter2.animations.BaseAnimation

class SlideInBottomAnimation : BaseAnimation {


    override fun getAnimators(view: View): Array<Animator> =
        arrayOf(ObjectAnimator.ofFloat(view, "translationY", view.measuredHeight.toFloat(), 0f))
}
