package com.vr.flovision.helper

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.Snackbar

fun showSnack(activity: Activity, message: String) {
    val view: View? = activity.findViewById(android.R.id.content)
    Snackbar.make(view!!, message, Snackbar.LENGTH_LONG).show()
}