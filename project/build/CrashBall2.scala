import sbt._

trait Defaults {
  def androidPlatformName = "android-7"
}
class CrashBall2(info: ProjectInfo) extends ParentProject(info) {
  override def shouldCheckOutputDirectories = false
  override def updateAction = task { None }

  lazy val main  = project(".", "CrashBall2", new MainProject(_))
  lazy val tests = project("tests",  "tests", new TestProject(_), main)

  class MainProject(info: ProjectInfo) extends AndroidProject(info) with Defaults with MarketPublish {
    val keyalias  = "change-me"
    val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
  }

  class TestProject(info: ProjectInfo) extends AndroidTestProject(info) with Defaults
}