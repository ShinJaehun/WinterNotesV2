package com.shinjaehun.winternotesv2

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var nav: NavController

    companion object {
        var screenHeight = 0
        var screenWidth = 0

        var snowList: ArrayList<SnowFlake> = ArrayList()

        var isNotPaused = true

        var job1: Job = Job()

        const val disappear_margin = 32				// pixels from each border where objects disappear
        const val flake_TX: Float = 1f // max. sec. of flake's constant X-movement on fluttering
        const val flake_XperY: Float = 2f // fluttering movement's max. vx/vy ratio
        var refresh_FperS = 100f					// initial frames/second, recalculated.
        var flake_speed 	= 0.3f				// flake speed in pixel/frame
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav = Navigation.findNavController(this, R.id.fragment_nav)

        val wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            screenWidth = windowMetrics.bounds.width() - insets.left - insets.right
            screenHeight = windowMetrics.bounds.height() - insets.bottom - insets.top
        } else {
            val point = Point()
            wm.defaultDisplay.getRealSize(point)
            screenWidth = point.x
            screenHeight = point.y

        }
        setUpSnowEffect()

        job1 = lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                updateSnowFlakes(10L)
            }
        }
    }

    private fun setUpSnowEffect() {
        // generate 200 snow flake
        val container: ViewGroup = window.decorView as ViewGroup
        for (i in 0 until 30) {
            snowList.add(
                SnowFlake(
                    baseContext,
                    container,
                    screenWidth.toFloat(),
                    screenHeight.toFloat(),
                    false,
                )
            )
        }

        Log.i(TAG, "the size of snowList: ${snowList.size}")
    }

    private suspend fun updateSnowFlakes(delay_refresh: Long){
        while (isNotPaused) {
            for (snow: SnowFlake in snowList){
                snow.update()
            }
            delay(delay_refresh)
        }
    }
}