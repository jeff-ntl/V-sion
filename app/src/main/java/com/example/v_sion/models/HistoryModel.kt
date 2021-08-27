package com.example.v_sion.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HistoryModel(
    var uid: String? = "",
    var user_email: String? = "",
    var date: String = "",
    var time : String = "",
    var targetAchieved: String = ""
): Parcelable