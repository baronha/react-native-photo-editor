package com.reactnativephotoeditor.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.reactnativephotoeditor.R;
import com.reactnativephotoeditor.activity.filters.FilterListener;
import com.reactnativephotoeditor.activity.filters.FilterViewAdapter;
import com.reactnativephotoeditor.activity.tools.EditingToolsAdapter;
import com.reactnativephotoeditor.activity.tools.ToolType;

import java.io.File;
import java.io.IOException;

import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;
import ja.burhanrashid52.photoeditor.SaveSettings;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import ja.burhanrashid52.photoeditor.shape.ShapeType;
import ja.burhanrashid52.photoeditor.TextStyleBuilder;
import ja.burhanrashid52.photoeditor.ViewType;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.reactnativephotoeditor.activity.FileSaveHelper.isSdkHigherThan28;

public class PhotoEditorActivity extends AppCompatActivity implements OnPhotoEditorListener,
  View.OnClickListener,
  PropertiesBSFragment.Properties,
  ShapeBSFragment.Properties,
  EmojiBSFragment.EmojiListener,
  StickerBSFragment.StickerListener, EditingToolsAdapter.OnItemSelected, FilterListener {

  private static final String TAG = PhotoEditorActivity.class.getSimpleName();
  public static final String FILE_PROVIDER_AUTHORITY = "com.burhanrashid52.photoeditor.fileprovider";
  public static final String ACTION_NEXTGEN_EDIT = "action_nextgen_edit";
  public static final String PINCH_TEXT_SCALABLE_INTENT_KEY = "PINCH_TEXT_SCALABLE";
  public static final int READ_WRITE_STORAGE = 52;

  PhotoEditor mPhotoEditor;
  private ProgressDialog mProgressDialog;
  private PhotoEditorView mPhotoEditorView;
  private PropertiesBSFragment mPropertiesBSFragment;
  private ShapeBSFragment mShapeBSFragment;
  private ShapeBuilder mShapeBuilder;
  private EmojiBSFragment mEmojiBSFragment;
  private StickerBSFragment mStickerBSFragment;
  private TextView mTxtCurrentTool;
  private RecyclerView mRvTools, mRvFilters;
  private final EditingToolsAdapter mEditingToolsAdapter = new EditingToolsAdapter(this);
  private final FilterViewAdapter mFilterViewAdapter = new FilterViewAdapter(this);
  private ConstraintLayout mRootView;
  private final ConstraintSet mConstraintSet = new ConstraintSet();
  private boolean mIsFilterVisible;

  @Nullable
  @VisibleForTesting
  Uri mSaveImageUri;

  private FileSaveHelper mSaveFileHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    makeFullScreen();
    setContentView(R.layout.photo_editor_view);

    initViews();

    handleIntentImage(mPhotoEditorView.getSource());

    mPropertiesBSFragment = new PropertiesBSFragment();
    mEmojiBSFragment = new EmojiBSFragment();
    mStickerBSFragment = new StickerBSFragment();
    mShapeBSFragment = new ShapeBSFragment();
    mStickerBSFragment.setStickerListener(this);
    mEmojiBSFragment.setEmojiListener(this);
    mPropertiesBSFragment.setPropertiesChangeListener(this);
    mShapeBSFragment.setPropertiesChangeListener(this);

    LinearLayoutManager llmTools = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
    mRvTools.setLayoutManager(llmTools);
    mRvTools.setAdapter(mEditingToolsAdapter);

    LinearLayoutManager llmFilters = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
    mRvFilters.setLayoutManager(llmFilters);
    mRvFilters.setAdapter(mFilterViewAdapter);

    // NOTE(lucianocheng): Used to set integration testing parameters to PhotoEditor
    boolean pinchTextScalable = getIntent().getBooleanExtra(PINCH_TEXT_SCALABLE_INTENT_KEY, true);

    Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");

    mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
      .setPinchTextScalable(pinchTextScalable) // set flag to make text scalable when pinch
      .setDefaultEmojiTypeface(mEmojiTypeFace)
      .build(); // build photo editor sdk

    mPhotoEditor.setOnPhotoEditorListener(this);

    //Set Image Dynamically
    mPhotoEditorView.getSource().setImageResource(R.drawable.paris_tower);

    mSaveFileHelper = new FileSaveHelper(this);
  }

  protected void showLoading(@NonNull String message) {
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(message);
    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    mProgressDialog.setCancelable(false);
    mProgressDialog.show();
  }

  protected void hideLoading() {
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
    }
  }

  public void requestPermission(String permission) {
    boolean isGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    if (!isGranted) {
      ActivityCompat.requestPermissions(
        this,
        new String[]{permission},
        READ_WRITE_STORAGE);
    }
  }

  public void makeFullScreen() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }

  protected void showSnackbar(@NonNull String message) {
    View view = findViewById(android.R.id.content);
    if (view != null) {
      Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    } else {
      Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
  }

  private void handleIntentImage(ImageView source) {
    Intent intent = getIntent();
    if (intent != null) {
      // NOTE(lucianocheng): Using "yoda conditions" here to guard against
      //                     a null Action in the Intent.
      if (Intent.ACTION_EDIT.equals(intent.getAction()) ||
        ACTION_NEXTGEN_EDIT.equals(intent.getAction())) {
        try {
          Uri uri = intent.getData();
          Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
          source.setImageBitmap(bitmap);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        String intentType = intent.getType();
        if (intentType != null && intentType.startsWith("image/")) {
          Uri imageUri = intent.getData();
          if (imageUri != null) {
            source.setImageURI(imageUri);
          }
        }
      }
    }
  }

  private void initViews() {
    ImageView imgUndo;
    ImageView imgRedo;
    ImageView imgSave;
    ImageView imgClose;

    mPhotoEditorView = findViewById(R.id.photoEditorView);
    mTxtCurrentTool = findViewById(R.id.txtCurrentTool);
    mRvTools = findViewById(R.id.rvConstraintTools);
    mRvFilters = findViewById(R.id.rvFilterView);
    mRootView = findViewById(R.id.rootView);

    imgUndo = findViewById(R.id.imgUndo);
    imgUndo.setOnClickListener(this);

    imgRedo = findViewById(R.id.imgRedo);
    imgRedo.setOnClickListener(this);

    imgSave = findViewById(R.id.imgSave);
    imgSave.setOnClickListener(this);

    imgClose = findViewById(R.id.imgClose);
    imgClose.setOnClickListener(this);
  }

  @Override
  public void onEditTextChangeListener(final View rootView, String text, int colorCode) {
    TextEditorDialogFragment textEditorDialogFragment =
      TextEditorDialogFragment.show(this, text, colorCode);
    textEditorDialogFragment.setOnTextEditorListener((inputText, newColorCode) -> {
      final TextStyleBuilder styleBuilder = new TextStyleBuilder();
      styleBuilder.withTextColor(newColorCode);

      mPhotoEditor.editText(rootView, inputText, styleBuilder);
      mTxtCurrentTool.setText(R.string.label_text);
    });
  }

  @Override
  public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
    Log.d(TAG, "onAddViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]");
  }

  @Override
  public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {
    Log.d(TAG, "onRemoveViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]");
  }

  @Override
  public void onStartViewChangeListener(ViewType viewType) {
    Log.d(TAG, "onStartViewChangeListener() called with: viewType = [" + viewType + "]");
  }

  @Override
  public void onStopViewChangeListener(ViewType viewType) {
    Log.d(TAG, "onStopViewChangeListener() called with: viewType = [" + viewType + "]");
  }

  @SuppressLint("NonConstantResourceId")
  @Override
  public void onClick(View view) {
    int id = view.getId();
    if (id == R.id.imgUndo) {
      mPhotoEditor.undo();
    } else if (id == R.id.imgRedo) {
      mPhotoEditor.redo();
    } else if (id == R.id.imgSave) {
      saveImage();
    } else if (id == R.id.imgClose) {
      onBackPressed();
    }
  }

  private Uri buildFileProviderUri(@NonNull Uri uri) {
    return FileProvider.getUriForFile(this,
      FILE_PROVIDER_AUTHORITY,
      new File(uri.getPath()));
  }


  private void saveImage() {
    final String fileName = System.currentTimeMillis() + ".png";
    final boolean hasStoragePermission =
      ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    if (hasStoragePermission || isSdkHigherThan28()) {
      showLoading("Saving...");
      mSaveFileHelper.createFile(fileName, (fileCreated, filePath, error, uri) -> {
        if (fileCreated) {
          SaveSettings saveSettings = new SaveSettings.Builder()
            .setClearViewsEnabled(true)
            .setTransparencyEnabled(true)
            .build();

          mPhotoEditor.saveAsFile(filePath, saveSettings, new PhotoEditor.OnSaveListener() {
            @Override
            public void onSuccess(@NonNull String imagePath) {
              mSaveFileHelper.notifyThatFileIsNowPubliclyAvailable(getContentResolver());
              hideLoading();
              showSnackbar("Image Saved Successfully");
              mSaveImageUri = uri;
              mPhotoEditorView.getSource().setImageURI(mSaveImageUri);
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
              hideLoading();
              showSnackbar("Failed to save Image");
            }
          });

        } else {
          hideLoading();
          showSnackbar(error);
        }
      });
    } else {
      requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
  }

  @Override
  public void onColorChanged(int colorCode) {
    mPhotoEditor.setShape(mShapeBuilder.withShapeColor(colorCode));
    mTxtCurrentTool.setText(R.string.label_brush);
  }

  @Override
  public void onOpacityChanged(int opacity) {
    mPhotoEditor.setShape(mShapeBuilder.withShapeOpacity(opacity));
    mTxtCurrentTool.setText(R.string.label_brush);
  }

  @Override
  public void onShapeSizeChanged(int shapeSize) {
    mPhotoEditor.setShape(mShapeBuilder.withShapeSize(shapeSize));
    mTxtCurrentTool.setText(R.string.label_brush);
  }

  @Override
  public void onShapePicked(ShapeType shapeType) {
    mPhotoEditor.setShape(mShapeBuilder.withShapeType(shapeType));
  }

  @Override
  public void onEmojiClick(String emojiUnicode) {
    mPhotoEditor.addEmoji(emojiUnicode);
    mTxtCurrentTool.setText(R.string.label_emoji);
  }

  @Override
  public void onStickerClick(Bitmap bitmap) {
    mPhotoEditor.addImage(bitmap);
    mTxtCurrentTool.setText(R.string.label_sticker);
  }


  private void showSaveDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(getString(R.string.msg_save_image));
    builder.setPositiveButton("Save", (dialog, which) -> saveImage());
    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
    builder.setNeutralButton("Discard", (dialog, which) -> finish());
    builder.create().show();

  }

  @Override
  public void onFilterSelected(PhotoFilter photoFilter) {
    mPhotoEditor.setFilterEffect(photoFilter);
  }

  @Override
  public void onToolSelected(ToolType toolType) {
    switch (toolType) {
      case SHAPE:
        mPhotoEditor.setBrushDrawingMode(true);
        mShapeBuilder = new ShapeBuilder();
        mPhotoEditor.setShape(mShapeBuilder);
        mTxtCurrentTool.setText(R.string.label_shape);
        showBottomSheetDialogFragment(mShapeBSFragment);
        break;
      case TEXT:
        TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this);
        textEditorDialogFragment.setOnTextEditorListener((inputText, colorCode) -> {
          final TextStyleBuilder styleBuilder = new TextStyleBuilder();
          styleBuilder.withTextColor(colorCode);

          mPhotoEditor.addText(inputText, styleBuilder);
          mTxtCurrentTool.setText(R.string.label_text);
        });
        break;
      case ERASER:
        mPhotoEditor.brushEraser();
        mTxtCurrentTool.setText(R.string.label_eraser_mode);
        break;
      case FILTER:
        mTxtCurrentTool.setText(R.string.label_filter);
        showFilter(true);
        break;
      case EMOJI:
        showBottomSheetDialogFragment(mEmojiBSFragment);
        break;
      case STICKER:
        showBottomSheetDialogFragment(mStickerBSFragment);
        break;
    }
  }

  private void showBottomSheetDialogFragment(BottomSheetDialogFragment fragment) {
    if (fragment == null || fragment.isAdded()) {
      return;
    }
    fragment.show(getSupportFragmentManager(), fragment.getTag());
  }


  void showFilter(boolean isVisible) {
    mIsFilterVisible = isVisible;
    mConstraintSet.clone(mRootView);

    if (isVisible) {
      mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.START);
      mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
        ConstraintSet.PARENT_ID, ConstraintSet.START);
      mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.END,
        ConstraintSet.PARENT_ID, ConstraintSet.END);
    } else {
      mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
        ConstraintSet.PARENT_ID, ConstraintSet.END);
      mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.END);
    }

    ChangeBounds changeBounds = new ChangeBounds();
    changeBounds.setDuration(350);
    changeBounds.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
    TransitionManager.beginDelayedTransition(mRootView, changeBounds);

    mConstraintSet.applyTo(mRootView);
  }

  @Override
  public void onBackPressed() {
    if (mIsFilterVisible) {
      showFilter(false);
      mTxtCurrentTool.setText(R.string.app_name);
    } else if (!mPhotoEditor.isCacheEmpty()) {
      showSaveDialog();
    } else {
      super.onBackPressed();
    }
  }
}
