package com.reactnativestripesdk

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver

internal object PrimaryButtonAccessibilityHelper {
  const val PRIMARY_BUTTON_ID = "stripe_primary_button"

  private var registered = false

  fun ensureAttached(application: Application) {
    if (registered) return
    registered = true
    application.registerActivityLifecycleCallbacks(callbacks)
  }

  private val callbacks = object : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
      if (!activity.javaClass.name.startsWith("com.stripe.android")) return
      val root = activity.window?.decorView ?: return
      root.viewTreeObserver.addOnGlobalLayoutListener(
        ViewTreeObserver.OnGlobalLayoutListener { tagPrimaryButtons(root) },
      )
      root.post { tagPrimaryButtons(root) }
    }

    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
  }

  private fun tagPrimaryButtons(view: View) {
    if (matchesPrimaryButton(view) && view.contentDescription != PRIMARY_BUTTON_ID) {
      view.contentDescription = PRIMARY_BUTTON_ID
    }
    if (view is ViewGroup) {
      for (i in 0 until view.childCount) tagPrimaryButtons(view.getChildAt(i))
    }
  }

  private fun matchesPrimaryButton(view: View): Boolean {
    if (view.javaClass.simpleName == "PrimaryButton") return true
    val id = view.id
    if (id == View.NO_ID) return false
    return try {
      view.resources.getResourceEntryName(id) == "primary_button"
    } catch (_: Throwable) {
      false
    }
  }
}
