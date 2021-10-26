package com.reactnativephotoeditor.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnticipateOvershootInterpolator
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reactnativephotoeditor.R
import com.reactnativephotoeditor.activity.EmojiBSFragment.EmojiListener
import com.reactnativephotoeditor.activity.StickerFragment.StickerListener
import com.reactnativephotoeditor.activity.filters.FilterListener
import com.reactnativephotoeditor.activity.filters.FilterViewAdapter
import com.reactnativephotoeditor.activity.tools.EditingToolsAdapter
import com.reactnativephotoeditor.activity.tools.EditingToolsAdapter.OnItemSelected
import com.reactnativephotoeditor.activity.tools.ToolType
import ja.burhanrashid52.photoeditor.*
import ja.burhanrashid52.photoeditor.PhotoEditor.OnSaveListener
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType
import java.io.File


open class PhotoEditorActivity : AppCompatActivity(), OnPhotoEditorListener, View.OnClickListener,
  PropertiesBSFragment.Properties, ShapeBSFragment.Properties, EmojiListener, StickerListener,
  OnItemSelected, FilterListener {
  private var mPhotoEditor: PhotoEditor? = null
  private var mProgressDialog: ProgressDialog? = null
  private var mPhotoEditorView: PhotoEditorView? = null
  private var mPropertiesBSFragment: PropertiesBSFragment? = null
  private var mShapeBSFragment: ShapeBSFragment? = null
  private var mShapeBuilder: ShapeBuilder? = null
  private var mEmojiBSFragment: EmojiBSFragment? = null
  private var mStickerFragment: StickerFragment? = null
  private var mTxtCurrentTool: TextView? = null
  private var mRvTools: RecyclerView? = null
  private var mRvFilters: RecyclerView? = null
  private val mEditingToolsAdapter = EditingToolsAdapter(this)
  private val mFilterViewAdapter = FilterViewAdapter(this)
  private var mRootView: ConstraintLayout? = null
  private val mConstraintSet = ConstraintSet()
  private var mIsFilterVisible = false

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    makeFullScreen()
    setContentView(R.layout.photo_editor_view)
    initViews()

    //intern
    val value = intent.extras
    val path = value?.getString("path")
    val stickers = value?.getStringArrayList("stickers")

    mPropertiesBSFragment = PropertiesBSFragment()
    mPropertiesBSFragment!!.setPropertiesChangeListener(this)

    mEmojiBSFragment = EmojiBSFragment()
    mEmojiBSFragment!!.setEmojiListener(this)

    mStickerFragment = StickerFragment()
    mStickerFragment!!.setStickerListener(this)
    mStickerFragment!!.setData(stickers!!)

    mShapeBSFragment = ShapeBSFragment()
    mShapeBSFragment!!.setPropertiesChangeListener(this)

    val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    mRvTools!!.layoutManager = llmTools
    mRvTools!!.adapter = mEditingToolsAdapter

    val llmFilters = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    mRvFilters!!.layoutManager = llmFilters
    mRvFilters!!.adapter = mFilterViewAdapter

    val pinchTextScalable = intent.getBooleanExtra(PINCH_TEXT_SCALABLE_INTENT_KEY, true)
    val mEmojiTypeFace = Typeface.createFromAsset(assets, "emojione-android.ttf")
    mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
      .setPinchTextScalable(pinchTextScalable) // set flag to make text scalable when pinch
      .setDefaultEmojiTypeface(mEmojiTypeFace)
      .build() // build photo editor sdk
    mPhotoEditor?.setOnPhotoEditorListener(this)

    Glide
      .with(this)
      .load(path)
//      .placeholder(drawable)
      .into(mPhotoEditorView!!.source);
  }

  private fun showLoading(message: String) {
    mProgressDialog = ProgressDialog(this)
    mProgressDialog!!.setMessage(message)
    mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
    mProgressDialog!!.setCancelable(false)
    mProgressDialog!!.show()
  }

  protected fun hideLoading() {
    if (mProgressDialog != null) {
      mProgressDialog!!.dismiss()
    }
  }

  private fun requestPermission(permission: String) {
    val isGranted =
      ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    if (!isGranted) {
      ActivityCompat.requestPermissions(
        this, arrayOf(permission),
        READ_WRITE_STORAGE
      )
    }
  }

  private fun makeFullScreen() {
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    window.setFlags(
      WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN
    )
  }

  private fun initViews() {
    //REDO
    val imgRedo: ImageView = findViewById(R.id.imgRedo)
    imgRedo.setOnClickListener(this)
    //UNDO
    val imgUndo: ImageView = findViewById(R.id.imgUndo)
    imgUndo.setOnClickListener(this)
    //CLOSE
    val imgClose: ImageView = findViewById(R.id.imgClose)
    imgClose.setOnClickListener(this)
    //SAVE
    val imgSave: ImageView = findViewById(R.id.imgSave)
    imgSave.setOnClickListener(this)

    mPhotoEditorView = findViewById(R.id.photoEditorView)
    mTxtCurrentTool = findViewById(R.id.txtCurrentTool)
    mRvTools = findViewById(R.id.rvConstraintTools)
    mRvFilters = findViewById(R.id.rvFilterView)
    mRootView = findViewById(R.id.rootView)
  }

  override fun onEditTextChangeListener(rootView: View, text: String, colorCode: Int) {
    val textEditorDialogFragment = TextEditorDialogFragment.show(this, text, colorCode)
    textEditorDialogFragment.setOnTextEditorListener { inputText: String?, newColorCode: Int ->
      val styleBuilder = TextStyleBuilder()
      styleBuilder.withTextColor(newColorCode)
      mPhotoEditor!!.editText(rootView, inputText, styleBuilder)
      mTxtCurrentTool!!.setText(R.string.label_text)
    }
  }

  override fun onAddViewListener(viewType: ViewType, numberOfAddedViews: Int) {
    Log.d(
      TAG,
      "onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
    )
  }

  override fun onRemoveViewListener(viewType: ViewType, numberOfAddedViews: Int) {
    Log.d(
      TAG,
      "onRemoveViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
    )
  }

  override fun onStartViewChangeListener(viewType: ViewType) {
    Log.d(TAG, "onStartViewChangeListener() called with: viewType = [$viewType]")
  }

  override fun onStopViewChangeListener(viewType: ViewType) {
    Log.d(TAG, "onStopViewChangeListener() called with: viewType = [$viewType]")
  }

  @SuppressLint("NonConstantResourceId")
  override fun onClick(view: View) {
    when (view.id) {
      R.id.imgUndo -> {
        mPhotoEditor!!.undo()
      }
      R.id.imgRedo -> {
        mPhotoEditor!!.redo()
      }
      R.id.imgSave -> {
        saveImage()
      }
      R.id.imgClose -> {
        onBackPressed()
      }
    }
  }

  private fun isSdkHigherThan28(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
  }

  private fun saveImage() {
    val fileName = System.currentTimeMillis().toString() + ".png"
    val hasStoragePermission = ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
    if (hasStoragePermission || isSdkHigherThan28()) {
      showLoading("Saving...")
      val path: File = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES
      )
      val file = File(path, fileName)
      path.mkdirs()

      mPhotoEditor!!.saveAsFile(file.absolutePath, object : OnSaveListener {
        override fun onSuccess(@NonNull imagePath: String) {
          hideLoading()
          val intent = Intent()
          intent.putExtra("path", imagePath)
          setResult(RESULT_OK, intent)
          finish()
        }

        override fun onFailure(@NonNull exception: Exception) {
          hideLoading()
        }
      })
    } else {
      requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
  }

  override fun onColorChanged(colorCode: Int) {
    mPhotoEditor!!.setShape(mShapeBuilder!!.withShapeColor(colorCode))
    mTxtCurrentTool!!.setText(R.string.label_brush)
  }

  override fun onOpacityChanged(opacity: Int) {
    mPhotoEditor!!.setShape(mShapeBuilder!!.withShapeOpacity(opacity))
    mTxtCurrentTool!!.setText(R.string.label_brush)
  }

  override fun onShapeSizeChanged(shapeSize: Int) {
    mPhotoEditor!!.setShape(mShapeBuilder!!.withShapeSize(shapeSize.toFloat()))
    mTxtCurrentTool!!.setText(R.string.label_brush)
  }

  override fun onShapePicked(shapeType: ShapeType) {
    mPhotoEditor!!.setShape(mShapeBuilder!!.withShapeType(shapeType))
  }

  override fun onEmojiClick(emojiUnicode: String) {
    mPhotoEditor!!.addEmoji(emojiUnicode)
    mTxtCurrentTool!!.setText(R.string.label_emoji)
  }

  override fun onStickerClick(bitmap: Bitmap) {
    mPhotoEditor!!.addImage(bitmap)
    mTxtCurrentTool!!.setText(R.string.label_sticker)
  }

  private fun showSaveDialog() {
    val builder = AlertDialog.Builder(this)
    builder.setMessage(getString(R.string.msg_save_image))
    builder.setPositiveButton("Save") { _: DialogInterface?, _: Int -> saveImage() }
    builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
    builder.setNeutralButton("Discard") { _: DialogInterface?, _: Int -> onCancel() }
    builder.create().show()
  }

  private fun onCancel() {
    val intent = Intent()
    setResult(RESULT_CANCELED, intent)
    finish()
  }

  override fun onFilterSelected(photoFilter: PhotoFilter) {
    mPhotoEditor!!.setFilterEffect(photoFilter)
  }

  override fun onToolSelected(toolType: ToolType) {
    when (toolType) {
      ToolType.SHAPE -> {
        mPhotoEditor!!.setBrushDrawingMode(true)
        mShapeBuilder = ShapeBuilder()
        mPhotoEditor!!.setShape(mShapeBuilder)
        mTxtCurrentTool!!.setText(R.string.label_shape)
        showBottomSheetDialogFragment(mShapeBSFragment)
      }
      ToolType.TEXT -> {
        val textEditorDialogFragment = TextEditorDialogFragment.show(this)
        textEditorDialogFragment.setOnTextEditorListener { inputText: String?, colorCode: Int ->
          val styleBuilder = TextStyleBuilder()
          styleBuilder.withTextColor(colorCode)
          mPhotoEditor!!.addText(inputText, styleBuilder)
          mTxtCurrentTool!!.setText(R.string.label_text)
        }
      }
      ToolType.ERASER -> {
        mPhotoEditor!!.brushEraser()
        mTxtCurrentTool!!.setText(R.string.label_eraser_mode)
      }
      ToolType.FILTER -> {
        mTxtCurrentTool!!.setText(R.string.label_filter)
        showFilter(true)
      }
      ToolType.EMOJI -> showBottomSheetDialogFragment(mEmojiBSFragment)
      ToolType.STICKER -> showBottomSheetDialogFragment(mStickerFragment)
    }
  }

  private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
    if (fragment == null || fragment.isAdded) {
      return
    }
    fragment.show(supportFragmentManager, fragment.tag)
  }

  fun showFilter(isVisible: Boolean) {
    mIsFilterVisible = isVisible
    mConstraintSet.clone(mRootView)
    if (isVisible) {
      mConstraintSet.clear(mRvFilters!!.id, ConstraintSet.START)
      mConstraintSet.connect(
        mRvFilters!!.id, ConstraintSet.START,
        ConstraintSet.PARENT_ID, ConstraintSet.START
      )
      mConstraintSet.connect(
        mRvFilters!!.id, ConstraintSet.END,
        ConstraintSet.PARENT_ID, ConstraintSet.END
      )
    } else {
      mConstraintSet.connect(
        mRvFilters!!.id, ConstraintSet.START,
        ConstraintSet.PARENT_ID, ConstraintSet.END
      )
      mConstraintSet.clear(mRvFilters!!.id, ConstraintSet.END)
    }
    val changeBounds = ChangeBounds()
    changeBounds.duration = 350
    changeBounds.interpolator = AnticipateOvershootInterpolator(1.0f)
    TransitionManager.beginDelayedTransition(mRootView!!, changeBounds)
    mConstraintSet.applyTo(mRootView)
  }

  override fun onBackPressed() {
    if (mIsFilterVisible) {
      showFilter(false)
      mTxtCurrentTool!!.setText(R.string.app_name)
    } else if (!mPhotoEditor!!.isCacheEmpty) {
      showSaveDialog()
    } else {
      onCancel()
    }
  }

  companion object {
    private val TAG = PhotoEditorActivity::class.java.simpleName
    const val FILE_PROVIDER_AUTHORITY = "com.burhanrashid52.photoeditor.fileprovider"
    const val ACTION_NEXTGEN_EDIT = "action_nextgen_edit"
    const val PINCH_TEXT_SCALABLE_INTENT_KEY = "PINCH_TEXT_SCALABLE"
    const val READ_WRITE_STORAGE = 52
  }
}
