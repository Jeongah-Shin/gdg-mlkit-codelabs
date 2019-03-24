package jeongari.com.lusmile

import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import jeongari.com.camera.Camera2BasicFragment


class CameraFragment : Camera2BasicFragment() {

    private var byteArray: ByteArray? = null

    private val metadata: FirebaseVisionImageMetadata by lazy {
        FirebaseVisionImageMetadata.Builder()
            .setWidth(textureView!!.width) // 480x360 is typically sufficient for
            .setHeight(textureView!!.height) // image recognition
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
            .setRotation(0)
            .build()

    }
    private val realTimeOpts: FirebaseVisionFaceDetectorOptions by lazy {
        FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()
    }
    private val detector: FirebaseVisionFaceDetector by lazy {
        FirebaseVision.getInstance()
            .getVisionFaceDetector(realTimeOpts)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View? {
        val view = inflateFragment(R.id.layoutFrame, inflater, container)
        return view
    }

    override fun detectFace() {
        val bitmap = textureView?.getBitmap(textureView!!.width, textureView!!.height)
        if (bitmap != null) {
            byteArray = getYV12ByteArray(textureView!!.width, textureView!!.height, bitmap)
            bitmap.recycle()

            val image = FirebaseVisionImage.fromByteArray(byteArray!!, metadata)

        }
    }

    private fun getYV12ByteArray(inputWidth: Int, inputHeight: Int, bitmap: Bitmap): ByteArray {
        val start_time = System.currentTimeMillis()
        val argb = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        val yuv = ByteArray(inputWidth * inputHeight * 3 / 2)
        encodeYV12(yuv, argb, inputWidth, inputHeight)
        bitmap.recycle()
        val end_time = System.currentTimeMillis()
        Log.d("RGBA to YV12", (end_time - start_time).toString() + " ms")
        return yuv
    }

    companion object {

        val TAG = "CameraFragment"

        fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }
}
