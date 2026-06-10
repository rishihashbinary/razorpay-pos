package com.routehub.pos.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DueItem(
    val id: String,
    val monthLabel: String,
    val amount: Double,
    var isSelected: Boolean = true
) : Parcelable