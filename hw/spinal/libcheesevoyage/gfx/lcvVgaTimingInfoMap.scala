package libcheesevoyage.gfx
import scala.collection.mutable.Map

object LcvVgaTimingInfoMap {
  //val map = Map[String, LcvVgaTimingInfo]()
  val map = Map[String, LcvVgaTimingInfo](
    // 640 x 480 @ 60 Hz, taken from http://www.tinyvga.com
    "640x480@60" -> LcvVgaTimingInfo(
      pixelClk=25.0,
      //pixelClk=25.175,
      htiming=LcvVgaTimingHv(
        visib=640,
        front=16,
        sync=96,
        back=48
      ),
      vtiming=LcvVgaTimingHv(
        visib=480,
        front=10,
        sync=2,
        back=33
      ),
    ),
    // This is an XGA VGA signal. It didn't work with my monitor.
    "1024x768@60" -> LcvVgaTimingInfo(
      pixelClk=65.0,
      htiming=LcvVgaTimingHv(
        visib=1024,
        front=24,
        sync=136,
        back=160
      ),
      vtiming=LcvVgaTimingHv(
        visib=768,
        front=3,
        sync=6,
        back=29
      ),
    ),
    "1280x800@60" -> LcvVgaTimingInfo(
      pixelClk=83.46,
      htiming=LcvVgaTimingHv(
        visib=1280,
        front=64,
        sync=136,
        back=200
      ),
      vtiming=LcvVgaTimingHv(
        visib=800,
        front=1,
        sync=3,
        back=24
      ),
    ),
    "1440x900@60" -> LcvVgaTimingInfo(
      pixelClk=106.47,
      htiming=LcvVgaTimingHv(
        visib=1440,
        front=80,
        sync=152,
        back=232,
      ),
      vtiming=LcvVgaTimingHv(
        visib=900,
        front=1,
        sync=3,
        back=28,
      )
    ),
    "1600x900@60" -> LcvVgaTimingInfo(
      pixelClk=118.25,
      htiming=LcvVgaTimingHv(
        visib=1600,
        front=88,
        sync=168,
        back=256,
      ),
      vtiming=LcvVgaTimingHv(
        visib=900,
        front=3,
        sync=5,
        back=26,
      ),
    ),
    "1920x1080p@60" -> LcvVgaTimingInfo(
      pixelClk=148.5,
      htiming=LcvVgaTimingHv(
        visib=1920,
        front=88,
        sync=44,
        back=148,
      ),
      vtiming=LcvVgaTimingHv(
        visib=1080,
        front=4,
        sync=5,
        back=36,
      ),
    )
  )
}
