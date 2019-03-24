package jeongari.com.lusmile

import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jeongari.com.camera.Camera2BasicFragment


class CameraFragment : Camera2BasicFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View? {
        val view = inflateFragment(R.id.layoutFrame, inflater, container)
        return view
    }

    override fun detectFace() {}

    companion object {

        val TAG = "CameraFragment"

        fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }
}
