package org.crashball

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

object Brick {
  var HEIGHT: Int = 20
  var WIDE: Int = 32

  def setWidth(w:Int) { WIDE  = w / 10 }
  def setHeight(h:Int) { HEIGHT  = h * 6 / 8 / 2 / 10 }
}

abstract class Brick extends ActiveObject {
  def this(x: Int, y: Int) {
    this ()
    this.x = x * Brick.WIDE
    this.y = y * Brick.HEIGHT + CrashBallView.STATE
    this.ly = this.y + Brick.HEIGHT
    this.lx = this.x + Brick.WIDE
  }

  def getRect: Rect = {
    return (new Rect(x, y, lx, ly))
  }

  def update: Unit = {
  }

  def draw(canvas: Canvas, paint: Paint): Unit = {
    canvas.drawRect(x, y, lx - 1, ly - 1, paint)
  }

  def crash(ball: Ball, crashPoint: Int): Boolean = {
    return true
  }

  var x: Int = 0
  var y: Int = 0
  var lx: Int = 0
  var ly: Int = 0
}