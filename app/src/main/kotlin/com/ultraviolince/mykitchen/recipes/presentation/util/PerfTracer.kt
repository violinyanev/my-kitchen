package com.ultraviolince.mykitchen.recipes.presentation.util

import android.os.Build
import android.os.Trace

object PerfTracer {
    fun beginAsyncSection(sectionName: String, cookie: Int = 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Trace.beginAsyncSection(sectionName, cookie)
        }
    }

    fun endAsyncSection(sectionName: String, cookie: Int = 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Trace.endAsyncSection(sectionName, cookie)
        }
    }
}
