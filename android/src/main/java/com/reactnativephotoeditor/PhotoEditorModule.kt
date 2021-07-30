package com.reactnativephotoeditor

import android.content.Intent
import com.facebook.react.bridge.*
import com.reactnativephotoeditor.activity.PhotoEditorActivity

class PhotoEditorModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val context = reactApplicationContext;
  private val ACTIVITY_DOES_NOT_EXIST = "ACTIVITY_DOES_NOT_EXIST"
  override fun getName(): String {
    return "PhotoEditor"
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun open(options: ReadableMap?, promise: Promise): Unit {

    val activity = currentActivity
    if (activity == null) {
      promise.reject(ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
      return;
    }

    val intent = Intent(context, PhotoEditorActivity::class.java)
    activity.startActivity(intent)
    promise.resolve(false)

  }


}
