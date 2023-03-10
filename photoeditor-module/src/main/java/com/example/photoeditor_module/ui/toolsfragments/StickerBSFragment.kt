package com.example.photoeditor_module.ui.toolsfragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.photoeditor_module.R
import com.example.photoeditor_module.data.offline.IconViewModel
import com.example.photoeditor_module.data.offline.entity.IconEntity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StickerBSFragment : BottomSheetDialogFragment() {

//    val listStoryItem = ArrayList<ListStoryItem>()
    private lateinit var mIconViewModel: IconViewModel


    private var mStickerListener: StickerListener? = null
    fun setStickerListener(stickerListener: StickerListener?) {
        mStickerListener = stickerListener
    }

    interface StickerListener {
        fun onStickerClick(bitmap: Bitmap?)
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
//        setStory()
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.fragment_bottom_sticker_emoji_dialog, null)
        dialog.setContentView(contentView)

        val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior
        if (behavior != null && behavior is BottomSheetBehavior<*>) {
//            behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
            behavior.addBottomSheetCallback(mBottomSheetBehaviorCallback)
        }
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
        val rvEmoji: RecyclerView = contentView.findViewById(R.id.rvEmoji)
        val gridLayoutManager = GridLayoutManager(activity, 3)
        rvEmoji.layoutManager = gridLayoutManager
        val stickerAdapter = StickerAdapter()
        rvEmoji.adapter = stickerAdapter
        rvEmoji.setHasFixedSize(true)
//        rvEmoji.setItemViewCacheSize(stickerPathList.size)
//        rvEmoji.setItemViewCacheSize(listStoryItem.size)

        //TODO : Try to fix the logic for sticker
        mIconViewModel = ViewModelProvider(this).get(IconViewModel::class.java)
        if (iconPestList.isNotEmpty()){
            mIconViewModel.deleteIconFromDB()
        }
//        mIconViewModel.deleteIconFromDB()

        mIconViewModel.readAllIcon.observe(this, Observer { icon->
            if (icon.isNullOrEmpty()){
                addIconToDatabase()
                stickerAdapter.setData(icon)
            }
            stickerAdapter.setData(icon)
        })
    }

    private fun addIconToDatabase(){
        mIconViewModel.apply {
            addIcon(IconEntity(null,"Fly","https://cdn-icons-png.flaticon.com/512/2849/2849909.png"));
            addIcon(IconEntity(null,"Ant","https://cdn-icons-png.flaticon.com/512/1850/1850279.png"));
            addIcon(IconEntity(null,"Bug","https://cdn-icons-png.flaticon.com/512/854/854649.png"));
            addIcon(IconEntity(null,"Centipede","https://cdn-icons-png.flaticon.com/512/1850/1850261.png"));
            addIcon(IconEntity(null,"Roach","https://cdn-icons-png.flaticon.com/512/1553/1553874.png"));
            addIcon(IconEntity(null,"Mosquito","https://cdn-icons-png.flaticon.com/512/2865/2865206.png"));
            addIcon(IconEntity(null,"Spider","https://cdn-icons-png.flaticon.com/512/1850/1850190.png"));
            addIcon(IconEntity(null,"Wasp","https://cdn-icons-png.flaticon.com/512/311/311590.png"));
            addIcon(IconEntity(null,"Beetle","https://cdn-icons-png.flaticon.com/512/2975/2975299.png"));
            addIcon(IconEntity(null,"Mouse","https://cdn-icons-png.flaticon.com/512/2297/2297338.png"));
            addIcon(IconEntity(null,"Bat","https://cdn-icons-png.flaticon.com/512/616/616620.png"));
            addIcon(IconEntity(null,"Bird","https://cdn-icons-png.flaticon.com/512/7197/7197073.png"));
        }
        Toast.makeText(requireContext(),"Icons have been added to DB",Toast.LENGTH_SHORT).show()
    }


//    private fun setStory() {
//        val service = ApiConfig.getApiService(this@StickerBSFragment.requireContext()).getAllStory(6, 0)
//        service.enqueue(object : Callback<GetAllStoryResponse> {
//            override fun onResponse(
//                call: Call<GetAllStoryResponse>,
//                response: Response<GetAllStoryResponse>
//            ) {
//                if (response.isSuccessful) {
//                    val responseBody = response.body()
//                    if (responseBody != null && !responseBody.error) {
//                        listStoryItem.clear()
//                        response.body()?.listStory?.let { listStoryItem.addAll(it) }
////                        listStoryItem.addAll(responseBody.listStory)
////                        adapter.setList(response.body()!!.listStory)
//                        Log.d("Sticker Status : ", "Berhasil mendapatkan stiker")
//
//                    } else {
//                        Toast.makeText(
//                            this@StickerBSFragment.requireContext(),
//                            response.message(),
//                            Toast.LENGTH_SHORT
//                        )
//                            .show()
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<GetAllStoryResponse>, t: Throwable) {
//                Toast.makeText(
//                    this@StickerBSFragment.requireContext(),
//                    "Gagal mendapatkan Sticker",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        })
//    }

    inner class StickerAdapter : RecyclerView.Adapter<StickerAdapter.ViewHolder>() {
       private var iconList = emptyList<IconEntity>()

        fun setData(icon: List<IconEntity>){
            this.iconList = icon
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_sticker, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            // Load sticker image from remote url
            Glide.with(requireContext())
                    .asBitmap()
//                    .load(stickerPathList[position])
//                    .load(listStoryItem[position].photoUrl)
                    .load(iconList[position].iconUrl)
                    .into(holder.imgSticker)
        }

        override fun getItemCount(): Int {
//            return stickerPathList.size
            return iconList.size
//            return listStoryItem.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imgSticker: ImageView = itemView.findViewById(R.id.imgSticker)

            init {
                itemView.setOnClickListener {
                    if (mStickerListener != null) {
                        Glide.with(requireContext())
                                .asBitmap()
//                                .load(stickerPathList[layoutPosition])
                                .load(iconList[layoutPosition].iconUrl)
//                            .load(listStoryItem[layoutPosition].photoUrl)
                            .into(object : CustomTarget<Bitmap?>(256, 256) {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                                        mStickerListener!!.onStickerClick(resource)
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {}
                                })
                    }
                    dismiss()
                }
            }
        }

    }

    companion object {
        // Image Urls from flaticon(https://www.flaticon.com/stickers-pack/food-289)
        private val stickerPathList = arrayOf(
                "https://cdn-icons-png.flaticon.com/256/4392/4392452.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392455.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392459.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392462.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392465.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392467.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392469.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392471.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392522.png",
        )
        private val iconPestList = ArrayList<IconEntity>()
    }
}