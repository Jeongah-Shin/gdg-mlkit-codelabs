package jeongari.com.lusmile

import android.app.Application
import android.content.Context
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

open class BaseApplication : Application() {

    private lateinit var refWatcher : RefWatcher

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        refWatcher = LeakCanary.install(this)
    }

    companion object {
        fun getRefWatcher(context:Context):RefWatcher {
            val application = context.getApplicationContext() as BaseApplication
            return application.refWatcher
        }
    }
}