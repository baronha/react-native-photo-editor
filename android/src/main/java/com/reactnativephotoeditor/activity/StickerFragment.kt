package com.reactnativephotoeditor.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reactnativephotoeditor.R
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection


class StickerFragment : BottomSheetDialogFragment() {
  private var mStickerListener: StickerListener? = null
  private var data: List<String> = emptyList()

  fun setStickerListener(stickerListener: StickerListener?) {
    mStickerListener = stickerListener
  }

  fun setData(stickerList: ArrayList<String>){
    if (stickerList.isNotEmpty()){
      this.data = this.data + stickerList
    }
  }

  interface StickerListener {
    fun onStickerClick(bitmap: Bitmap)
  }

  private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
    override fun onStateChanged(bottomSheet: View, newState: Int) {
      if (newState == BottomSheetBehavior.STATE_HIDDEN) {
        dismiss()
      }
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
  }

  @SuppressLint("RestrictedApi")
  override fun setupDialog(dialog: Dialog, style: Int) {
    super.setupDialog(dialog, style)
    val contentView = View.inflate(context, R.layout.fragment_bottom_sticker_emoji_dialog, null)
    dialog.setContentView(contentView)
    val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
    val behavior = params.behavior
    if (behavior is BottomSheetBehavior<*>) {
      behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
    }
    (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
    val rvEmoji: RecyclerView = contentView.findViewById(R.id.rvEmoji)
    val gridLayoutManager = GridLayoutManager(
      activity, 3
    )
    rvEmoji.layoutManager = gridLayoutManager
    val stickerAdapter = StickerAdapter()
    stickerAdapter.stickerList = this.data
    rvEmoji.adapter = stickerAdapter
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }

  inner class StickerAdapter :
    RecyclerView.Adapter<StickerAdapter.ViewHolder?>() {
    lateinit var stickerList: List<String>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.row_sticker, parent, false)
      return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      context?.let {
        Glide
          .with(it)
          .load(stickerList[position])
          .centerCrop()
          .placeholder(R.drawable.ic_sticker_placeholder)
          .into(holder.imgSticker)
      };
    }

    override fun getItemCount(): Int {
      return stickerList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      val imgSticker: ImageView = itemView.findViewById(R.id.imgSticker)
      init {
        itemView.setOnClickListener {
          if (mStickerListener != null) {
            val url = stickerList[layoutPosition]
            val bitmap = handleBitmapImage(url)
            mStickerListener!!.onStickerClick(bitmap)
          }
          dismiss()
        }
      }
    }

  }

  private fun handleBitmapImage(path: String) : Bitmap {
      val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
      StrictMode.setThreadPolicy(policy)
      val url = URL(path)
      return BitmapFactory.decodeStream(url.openConnection().getInputStream())
  }

  private fun convertEmoji(emoji: String): String {
    var returnedEmoji = ""
    returnedEmoji = try {
      val convertEmojiToInt = emoji.substring(2).toInt(16)
      getEmojiByUnicode(convertEmojiToInt)
    } catch (e: NumberFormatException) {
      ""
    }
    return returnedEmoji
  }

  private fun getEmojiByUnicode(unicode: Int): String {
    return String(Character.toChars(unicode))
  }

  fun downloadImage(url: String?): Bitmap? {
    var bitmap: Bitmap? = null
    var stream: InputStream? = null
    val bmOptions = BitmapFactory.Options()
    bmOptions.inSampleSize = 1
    try {
      stream = getHttpConnection(url)
      bitmap = BitmapFactory.decodeStream(stream, null, bmOptions)
      stream!!.close()
    } catch (e1: IOException) {
      e1.printStackTrace()
      println("downloadImage $e1")
    }
    return bitmap
  }

  private fun getHttpConnection(urlString: String?): InputStream? {
    var stream: InputStream? = null
    val url = URL(urlString)
    val connection: URLConnection = url.openConnection()
    try {
      val httpConnection: HttpURLConnection = connection as HttpURLConnection
      httpConnection.requestMethod = "GET"
      httpConnection.connect()
      if (httpConnection.responseCode === HttpURLConnection.HTTP_OK) {
        stream = httpConnection.getInputStream()
      }
    } catch (ex: Exception) {
      ex.printStackTrace()
      println("downloadImage$ex")
    }
    return stream
  }
}
