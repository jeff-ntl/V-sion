package com.example.v_sion.models

import android.graphics.drawable.Drawable;
import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ResultModel(
    var packageName: String? = null,
    var appIcon: Bitmap ?= null,
    var appName: String? = null,
    var timeInForeground : Long = 0,
    var launchCount : Long = 0
):Parcelable