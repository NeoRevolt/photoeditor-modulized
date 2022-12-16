package com.example.photoeditor_module

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.photoediting.filters.FilterViewAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import java.io.IOException
import java.lang.Exception
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModelProvider
import com.example.photoeditor_module.base.BaseActivity
import com.example.photoeditor_module.burhanrashid52.photoeditor.*
import com.example.photoeditor_module.burhanrashid52.photoeditor.shape.ShapeBuilder
import com.example.photoeditor_module.burhanrashid52.photoeditor.shape.ShapeType
import com.example.photoeditor_module.data.offline.LayoutViewModel
import com.example.photoeditor_module.data.offline.entity.TransactionEntity
import com.example.photoeditor_module.filters.FilterListener
import com.example.photoeditor_module.tools.EditingToolsAdapter
import com.example.photoeditor_module.tools.ToolType
import com.example.photoeditor_module.ui.toolsfragments.*
import com.example.photoeditor_module.utils.FileSaveHelper
import com.example.photoeditor_module.utils.uriToFile
import java.util.concurrent.Executors

class EditImageActivity : BaseActivity(), OnPhotoEditorListener, View.OnClickListener,
    PropertiesBSFragment.Properties, ShapeBSFragment.Properties, EmojiBSFragment.EmojiListener,
    StickerBSFragment.StickerListener,
    EditingToolsAdapter.OnItemSelected, FilterListener {

    //Init Variables
    var mPhotoEditor: PhotoEditor? = null
    private var mPhotoEditorView: PhotoEditorView? = null
    private var mPropertiesBSFragment: PropertiesBSFragment? = null
    private var mShapeBSFragment: ShapeBSFragment? = null
    private var mShapeBuilder: ShapeBuilder? = null
    private var mEmojiBSFragment: EmojiBSFragment? = null
    private var mStickerBSFragment: StickerBSFragment? = null
    private var mTxtCurrentTool: TextView? = null
    private var mWonderFont: Typeface? = null
    private var mRvTools: RecyclerView? = null
    private var mRvFilters: RecyclerView? = null
    private val mEditingToolsAdapter = EditingToolsAdapter(this)
    private val mFilterViewAdapter = FilterViewAdapter(this)
    private var mRootView: ConstraintLayout? = null
    private val mConstraintSet = ConstraintSet()
    private var mIsFilterVisible = false

    private var getFile: File? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var mTransactions: LayoutViewModel

    @VisibleForTesting
    var mSaveImageUri: Uri? = null
    private var mSaveFileHelper: FileSaveHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeFullScreen()
        setContentView(R.layout.activity_edit_image)

        progressBar = findViewById(R.id.progress_bar)

        initViews()
        handleIntentImage(mPhotoEditorView?.source)
        // TODO : This Wonder Font are optional
//        mWonderFont = Typeface.createFromAsset(assets, "beyond_wonderland.ttf")
        mPropertiesBSFragment = PropertiesBSFragment()
        // TODO : This EmojiFragment() still has some bug, the emoji is still null
//        mEmojiBSFragment = EmojiBSFragment()
        mStickerBSFragment = StickerBSFragment()
        mShapeBSFragment = ShapeBSFragment()
        mStickerBSFragment?.setStickerListener(this)
        mEmojiBSFragment?.setEmojiListener(this)
        mPropertiesBSFragment?.setPropertiesChangeListener(this)
        mShapeBSFragment?.setPropertiesChangeListener(this)
        val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvTools?.layoutManager = llmTools
        mRvTools?.adapter = mEditingToolsAdapter
        val llmFilters = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvFilters?.layoutManager = llmFilters
        mRvFilters?.adapter = mFilterViewAdapter

        // NOTE(lucianocheng): Used to set integration testing parameters to PhotoEditor
        val pinchTextScalable = intent.getBooleanExtra(PINCH_TEXT_SCALABLE_INTENT_KEY, true)

        // NOTE Get Image URL from Detail Activity
        val photoUrl = intent.getStringExtra(EXTRA_PHOTO)
        val requestCode = intent.getStringExtra(EXTRA_REQ)

        //Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);
//        Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");
        mPhotoEditor = mPhotoEditorView?.run {
            PhotoEditor.Builder(this@EditImageActivity, this)
                .setPinchTextScalable(pinchTextScalable) // set flag to make text scalable when pinch
                //.setDefaultTextTypeface(mTextRobotoTf)
                //.setDefaultEmojiTypeface(mEmojiTypeFace)
                .build() // build photo editor sdk
        }
        mPhotoEditor?.setOnPhotoEditorListener(this)

        //TODO NOTE : Langsung mengambil gambar dari gallery
        if (requestCode == "gallery") {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_REQUEST)
            showLoading(false)
        } else if (requestCode == "remote"){
            val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())
            var image: Bitmap? = null
            executor.execute {
                try {
                    showLoading(true)
                    val `in` = java.net.URL(photoUrl).openStream()
                    image = BitmapFactory.decodeStream(`in`)
                    handler.post{
                        showLoading(false)
                        mPhotoEditorView?.source?.setImageBitmap(image)
                    }
                }
                catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
        showLoading(false)
        mPhotoEditorView?.source?.setImageResource(R.drawable.blank_image)
        mSaveFileHelper = FileSaveHelper(this)
    }

//    private fun uploadImage(){
//        mTransactions = ViewModelProvider(this).get(LayoutViewModel::class.java)
//        if (getFile != null){
//            val file = reduceFileImage(getFile as File)
//            val isi = "no capt"
//
//            val description = isi.toRequestBody("text/plain".toMediaType())
//            val requestImageFile = file.asRequestBody("image/png".toMediaTypeOrNull())
//            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
//                "photo",
//                file.name,
//                requestImageFile
//            )
//            showLoading(true)
//            val service = ApiConfig.getApiService(this).uploadStory(imageMultipart, description)
//            service.enqueue(object : Callback<AddNewStoryResponse>{
//                override fun onResponse(
//                    call: Call<AddNewStoryResponse>,
//                    response: Response<AddNewStoryResponse>
//                ) {
//                    if (response.isSuccessful) {
//                        showLoading(false)
//                        val responseBody = response.body()
//                        if (responseBody != null && !responseBody.error) {
//                            Toast.makeText(
//                                this@EditImageActivity,
//                                responseBody.message,
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            Intent(this@EditImageActivity, RemoteImagesActivity::class.java).also {
//                                startActivity(it)
//                                finish()
//                            }
//                            mTransactions.addTransaction(TransactionEntity(file.toString(),file.name,null))
//                        }
//                    } else {
//                        showLoading(false)
//                        Toast.makeText(this@EditImageActivity, response.message(), Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                }
//
//                override fun onFailure(call: Call<AddNewStoryResponse>, t: Throwable) {
//                    Toast.makeText(
//                        this@EditImageActivity,
//                        "Failed to connect",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            })
//        }else {
//            Toast.makeText(
//                this,
//                "Please save the image first",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }

    private fun handleIntentImage(source: ImageView?) {
        if (intent == null) {
            return;
        }

        when (intent.action) {
            Intent.ACTION_EDIT, ACTION_NEXTGEN_EDIT -> {
                try {
                    val uri = intent.data
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver, uri
                    )
                    source?.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            else -> {
                val intentType = intent.type
                if (intentType != null && intentType.startsWith("image/")) {
                    val imageUri = intent.data
                    if (imageUri != null) {
                        source?.setImageURI(imageUri)
                    }
                }
            }
        }
    }

    private fun initViews() {
        mPhotoEditorView = findViewById(R.id.photoEditorView)
        mTxtCurrentTool = findViewById(R.id.txtCurrentTool)
        mRvTools = findViewById(R.id.rvConstraintTools)
        mRvFilters = findViewById(R.id.rvFilterView)
        mRootView = findViewById(R.id.rootView)

        val imgUndo: ImageView = findViewById(R.id.imgUndo)
        imgUndo.setOnClickListener(this)
        val imgRedo: ImageView = findViewById(R.id.imgRedo)
        imgRedo.setOnClickListener(this)
        val imgCamera: ImageView = findViewById(R.id.imgCamera)
        imgCamera.setOnClickListener(this)
        val imgGallery: ImageView = findViewById(R.id.imgGallery)
        imgGallery.setOnClickListener(this)
        val imgSave: ImageView = findViewById(R.id.imgSave)
        imgSave.setOnClickListener(this)
        val imgClose: ImageView = findViewById(R.id.imgClose)
        imgClose.setOnClickListener(this)
        val imgShare: ImageView = findViewById(R.id.imgShare)
        imgShare.setOnClickListener(this)
    }

    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
        val textEditorDialogFragment = TextEditorDialogFragment.show(this, text.toString(), colorCode)
        textEditorDialogFragment.setOnTextEditorListener (object : TextEditorDialogFragment.TextEditorListener {
            override fun onDone(inputText: String?, colorCode: Int) {
                val styleBuilder = TextStyleBuilder()
                styleBuilder.withTextColor(colorCode)
                if (rootView != null) {
                    mPhotoEditor?.editText(rootView, inputText, styleBuilder)
                }
                mTxtCurrentTool?.setText(R.string.label_text)
            }
        })
    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d(TAG, "onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]")
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d(TAG, "onRemoveViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]")
    }

    override fun onStartViewChangeListener(viewType: ViewType?) {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onStopViewChangeListener(viewType: ViewType?) {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onTouchSourceImage(event: MotionEvent?) {
        Log.d(TAG, "onTouchView() called with: event = [$event]")
    }

    @SuppressLint("NonConstantResourceId", "MissingPermission")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgUndo -> mPhotoEditor?.undo()
            R.id.imgRedo -> mPhotoEditor?.redo()
            R.id.imgSave -> saveImage()
            R.id.imgClose -> onBackPressed()
            R.id.imgShare -> sharePicture()
            R.id.imgCamera -> {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            }
            R.id.imgGallery -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_REQUEST)
            }
        }
    }

    // TODO : Share to different app
    private fun shareImage() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        val saveImageUri = mSaveImageUri
        if (saveImageUri == null) {
            showSnackbar(getString(R.string.msg_save_image_to_share))
            return
        }
        intent.putExtra(Intent.EXTRA_STREAM, buildFileProviderUri(saveImageUri))
        startActivity(Intent.createChooser(intent, getString(R.string.msg_share_image)))
    }

    private fun buildFileProviderUri(uri: Uri): Uri {
        if (FileSaveHelper.isSdkHigherThan28()) {
            return uri
        }
        val path: String = uri.path ?: throw IllegalArgumentException("URI Path Expected")

        return FileProvider.getUriForFile(
            this,
            FILE_PROVIDER_AUTHORITY,
            File(path)
        )
    }


    // TODO : Coba agar bisa save ke gallery atau remote
    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    private fun saveImage() {
        val fileName = System.currentTimeMillis().toString() + ".png"
        val hasStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasStoragePermission || FileSaveHelper.isSdkHigherThan28()) {
            showLoading("Saving...")
            mSaveFileHelper?.createFile(fileName, object : FileSaveHelper.OnFileCreateResult {

                @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
                override fun onFileCreateResult(
                    created: Boolean,
                    filePath: String?,
                    error: String?,
                    uri: Uri?
                ) {
                    if (created && filePath != null) {
                        val saveSettings = SaveSettings.Builder()
                            .setClearViewsEnabled(true)
                            .setTransparencyEnabled(true)
                            .build()

                        mPhotoEditor?.saveAsFile(
                            filePath,
                            saveSettings,
                            object : PhotoEditor.OnSaveListener {
                                override fun onSuccess(imagePath: String) {
                                    mSaveFileHelper?.notifyThatFileIsNowPubliclyAvailable(
                                        contentResolver
                                    )
                                    hideLoading()
                                    showSnackbar("Image Saved Successfully")
                                    mSaveImageUri = uri
                                    mPhotoEditorView?.source?.setImageURI(mSaveImageUri)
                                    val myFile = mSaveImageUri?.let { uriToFile(it,this@EditImageActivity)}
                                    getFile = myFile
                                }

                                override fun onFailure(exception: Exception) {
                                    hideLoading()
                                    showSnackbar("Failed to save Image")
                                }
                            })
                    } else {
                        hideLoading()
                        error?.let { showSnackbar(error) }
                    }
                }
            })
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun sharePicture() {
        val fileName = System.currentTimeMillis().toString() + ".png"
        val hasStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasStoragePermission || FileSaveHelper.isSdkHigherThan28()) {
            showLoading("Saving...")
            mSaveFileHelper?.createFile(fileName, object : FileSaveHelper.OnFileCreateResult {

                @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
                override fun onFileCreateResult(
                    created: Boolean,
                    filePath: String?,
                    error: String?,
                    uri: Uri?
                ) {
                    if (created && filePath != null) {
                        val saveSettings = SaveSettings.Builder()
                            .setClearViewsEnabled(true)
                            .setTransparencyEnabled(true)
                            .build()

                        mPhotoEditor?.saveAsFile(
                            filePath,
                            saveSettings,
                            object : PhotoEditor.OnSaveListener {
                                override fun onSuccess(imagePath: String) {
                                    mSaveFileHelper?.notifyThatFileIsNowPubliclyAvailable(
                                        contentResolver
                                    )
                                    hideLoading()
                                    showSnackbar("Image Saved Successfully")
                                    mSaveImageUri = uri
                                    mPhotoEditorView?.source?.setImageURI(mSaveImageUri)
                                    val myFile = mSaveImageUri?.let { uriToFile(it,this@EditImageActivity)}
                                    getFile = myFile
                                    // TODO: Delete this save to DB when UploadImage() Completed
                                    mTransactions = ViewModelProvider(this@EditImageActivity).get(LayoutViewModel::class.java)
                                    mTransactions.addTransaction(TransactionEntity(getFile.toString(),getFile?.name.toString(),null))
//                                    uploadImage()
                                }

                                override fun onFailure(exception: Exception) {
                                    hideLoading()
                                    showSnackbar("Failed to save Image")
                                }
                            })
                    } else {
                        hideLoading()
                        error?.let { showSnackbar(error) }
                    }
                }
            })
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    mPhotoEditor?.clearAllViews()
                    val photo = data?.extras?.get("data") as Bitmap?
                    mPhotoEditorView?.source?.setImageBitmap(photo)
                }
                PICK_REQUEST -> try {
                    mPhotoEditor?.clearAllViews()
                    val uri = data?.data

                    val selectedImg: Uri = data?.data as Uri
                    val myFile = uriToFile(selectedImg, this)

                    val bitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver, uri
                    )
                    mPhotoEditorView?.source?.setImageBitmap(bitmap)
                    getFile = myFile
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor?.setShape(mShapeBuilder?.withShapeColor(colorCode))
        mTxtCurrentTool?.setText(R.string.label_brush)
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor?.setShape(mShapeBuilder?.withShapeOpacity(opacity))
        mTxtCurrentTool?.setText(R.string.label_brush)
    }

    override fun onShapeSizeChanged(shapeSize: Int) {
        mPhotoEditor?.setShape(mShapeBuilder?.withShapeSize(shapeSize.toFloat()))
        mTxtCurrentTool?.setText(R.string.label_brush)
    }

    override fun onShapePicked(shapeType: ShapeType?) {
        mPhotoEditor?.setShape(mShapeBuilder?.withShapeType(shapeType))
    }

    override fun onEmojiClick(emojiUnicode: String?) {
        mPhotoEditor?.addEmoji(emojiUnicode)
        mTxtCurrentTool?.setText(R.string.label_emoji)
    }

    override fun onStickerClick(bitmap: Bitmap?) {
        mPhotoEditor?.addImage(bitmap)
        mTxtCurrentTool?.setText(R.string.label_sticker)
    }

    @SuppressLint("MissingPermission")
    override fun isPermissionGranted(isGranted: Boolean, permission: String?) {
        if (isGranted) {
            saveImage()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.msg_save_image))
        builder.setPositiveButton("Save") { _: DialogInterface?, _: Int -> saveImage() }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        builder.setNeutralButton("Discard") { _: DialogInterface?, _: Int -> finish() }
        builder.create().show()
    }

    override fun onFilterSelected(photoFilter: PhotoFilter?) {
        mPhotoEditor?.setFilterEffect(photoFilter)
    }

    override fun onToolSelected(toolType: ToolType?) {
        when (toolType) {
            ToolType.SHAPE -> {
                mPhotoEditor?.setBrushDrawingMode(true)
                mShapeBuilder = ShapeBuilder()
                mPhotoEditor?.setShape(mShapeBuilder)
                mTxtCurrentTool?.setText(R.string.label_shape)
                showBottomSheetDialogFragment(mShapeBSFragment)
            }
            ToolType.TEXT -> {
                val textEditorDialogFragment = TextEditorDialogFragment.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object : TextEditorDialogFragment.TextEditorListener {
                    override fun onDone(inputText: String?, colorCode: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        mPhotoEditor?.addText(inputText, styleBuilder)
                        mTxtCurrentTool?.setText(R.string.label_text)
                    }
                })
            }
            ToolType.ERASER -> {
                mPhotoEditor?.brushEraser()
                mTxtCurrentTool?.setText(R.string.label_eraser_mode)
            }
            ToolType.FILTER -> {
                mTxtCurrentTool?.setText(R.string.label_filter)
                showFilter(true)
            }
            ToolType.EMOJI -> showBottomSheetDialogFragment(mEmojiBSFragment)
            ToolType.STICKER -> showBottomSheetDialogFragment(mStickerBSFragment)
            else -> {}
        }
    }

    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(supportFragmentManager, fragment.tag)
    }

    private fun showFilter(isVisible: Boolean) {
        mIsFilterVisible = isVisible
        mConstraintSet.clone(mRootView)
        val rvFilterId: Int = mRvFilters?.id ?: throw IllegalArgumentException("RV Filter ID Expected")
        if (isVisible) {
            mConstraintSet.clear(rvFilterId, ConstraintSet.START)
            mConstraintSet.connect(
                rvFilterId, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START
            )
            mConstraintSet.connect(
                rvFilterId, ConstraintSet.END,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
        } else {
            mConstraintSet.connect(
                rvFilterId, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
            mConstraintSet.clear(rvFilterId, ConstraintSet.END)
        }
        val changeBounds = ChangeBounds()
        changeBounds.duration = 350
        changeBounds.interpolator = AnticipateOvershootInterpolator(1.0f)
        mRootView?.let { TransitionManager.beginDelayedTransition(it, changeBounds) }
        mConstraintSet.applyTo(mRootView)
    }

    override fun onBackPressed() {
        val isCacheEmpty = mPhotoEditor?.isCacheEmpty ?: throw IllegalArgumentException("isCacheEmpty Expected")

        if (mIsFilterVisible) {
            showFilter(false)
            mTxtCurrentTool?.setText(R.string.app_name)
        } else if (!isCacheEmpty) {
            showSaveDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun showLoading(state: Boolean) {
        if (state) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }


    companion object {
        private val TAG = EditImageActivity::class.java.simpleName
        const val FILE_PROVIDER_AUTHORITY = "com.example.photoediting.fileprovider"
        private const val CAMERA_REQUEST = 52
        private const val PICK_REQUEST = 53
        const val ACTION_NEXTGEN_EDIT = "action_nextgen_edit"
        const val PINCH_TEXT_SCALABLE_INTENT_KEY = "PINCH_TEXT_SCALABLE"
        const val EXTRA_PHOTO = "extra_photo"
        const val EXTRA_REQ = "extra_req"
    }
}