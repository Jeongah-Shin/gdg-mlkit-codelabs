package jeongari.com.lusmile

import android.graphics.Bitmap
import android.media.ImageReader
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import jeongari.com.camera.Camera2BasicFragment
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDateTime


class CameraFragment : Camera2BasicFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View? {
        val view = inflateFragment(R.layout.fragment_camera, inflater, container)
        return view
    }

    override fun detectFace() {
        val bitmap = textureView?.getBitmap(480, 360)
        if (bitmap != null){

            val byteArray = ImageUtils.getNV21(480, 360, bitmap)

            val metadata = FirebaseVisionImageMetadata.Builder()
                .setWidth(480) // 480x360 is typically sufficient for
                .setHeight(360) // image recognition
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(0)
                .build()

            val realTimeOpts = FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build()

            val detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(realTimeOpts)

            val image = FirebaseVisionImage.fromByteArray(byteArray, metadata)

            val result = detector.detectInImage(image)
                .addOnSuccessListener { faces ->
                    for (face in faces) {
                        val bounds = face.boundingBox
                        if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            val smileProb = face.smilingProbability
                            showTextview(bounds.toShortString() + "\n smile probability" + smileProb.toString())
                        }
                    }

                    bitmap.recycle()
                }
                .addOnCanceledListener {
                    showTextview("cancel")
                }
                .addOnFailureListener(
                    object : OnFailureListener {
                        override fun onFailure(e: Exception) {
                            showTextview("fail")
                        }
                    })
        }
    }

    companion object {

        fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }
}
