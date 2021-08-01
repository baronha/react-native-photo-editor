package com.reactnativephotoeditor

import android.app.Activity
import android.content.Intent
import com.facebook.react.bridge.*
import com.reactnativephotoeditor.activity.PhotoEditorActivity


class PhotoEditorModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val context = reactApplicationContext;
  private val ACTIVITY_DOES_NOT_EXIST = "ACTIVITY_DOES_NOT_EXIST"
  private val REQUEST_CODE = 1
  private var promise: Promise? = null
  override fun getName(): String {
    return "PhotoEditor"
  }

  @ReactMethod
  fun open(options: ReadableMap?, promise: Promise): Unit {
    this.promise = promise
    val activity = currentActivity
    if (activity == null) {
      promise.reject(ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
      return;
    }
    val path = options?.getString("path")
    val intent = Intent(context, PhotoEditorActivity::class.java)
    context.addActivityEventListener(mActivityEventListener)
    intent.putExtra("path", path)
    activity.startActivityForResult(intent, REQUEST_CODE)
  }

  private val mActivityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, intent: Intent) {
      if (requestCode == REQUEST_CODE) {
        when (resultCode) {
          Activity.RESULT_OK -> {
            val path = intent.getStringExtra("path")
            promise?.resolve("file://$path")
          }
          Activity.RESULT_CANCELED -> {
            promise?.reject("User Cancelled")
          }
        }
      }
    }
  }
}
