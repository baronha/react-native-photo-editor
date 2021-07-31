package com.reactnativephotoeditor

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.bridge.*
import com.reactnativephotoeditor.activity.PhotoEditorActivity

class PhotoEditorModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), ActivityEventListener {
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
    intent.putExtra("path", path)
    activity.startActivityForResult(intent, REQUEST_CODE)
  }

  override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
    println("path result: haha")
    if (requestCode == REQUEST_CODE) {
      when (resultCode) {
        RESULT_OK -> {
          val path = data?.getStringExtra("path")
          println("path result: $path")
//          promise.resolve(uri)
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    TODO("Not yet implemented")
  }


}
