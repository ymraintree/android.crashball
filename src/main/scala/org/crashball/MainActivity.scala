package org.crashball

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.view.{View, MotionEvent, Window}
import android.util.Log

//object MainActivity {
//  /**Called when the activity is first created. */
//}

class MainActivity extends Activity {
  private var ICICLE_KEY = "CRASH_BALL"
  private var mView: CrashBallView = null

  @Override override def onCreate(icicle: Bundle): Unit = {
    super.onCreate(icicle)
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    setContentView(R.layout.main)
    mView = findViewById(R.id.ball).asInstanceOf[CrashBallView]
    val display = getWindowManager.getDefaultDisplay
    Log.d(this.getClass.getSimpleName, "onCreate width=" + display.getWidth() + " height=" + display.getHeight())
    mView.w = display.getWidth
    mView.h = display.getHeight
    Brick.setWidth(display.getWidth)
    Brick.setHeight(display.getHeight)
//    mView = new CrashBallView(getApplicationContext)
    mView.setTextView(findViewById(R.id.message).asInstanceOf[TextView])
    mView.setPointTextView(findViewById(R.id.stock_balls).asInstanceOf[TextView])
    if (icicle == null) {
      mView.setMode(CrashBallView.READY)
    }
    else {
      var map: Bundle = icicle.getBundle(ICICLE_KEY)
      if (null != map) {
        mView.restoreState(map)
      }
      else {
        mView.setMode(CrashBallView.READY)
      }
    }
  }

  @Override protected override def onPause: Unit = {
    super.onPause
    mView.setMode(CrashBallView.PAUSE)
  }

  @Override override def onTouchEvent(event: MotionEvent): Boolean = {
    super.onTouchEvent(event)
    return (mView.onTouchEvent(event))
  }

  @Override override def onSaveInstanceState(outState: Bundle): Unit = {
    var icicle: Bundle = new Bundle
    outState.putBundle(ICICLE_KEY, mView.saveState(icicle))
  }
}