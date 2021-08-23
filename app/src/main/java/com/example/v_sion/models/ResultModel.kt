package com.example.v_sion.models

import android.graphics.drawable.Drawable;
import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ResultModel(
    //var appIcon : Drawable? = null,
    //var appName: String? = null,
    var packageName: String? = null,
    var timeInForeground: Long = 0,
    var launchCount : Long = 0
):Parcelable