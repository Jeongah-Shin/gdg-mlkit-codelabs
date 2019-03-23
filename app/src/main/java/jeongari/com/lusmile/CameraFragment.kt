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
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import jeongari.com.camera.Camera2BasicFragment
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDateTime


class CameraFragment : Camera2BasicFragment() {

    private var ltViewHappy: LottieAnimationView? = null
    private var ltViewUpset: LottieAnimationView? = null

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ltViewHappy = view.findViewById(R.id.ltViewHappy)

        ltViewHappy?.visibility = View.INVISIBLE

        ltViewHappy?.speed = 20.0f
    }

    override fun detectFace() {
        val bitmap = textureView?.getBitmap(textureView!!.width, textureView!!.height)
        if (bitmap != null) {
            byteArray = getYV12(textureView!!.width, textureView!!.height, bitmap)
            bitmap.recycle()

//            activity?.runOnUiThread{
//                drawView?.setImgSize(textureView!!.width, textureView!!.height)
//            }

            val image = FirebaseVisionImage.fromByteArray(byteArray!!, metadata)

            val startTime = System.currentTimeMillis()
            detector.detectInImage(image)
                .addOnCompleteListener {
                    val endTime = System.currentTimeMillis()
                    Log.d("MLKit Face Detection", (endTime - startTime).toString() + "ms")
                }
                .addOnSuccessListener { faces ->
                    if (faces.isEmpty()) {
                        showTextview("No Face deteced")
                    } else {
                        for (face in faces) {
                            val bounds = face.boundingBox
                            val boundWidth = (bounds.right - bounds.left)
                            val boundHeight = (bounds.bottom - bounds.top)
//                        drawView!!.setDrawPoint(RectF(bounds), 1f)
                            if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                val smileProb = face.smilingProbability
                                if (smileProb > 0.7f) {
                                    activity?.runOnUiThread {
                                        ltViewHappy?.visibility = View.VISIBLE
                                        ltViewHappy?.layoutParams?.width = boundWidth
                                        ltViewHappy?.layoutParams?.height = boundWidth
                                        ltViewHappy?.x = bounds.left.toFloat()
                                        ltViewHappy?.y = bounds.top.toFloat() - boundWidth

                                        ltViewHappy?.requestLayout()
                                    }
                                    if (ltViewHappy?.isAnimating != true)
                                        ltViewHappy?.playAnimation()
                                    showImageview(resources.getDrawable(R.drawable.ic_calm))

                                } else {
                                    activity?.runOnUiThread {
                                        ltViewHappy?.visibility = View.INVISIBLE
                                    }
                                    if (ltViewHappy!!.isAnimating) {
                                        ltViewHappy?.cancelAnimation()
                                    }
                                    showImageview(resources.getDrawable(R.drawable.ic_sad))
                                }
                                showTextview("Smiling Probability Estimation : " + (smileProb * 100).toFloat() + " %")
                            }
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
            //drawView?.invalidate()
        }
    }

    fun getYV12(inputWidth: Int, inputHeight: Int, scaled: Bitmap): ByteArray {
        val start_time = System.currentTimeMillis()
        val argb = IntArray(inputWidth * inputHeight)
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        val yuv = ByteArray(inputWidth * inputHeight * 3 / 2)
        encodeYV12(yuv, argb, inputWidth, inputHeight)
        scaled.recycle()
        val end_time = System.currentTimeMillis()
        Log.d("RGBA to YV12", (end_time - start_time).toString() + " ms")
        return yuv
    }

    fun encodeYV12(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uIndex = frameSize
        var vIndex = frameSize + (frameSize / 4)
        var a: Int
        var R: Int
        var G: Int
        var B: Int
        var Y: Int
        var U: Int
        var V: Int
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                a = (argb[index] and -0x1000000) shr 24 // a is not used obviously
                R = (argb[index] and 0xff0000) shr 16
                G = (argb[index] and 0xff00) shr 8
                B = (argb[index] and 0xff) shr 0
                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) shr 8) + 16
                U = ((-38 * R - 74 * G + 112 * B + 128) shr 8) + 128
                V = ((112 * R - 94 * G - 18 * B + 128) shr 8) + 128
                // YV12 has a plane of Y and two chroma plans (U, V) planes each sampled by a factor of 2
                // meaning for every 4 Y pixels there are 1 V and 1 U. Note the sampling is every other
                // pixel AND every other scanline.
                yuv420sp[yIndex++] = (if ((Y < 0)) 0 else (if ((Y > 255)) 255 else Y)).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uIndex++] = (if ((V < 0)) 0 else (if ((V > 255)) 255 else V)).toByte()
                    yuv420sp[vIndex++] = (if ((U < 0)) 0 else (if ((U > 255)) 255 else U)).toByte()
                }
                index++
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        BaseApplication.getRefWatcher(activity).watch(this)
    }

    companion object {

        fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }
}
