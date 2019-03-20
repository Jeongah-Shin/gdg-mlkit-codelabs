package jeongari.com.lusmile

import android.media.ImageReader
import android.os.Bundle
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
import java.time.LocalDateTime


class CameraFragment : Camera2BasicFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View? {
        val view = inflateFragment(R.layout.fragment_camera, inflater, container)
        return view
    }

    override fun detectFace() {

        imageReader?.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
            override fun onImageAvailable(reader: ImageReader) {
                val frame = imageReader?.acquireLatestImage()

                if (frame != null){
                    Log.d("frame info", "frame format: " +frame.format + " , frame width/height: " + frame.width +"/" + frame.height)
                    val planes = frame.planes
                    if (planes.size >= 3) {
                        val y = planes[0].buffer
//                        val u = planes[1].buffer
//                        val v = planes[2].buffer
                        val ly = y.remaining()
//                        val lu = u.remaining()
//                        val lv = v.remaining()
//
//                        val dataYUV = ByteArray(ly + lu + lv)
//
//                        y.get(dataYUV, 0, ly)
//                        v.get(dataYUV, ly, lv)
//                        u.get(dataYUV, ly + lv, lu)
//
                        val dataYUV = ImageUtils.YUV_420_888toNV21(frame)

                        val unsignedDataYUV = ByteArray(ly + (frame.width * frame.height / 2))

                        for(i in 0 until dataYUV.size){
                            val byte = Math.abs(dataYUV[i].toInt()).toByte()
                            unsignedDataYUV.set(i, byte)
                        }



                        val metadata = FirebaseVisionImageMetadata.Builder()
                            .setWidth(480) // 480x360 is typically sufficient for
                            .setHeight(360) // image recognition
                            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
                            .setRotation(0)
                            .build()

                        val realTimeOpts = FirebaseVisionFaceDetectorOptions.Builder()
                            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                            .build()

                        val detector = FirebaseVision.getInstance()
                            .getVisionFaceDetector(realTimeOpts)

//                        val image = FirebaseVisionImage.fromByteArray(unsignedDataYUV, metadata)
                        val image = FirebaseVisionImage.fromBitmap(textureView!!.getBitmap(480,360))

                        val result = detector.detectInImage(image)
                            .addOnSuccessListener { faces ->
                                for (face in faces) {
                                    val bounds = face.boundingBox
                                    if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                        val smileProb = face.smilingProbability
                                        showTextview(bounds.toShortString() + "\n smile probability" + smileProb.toString())
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
                    }
                    frame.close()
                }
            }
        },null)
    }

    companion object {

        fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }
}
