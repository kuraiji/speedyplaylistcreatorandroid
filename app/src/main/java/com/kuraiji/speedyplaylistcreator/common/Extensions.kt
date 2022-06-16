package com.kuraiji.speedyplaylistcreator.common

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import com.kuraiji.speedyplaylistcreator.ui.MainActivity

internal fun Context.findActivity(): MainActivity? = when (this) {
    is MainActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}