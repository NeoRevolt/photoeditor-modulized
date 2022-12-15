package ja.burhanrashid52.photoeditor

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.example.photoeditor_module.R
import com.example.photoeditor_module.burhanrashid52.photoeditor.*
import com.example.photoeditor_module.burhanrashid52.photoeditor.Graphic
import com.example.photoeditor_module.burhanrashid52.photoeditor.GraphicManager
import com.example.photoeditor_module.burhanrashid52.photoeditor.MultiTouchListener
import com.example.photoeditor_module.burhanrashid52.photoeditor.PhotoEditorViewState

/**
 * Created by Burhanuddin Rashid on 14/05/21.
 *
 * @author <https:></https:>//github.com/example>
 */
internal class Emoji(
    private val mPhotoEditorView: PhotoEditorView,
    private val mMultiTouchListener: MultiTouchListener,
    private val mViewState: PhotoEditorViewState,
    graphicManager: GraphicManager?,
    private val mDefaultEmojiTypeface: Typeface?
) : Graphic(
    context = mPhotoEditorView.context,
    graphicManager = graphicManager,
    viewType = ViewType.EMOJI,
    layoutId = R.layout.view_photo_editor_text
) {
    private var txtEmoji: TextView? = null
    fun buildView(emojiTypeface: Typeface?, emojiName: String?) {
        txtEmoji?.apply {
            if (emojiTypeface != null) {
                typeface = emojiTypeface
            }
            textSize = 56f
            text = emojiName
        }
    }

    private fun setupGesture() {
        val onGestureControl = buildGestureController(mPhotoEditorView, mViewState)
        mMultiTouchListener.setOnGestureControl(onGestureControl)
        val rootView = rootView
        rootView.setOnTouchListener(mMultiTouchListener)
    }

    override fun setupView(rootView: View) {
        txtEmoji = rootView.findViewById(R.id.tvPhotoEditorText)
        txtEmoji?.run {
            if (mDefaultEmojiTypeface != null) {
                typeface = mDefaultEmojiTypeface
            }
            gravity = Gravity.CENTER
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    init {
        setupGesture()
    }
}