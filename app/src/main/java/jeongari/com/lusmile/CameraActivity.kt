package jeongari.com.lusmile

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import jeongari.com.lusmile.camera.Camera2BasicFragment

class CameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        if (null == savedInstanceState) {
            fragmentManager
                .beginTransaction()
                .replace(R.id.container, Camera2BasicFragment.newInstance())
                .commit()
        }
    }
}
