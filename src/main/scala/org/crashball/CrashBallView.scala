package org.crashball

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView;

import java.lang.Math
import java.util._
import android.graphics.{Color, Canvas, Paint}
import android.view.{KeyEvent, MotionEvent, View}
import android.os.{Bundle, Handler, Message}

object CrashBallView {
  final val PAUSE: Int = 0
  final val READY: Int = 1
  //  final val RUNNING: Int = 2
  //  final val LOSE: Int = 3
  //  final val CLEAR: Int = 4
  //  private final val BRICK_ROW: Int = 10
  //  private final val BRICK_COL: Int = 10
  final val STATE: Int = 50
  //  private var w: Int = 320
  //  private var h: Int = 480
  //  private final val DELAY_MILLIS: Long = 1000 / 60
}

class CrashBallView(var context: Context, var attrs: AttributeSet) extends View(context, attrs) {

  final val RUNNING: Int = 2
  final val LOSE: Int = 3
  final val CLEAR: Int = 4
  private final val BRICK_ROW: Int = 10
  private final val BRICK_COL: Int = 10
  //  final val STATE: Int = 50
  var w: Int = 320
  var h: Int = 480
  private final val DELAY_MILLIS: Long = 1000 / 60

  private var mMode: Int = CrashBallView.READY
//  private var mMessage: TextView = null
  private var mMessage: TextView = new TextView(context)
//  private var mPoint: TextView = null
  private var mPoint: TextView = new TextView(context)
  private var mFieldHandler: RefreshHandler = new RefreshHandler
  private var mPaint: Paint = new Paint
  private var mPad: Pad = null
  private var mBalls: ArrayList[Ball] = new ArrayList[Ball]
  private var mBricks: Array[Array[Brick]] = new Array[Array[Brick]](BRICK_COL, BRICK_ROW)
  private var mBallsCount: Int = 0
  private var mStockBallCount: Int = 0
  private var mBricksCount: Int = 0
  private var mTouchEvent: MotionEvent = null

  initialProcess

//  def this(context: Context, attrs: AttributeSet) {
//    this (context)
//    //    `super`(context, attrs)
//    Log.d(this.getClass.getSimpleName, "constructor1 called")
//    initialProcess
//  }

  private def initialProcess: Unit = {
    Log.d(this.getClass.getSimpleName, "initialProcess called")
    setFocusable(true)
    Log.d(this.getClass.getSimpleName, "w=" + w + " h=" + h + " " + getMeasuredWidth + " " + getMeasuredHeight)
  }

  private def newGame: Unit = {
    mBalls.clear
    mBricksCount = 0
    mStockBallCount = 5
    mBallsCount = 0
    var i: Int = 0
    while (i < BRICK_COL) {
      var j: Int = 0
      while (j < BRICK_ROW) {
        mBricksCount += 1;
        mBricksCount
        mBricks(i)(j) = new BrickNormal(i, j)
        j += 1;
      }
      i += 1;
    }
    CrashBallView.this.invalidate
  }

  override def onTouchEvent(event: MotionEvent): Boolean = {
    mTouchEvent = event
    var action: Int = event.getAction
    if (action == MotionEvent.ACTION_DOWN) {
      mMode match {
        case CrashBallView.READY =>
          setMode(RUNNING)
          addBall
        case LOSE =>
          setMode(CrashBallView.READY)
        case CLEAR =>
          setMode(CrashBallView.READY)
        case CrashBallView.PAUSE =>
          setMode(RUNNING)
          addBall
        case _ =>
      }
    }
    return true
  }

  def setMode(newMode: Int): Unit = {
    var oldMode: Int = mMode
    mMode = newMode
    if (newMode == RUNNING) {
      if (oldMode == CrashBallView.READY) {
        newGame
        var resource: Resources = getContext.getResources
        var newMessage: CharSequence = resource.getText(R.string.new_ball_help)
        mMessage.setText(newMessage)
        mMessage.setVisibility(View.VISIBLE)
      }
      else if (oldMode == CrashBallView.PAUSE) {
        if (mBallsCount == 0) {
          val resource: Resources = getContext.getResources
          val newMessage: CharSequence = resource.getText(R.string.new_ball_help)
          mMessage.setText(newMessage)
        }
        else {
          mMessage.setVisibility(View.INVISIBLE)
          addBall
        }
      }
      if (oldMode != RUNNING) {
        update
      }
      return
    }
    var newMessage: CharSequence = ""
    var resource: Resources = getContext.getResources
    newMode match {
      case CrashBallView.PAUSE =>
        newMessage = resource.getText(R.string.pause_message)
      case CrashBallView.READY =>
        newMessage = resource.getText(R.string.ready_message)
      case LOSE =>
        newMessage = resource.getText(R.string.game_over_message)
      case CLEAR =>
        newMessage = resource.getText(R.string.game_clear_message)
    }
    Log.d(this.getClass.getSimpleName, "newMessage=" + newMessage)
    mMessage.setText(newMessage)
    mMessage.setVisibility(View.VISIBLE)
  }

  @Override protected override def onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int): Unit = {
    Log.d(this.getClass.getSimpleName, "onSizeChanged called w=" + w + " h=" + h)
    this.w = w
    this.h = h
    mPad = new Pad(w, h)
    setMode(CrashBallView.READY)
  }

  private def isBricksCrash(xIndex: Int, yIndex: Int): Boolean = {
    if (yIndex >= BRICK_ROW || xIndex >= BRICK_COL) {
      return false
    }
    if (mBricks(xIndex)(yIndex) != null) {
      return true
    }
    return false
  }

  private def crashBrick(xIndex: Int, yIndex: Int): Unit = {
    if (yIndex >= BRICK_ROW || xIndex >= BRICK_COL || mBricks(xIndex)(yIndex) == null) {
      return
    }
    CrashBallView.this.invalidate(mBricks(xIndex)(yIndex).getRect)
    mBricks(xIndex)(yIndex) = null
    ({
      mBricksCount -= 1;
      mBricksCount
    })
    if (mBricksCount <= 0) {
      setMode(CLEAR)
    }
  }

  def update: Unit = {
    if (mMode == RUNNING) {
      if (mTouchEvent != null) {
        CrashBallView.this.invalidate(mPad.getRect)
        mPad.onTouchEvent(mTouchEvent)
        mPad.update
        CrashBallView.this.invalidate(mPad.getRect)
      }
      var xCrash: Int = 0
      var yCrash: Int = 0
      var i: Int = 0
      while (i < this.mBallsCount) {
        xCrash = 0
        yCrash = 0
        var ball: Ball = this.mBalls.get(i)
        CrashBallView.this.invalidate(ball.getRect)
        ball.update
        CrashBallView.this.invalidate(ball.getRect)
        var xIndex: Int = (ball.x / Brick.WIDE).asInstanceOf[Int]
        var yIndex: Int = ((ball.y - CrashBallView.STATE) / Brick.HEIGHT).asInstanceOf[Int]
        var lxIndex: Int = (ball.getlx / Brick.WIDE).asInstanceOf[Int]
        var lyIndex: Int = ((ball.getly - CrashBallView.STATE) / Brick.HEIGHT).asInstanceOf[Int]
        if (isBricksCrash(xIndex, yIndex)) {
          xCrash += 1;
          xCrash
          yCrash += 1;
          yCrash
        }
        if (isBricksCrash(lxIndex, yIndex)) {
          xCrash -= 1;
          xCrash
          yCrash += 1;
          yCrash
        }
        if (isBricksCrash(xIndex, lyIndex)) {
          xCrash += 1;
          xCrash
          yCrash -= 1;
          yCrash
        }
        if (isBricksCrash(lxIndex, lyIndex)) {
          xCrash -= 1;
          xCrash
          yCrash -= 1;
          yCrash
        }
        crashBrick(xIndex, yIndex)
        crashBrick(xIndex, lyIndex)
        crashBrick(lxIndex, yIndex)
        crashBrick(lxIndex, lyIndex)
        if (yCrash > 0) {
          ball.topCrash(yIndex)
        } else if (yCrash < 0) {
          ball.downCrash(lyIndex)
        }
        if (xCrash > 0) {
          ball.leftCrash(xIndex)
        } else if (xCrash < 0) {
          ball.rightCrash(lxIndex)
        }
        if (mPad.y <= ball.getly && mPad.getly >= ball.y && mPad.x <= ball.getlx && mPad.getlx >= ball.x) {
          var newXSpeed: Float = .0f
          var newYSpeed: Float = .0f
          newXSpeed = ball.xSpeed + (ball.getcx - mPad.getlx) / 5
          newYSpeed = -(ball.ySpeed - Math.abs(ball.getcx - mPad.getlx) * 1.2f)
          if (newYSpeed > -10) {
            newYSpeed = -10
          }
          ball.setXSpeed(newXSpeed)
          ball.setYSpeed(newYSpeed)
          if (ball.maxYSpeed < 15) {
            ball.maxYSpeed += 0.1f
          }
        } else if (ball.y > this.h) {
          this.mBalls.remove(i)
          mBallsCount -= 1;
          mBallsCount
          if (mBallsCount == 0) {
            if (mStockBallCount > 0) {
              var resource: Resources = getContext.getResources
              var newMessage: CharSequence = resource.getText(R.string.new_ball_help)
              setMode(CrashBallView.PAUSE)
              mMessage.setText(newMessage)
              mMessage.setVisibility(View.VISIBLE)
            } else {
              setMode(LOSE)
              return
            }
          }
        }
        i += 1;
      }
      mFieldHandler.sleep(DELAY_MILLIS)
    } else {
      CrashBallView.this.invalidate
    }
  }

  @Override override def onDraw(canvas: Canvas): Unit = {
    canvas.drawColor(Color.rgb(120, 140, 160))
    mPaint.setColor(Color.BLACK)
    canvas.drawRect(0, CrashBallView.STATE, w, h, mPaint)
    mPad.draw(canvas, mPaint)
    var i: Int = 0
    while (i < this.mBallsCount) {
      this.mBalls.get(i).draw(canvas, mPaint)
      i += 1;
    }
    i = 0
    while (i < BRICK_COL) {
      var j: Int = 0
      while (j < BRICK_ROW) {
        if (mBricks(i)(j) != null) {
          mBricks(i)(j).draw(canvas, mPaint)
        }
        j += 1;
        j
      }
      i += 1;
      i
    }
  }

  def setTextView(message: TextView): Unit = {
    this.mMessage = message
  }

  def setPointTextView(point: TextView): Unit = {
    this.mPoint = point
  }

  @Override override def onKeyDown(keyCode: Int, event: KeyEvent): Boolean = {
    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
      if (mMode == CrashBallView.READY) {
        setMode(RUNNING)
        update
        return (true)
      }
      if (mMode == RUNNING) {
        setMode(CrashBallView.PAUSE)
        update
        return (true)
      }
      if (mMode == CrashBallView.PAUSE) {
        setMode(RUNNING)
        update
        return (true)
      }
      return (true)
    }
    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_SPACE) {
      addBall
    }
    return super.onKeyDown(keyCode, event)
  }

  private def addBall: Unit = {
    Log.d(this.getClass.getSimpleName, "addBall called w=" + w + " h=" + h)
    if (mMode == RUNNING && mStockBallCount > 0) {
      if (mBallsCount == 0) {
        mMessage.setVisibility(View.INVISIBLE)
      }
      mBalls.add(new Ball(w / 2, h / 2, -0.2f, -5, w, h))
      mBallsCount += 1;
      mBallsCount
      mStockBallCount -= 1;
      mStockBallCount
      var resource: Resources = getContext.getResources
      var newMessage: CharSequence = resource.getText(R.string.stock_ball_count)
      mPoint.setText(newMessage + Integer.toString(mStockBallCount))
    }
  }

  def restoreState(icicle: Bundle): Unit = {
    setMode(CrashBallView.PAUSE)
    mMode = icicle.getInt("mode")
    mBalls = flaotsToBalls(icicle.getFloatArray("balls"))
  }

  private def flaotsToBalls(rawArray: Array[Float]): ArrayList[Ball] = {
    var balls: ArrayList[Ball] = new ArrayList[Ball]
    var coordCount: Int = rawArray.length
    var index: Int = 0
    while (index < coordCount) {
      var ball: Ball = new Ball(rawArray(index), rawArray(index + 1), rawArray(index + 2), rawArray(index + 3), w, h)
      balls.add(ball)
      index += 4
    }
    return balls
  }

  def saveState(icicle: Bundle): Bundle = {
    icicle.putInt("mode", mMode)
    icicle.putFloatArray("balls", ballsToFloats(mBalls))
    return icicle
  }

  private def ballsToFloats(cvec: ArrayList[Ball]): Array[Float] = {
    var count: Int = cvec.size
    var rawArray: Array[Float] = new Array[Float](count * 4)
    var index: Int = 0
    while (index < count) {
      var setBall: Ball = cvec.get(index)
      rawArray(4 * index) = setBall.x
      rawArray(4 * index + 1) = setBall.y
      rawArray(4 * index + 2) = setBall.xSpeed
      rawArray(4 * index + 3) = setBall.ySpeed
      index += 1;
      index
    }
    return rawArray
  }

  private class RefreshHandler extends Handler {
    def sleep(delayMillis: Long): Unit = {
      this.removeMessages(0)
      sendMessageDelayed(obtainMessage(0), delayMillis)
    }

    @Override override def handleMessage(msg: Message): Unit = {
      CrashBallView.this.update
    }
  }

}