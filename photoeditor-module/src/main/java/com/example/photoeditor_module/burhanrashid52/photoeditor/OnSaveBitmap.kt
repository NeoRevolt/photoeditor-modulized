package com.example.photoeditor_module.burhanrashid52.photoeditor

import android.graphics.Bitmap

/**
 * @author [Burhanuddin Rashid](https://github.com/example)
 * @version 0.1.2
 * @since 5/21/2018
 */
interface OnSaveBitmap {
    fun onBitmapReady(saveBitmap: Bitmap?)
    fun onFailure(e: Exception?)
}