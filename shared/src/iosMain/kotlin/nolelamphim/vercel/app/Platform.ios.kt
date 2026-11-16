package nolelamphim.vercel.app

import platform.UIKit.UIDevice
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val isTv: Boolean = false
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getCurrentTimeString(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "h:mm a"
    return formatter.stringFromDate(NSDate())
}

actual fun getCurrentHourOfDay(): Int {
    val calendar = NSCalendar.currentCalendar
    val components = calendar.components(NSCalendarUnitHour, NSDate())
    return components.hour.toInt()
}