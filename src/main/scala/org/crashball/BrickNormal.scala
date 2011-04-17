package org.crashball

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class BrickNormal(x: Int, y: Int) extends Brick(x, y) {
//  def this(x: Int, y: Int) {
//    this ()
////    super(x, y)
//  }

  override def draw(canvas: Canvas, paint: Paint): Unit = {
    paint.setColor(Color.RED)
    super.draw(canvas, paint)
  }
}