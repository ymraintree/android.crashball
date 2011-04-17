package org.crashball

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.util.Log

//object Pad {
//  final val HEIGHT: Int = 10
//  final val WIDE: Int = 80
//  final val WIDE_BLOCK: Int = WIDE / 5
//  final val HALF_WIDE: Int = WIDE / 2
//}

class Pad extends ActiveObject {
  final val HEIGHT: Int = 10
  final val WIDE: Int = 80
  final val WIDE_BLOCK: Int = WIDE / 5
  final val HALF_WIDE: Int = WIDE / 2

  private var mScreenWide: Int = 100
  var x: Float = .0f
  var y: Float = .0f
  private var mTouchX: Float = 100

  def this(w: Int, h: Int) {
    this ()
    mScreenWide = w
    y = h * 5 / 6
    Log.d(this.getClass.getSimpleName, "Pad constructor mScreenWide=" + mScreenWide + " y=" + y)
  }

  def getlx: Float = {
    return (x + WIDE)
  }

  def getly: Float = {
    return (y + HEIGHT)
  }

  def colArea(pointX: Float): Int = {
    if (pointX < x) {
      return 0
    }
    else {
      return (pointX - x).asInstanceOf[Int] / WIDE_BLOCK + 1
    }
  }

  def onTouchEvent(event: MotionEvent): Unit = {
    if (event.getAction == MotionEvent.ACTION_MOVE) {
      mTouchX = event.getX
    }
  }

  def update: Unit = {
    x = mTouchX - HALF_WIDE
    if (x < 0) {
      x = 0
    }
    else if (getlx > mScreenWide) {
      x = mScreenWide - WIDE
    }
  }

  def setSize(w: Int, h: Int): Unit = {
    mScreenWide = w
  }

  def draw(canvas: Canvas, paint: Paint): Unit = {
    paint.setColor(Color.YELLOW)
    canvas.drawRect(x, y, x + WIDE, y + HEIGHT, paint)
  }

  @Override def getRect: Rect = {
    return (new Rect(x.asInstanceOf[Int], y.asInstanceOf[Int], x.asInstanceOf[Int] + WIDE, y.asInstanceOf[Int] + HEIGHT))
  }
}