package jeongari.com.lusmile

import android.graphics.Bitmap
import android.graphics.RectF
import android.media.ImageReader
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.LottieAnimationView
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

    private var ltViewHappy : LottieAnimationView ?= null
    private var ltViewUpset : LottieAnimationView ?= null

    private var byteArray : ByteArray ?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View? {
        val view = inflateFragment(R.id.layoutFrame, inflater, container)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ltViewHappy = view.findViewById(R.id.ltViewHappy)

        ltViewHappy?.visibility = View.INVISIBLE

        ltViewHappy?.speed = 5.0f
        ltViewUpset?.speed = 5.0f
    }

    override fun detectFace() {
        val bitmap = textureView?.getBitmap(textureView!!.width, textureView!!.height)
        if (bitmap != null){
            byteArray = ImageUtils.getYV12(textureView!!.width, textureView!!.height, bitmap)

//            activity?.runOnUiThread{
//                drawView?.setImgSize(textureView!!.width, textureView!!.height)
//            }

            val metadata = FirebaseVisionImageMetadata.Builder()
                .setWidth(textureView!!.width) // 480x360 is typically sufficient for
                .setHeight(textureView!!.height) // image recognition
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
                .setRotation(0)
                .build()

            val realTimeOpts = FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build()

            val detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(realTimeOpts)

            val image = FirebaseVisionImage.fromByteArray(byteArray!!, metadata)

            val result = detector.detectInImage(image)
                .addOnSuccessListener { faces ->
                    for (face in faces) {
                        val bounds = face.boundingBox
                        val boundWidth = bounds.right - bounds.left
                        val boundHeight = bounds.bottom - bounds.top
//                        drawView!!.setDrawPoint(RectF(bounds), 1f)
                        if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            val smileProb = face.smilingProbability
                            if(smileProb > 0.7f){
                                activity?.runOnUiThread {
                                    ltViewHappy?.visibility = View.VISIBLE
                                    ltViewHappy?.layoutParams?.width = boundWidth
                                    ltViewHappy?.layoutParams?.height = boundWidth
                                    ltViewHappy?.x = bounds.left.toFloat()
                                    ltViewHappy?.y = bounds.top.toFloat() - boundWidth

                                    ltViewHappy?.requestLayout()

                                    if(ltViewHappy?.isAnimating != true)
                                        ltViewHappy?.playAnimation()
                                }
                                showImageview(resources.getDrawable(R.drawable.ic_calm))

                            }else{
                                activity?.runOnUiThread {
                                    ltViewHappy?.visibility = View.INVISIBLE
                                    if(ltViewHappy!!.isAnimating){
                                        ltViewHappy?.cancelAnimation()
                                    }

                                }
                                showImageview(resources.getDrawable(R.drawable.ic_sad))
                            }
                            showTextview("Smiling Probability Estimation : " + (smileProb*100).toFloat() + " %")
                        }
                    }
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
            drawView?.invalidate()
        }
        bitmap?.recycle()
    }

    fun cancelAnimation(){
        if (ltViewHappy!!.isAnimating){
            ltViewHappy!!.cancelAnimation()
        }
        if(ltViewUpset!!.isAnimating){
            ltViewUpset!!.cancelAnimation()
        }
    }

    companion object {

        fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }
}
