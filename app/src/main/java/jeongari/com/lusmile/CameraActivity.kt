package jeongari.com.lusmile

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class CameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        if (null == savedInstanceState) {
            fragmentManager
                .beginTransaction()
                .replace(R.id.container, CameraFragment.newInstance())
                .commit()
        }
    }
}
