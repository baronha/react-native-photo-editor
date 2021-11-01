package com.reactnativephotoeditor.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reactnativephotoeditor.R
import java.io.InputStream
import java.net.URI
import java.net.URL

class StickerFragment : BottomSheetDialogFragment() {
  private var mStickerListener: StickerListener? = null
  private var data: List<String> = emptyList()

  fun setStickerListener(stickerListener: StickerListener?) {
    mStickerListener = stickerListener
  }

  fun setData(stickerList: ArrayList<String>) {
    if (stickerList.isNotEmpty()) {
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
    val contentView = View.inflate(context, R.layout.fragment_bottom_sticker_dialog, null)
    dialog.setContentView(contentView)
    val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
    val behavior = params.behavior
    if (behavior is BottomSheetBehavior<*>) {
      behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
    }
    (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
    val rvSticker: RecyclerView = contentView.findViewById(R.id.rvSticker)
    val gridLayoutManager = GridLayoutManager(
      activity, 5
    )
    rvSticker.layoutManager = gridLayoutManager
    val stickerAdapter = StickerAdapter()
    stickerAdapter.stickerList = this.data
    rvSticker.adapter = stickerAdapter
  }

  inner class StickerAdapter :
    RecyclerView.Adapter<StickerAdapter.ViewHolder?>() {
    lateinit var stickerList: List<String>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.row_sticker, parent, false)
      return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val item = stickerList[position]
      context?.let {
        Glide
          .with(it)
          .load(item)
          .centerCrop()
          .placeholder(R.drawable.ic_sticker_placeholder)
          .into(holder.imgSticker)
      }
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

            if (URLUtil.isValidUrl(url)) {
              val bitmap = handleBitmapImage(url)
              mStickerListener!!.onStickerClick(bitmap)
            } else {
              context?.let {
                val stream = openInputStream(
                  "file://$url",
                  it
                )

                val bitmap = BitmapFactory.decodeStream(stream)
                mStickerListener!!.onStickerClick(bitmap)
              }
            }
          }
          dismiss()
        }
      }
    }


  }

  private fun openInputStream(uriString: String?, context: Context): InputStream? {
    val uri = URI.create(uriString)
    return if (uri.scheme == "file" && uri.path.startsWith("/android_asset/")) {
      val path = uri.path.replace("/android_asset/", "") // TODO: should be at start only
      context.assets.open(path)
    } else {
      uri.toURL().openStream()
    }
  }

  private fun handleBitmapImage(path: String): Bitmap {
    val url = URL(path)
    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
    StrictMode.setThreadPolicy(policy)
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
}
