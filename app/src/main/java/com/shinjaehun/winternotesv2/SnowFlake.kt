package com.shinjaehun.winternotesv2

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.shinjaehun.winternotesv2.MainActivity.Companion.disappear_margin
import com.shinjaehun.winternotesv2.MainActivity.Companion.flake_TX
import com.shinjaehun.winternotesv2.MainActivity.Companion.flake_XperY
import com.shinjaehun.winternotesv2.MainActivity.Companion.flake_speed
import com.shinjaehun.winternotesv2.MainActivity.Companion.refresh_FperS
import java.util.Random

class SnowFlake(
    context: Context,
    parent: ViewGroup,
    private val screenW: Float,
    private val screenH: Float,
    private val flake_visible: Boolean,
) {

    private var snowRes: IntArray = intArrayOf(R.drawable.snow0, R.drawable.snow1, R.drawable.snow2, R.drawable.snow3, R.drawable.snow4, R.drawable.snow5, R.drawable.snow6)
    var iv: ImageView
    var flakeX: Float
    var flakeY: Float
    var flakeSX: Float
    var flakeVX: Float
    var flakeVY: Float
    var flakeVIS: Boolean

//        private var distance = 0.7f // 0.5 ~ 1.0f
//        private var randomParam = 0.0f // -0.3 ~ 0.3 f for some special effects
//        private val fallingSpeed = 6
//        private val windSpeed = 4

    init {
        iv = ImageView(context)

        flakeX = Random().nextFloat() * screenW
        flakeY = Random().nextFloat() * screenH
//            flakeY = 0f
        flakeSX = 0f
        flakeVX = 0f
        flakeVY = 1f
        flakeVIS = flake_visible

//            randomParam = Random().nextFloat() * 0.6f - 0.3f
//            distance = Random().nextFloat() * 0.5f + 0.5f
        iv.setBackgroundResource(snowRes[Random().nextInt(6)])
        parent.addView(iv)

        // size
//            iv!!.layoutParams.height = (screenH * 0.01).toInt()
//            iv!!.layoutParams.width = (screenH * 0.01).toInt()
        iv.layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
        iv.layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT

    }

    fun update() {
//            flakeX += flakeVX + flakeDX
//            flakeY += flakeVY + flakeDY

        flakeX += flakeVX
        flakeY += flakeVY

        if (flakeY > screenH - disappear_margin) {
            flakeX = Random().nextFloat() * screenW
            flakeY = 0f
            flakeVY = (Random().nextFloat() * flakeVY) + flake_speed

//                Log.i(TAG, "new flakeVY: $flakeVY")
//                Log.i(TAG, "new flake_speed: $flake_speed")

            if (Random().nextFloat() < 0.1) flakeVY *= 2
            if (!flake_visible) flakeVIS = true
        }

//            Log.i(TAG, "flakeVIS: $flakeVIS")
//            Log.i(TAG, "flakeVY: $flakeVY")
//            Log.i(TAG, "flake_speed: $flake_speed")

        flakeSX--
        if (flakeSX <= 0) {
            flakeSX = Random().nextFloat() * refresh_FperS * flake_TX
            flakeVX = (2f* Random().nextFloat()-1f) * flake_XperY * flake_speed
//                flakeSX = Random().nextFloat() * flake_TX
//                flakeVX = (2f * Random().nextFloat() - 1f) * flake_speed
        }

//            Log.i(TAG, "refresh_FperS: $refresh_FperS")

        if (flakeX < -disappear_margin)
            flakeX += screenW.toInt()
        if (flakeX >= screenW - disappear_margin)
            flakeX -= screenW.toInt()

        if (flakeVIS) {
            iv.translationX = flakeX
            iv.translationY = flakeY
        }

        // far=slow, close=fast
//            iv.translationY = (iv.translationY + fallingSpeed * (1 - distance * 0.7)).toFloat()
//            iv.translationX = (iv.translationX + windSpeed * (1 - distance * 0.7)).toFloat()
//            var time: Float = System.currentTimeMillis() % 1000 / 1000.toFloat()
//            if (iv.translationY > screenH)
//                iv.translationY = iv.translationY - screenH
//            if (iv.translationX > screenW)
//                iv.translationX = iv.translationX - screenW
//            iv.rotation = iv.rotation +  randomParam * 5 // rotation parameter

    }
}