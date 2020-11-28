package dev.jzdevelopers.cstracker.libs

import android.util.Log
import dev.jzdevelopers.cstracker.libs.JZTimeFormat.MILITARY
import dev.jzdevelopers.cstracker.libs.JZTimeFormat.STANDARD
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import kotlin.math.abs

// The Available Formats For The JZTime Library//
enum class JZTimeFormat {MILITARY, STANDARD}

/** Kotlin Class JZTime
 *  Class That Handles Time And Time Differences
 *  @author Jordan Zimmitti
 *  @param [startTimeHour]   The hour of the start-time in military-time
 *  @param [startTimeMinute] The minute of the start-time in military-time
 *  @param [endTimeHour]     The hour of the end-time in military-time
 *  @param [endTimeMinute]   The minute of the end-time in military-time
 */
@Suppress("unused")
class JZTime(
    var startTimeHour   : Int = NO_INPUT,
    var startTimeMinute : Int = NO_INPUT,
    var endTimeHour     : Int = NO_INPUT,
    var endTimeMinute   : Int = NO_INPUT,
) {

    /**.
     * Configures Static Functions And Variables
     */
    companion object {

        // Define And Initializes Error Values//
        private const val FORMAT_ERROR = -1
        private const val NO_INPUT = -1

        /**.
         * Function That Takes In The Time And Gets The Hour
         * @param [time]       The time (hh:mm) or (hh:mm aa)
         * @param [timeFormat] The format for the returned hour
         * @return The hour (H)
         */
        fun fullTimeToHour(time: String, timeFormat: JZTimeFormat): Int {

            // Gets The Hour//
            val hour = time.split(":")[0]

            // When The Inputted Time Was In Standard Time//
            if (time.contains("AM") || time.contains("PM")) {

                // Returns The Hour//
                return when {

                    // When The timeFormat Is Standard//
                    timeFormat == STANDARD -> hour.toInt()

                    // When The timeFormat Is Military And The Hour Equals Twelve//
                    hour.toInt() == 12 && time.contains("AM") -> 0
                    hour.toInt() == 12 && time.contains("PM") -> 12

                    // When The timeFormat Is Military And Does Not Equal Twelve//
                    time.contains("PM") -> hour.toInt() + 12
                    time.contains("AM") -> hour.toInt()

                    // When The Inputted Time Is Wrong//
                    else -> FORMAT_ERROR
                }
            }

            // Returns The Hour//
            return when {

                // When The timeFormat Is Military//
                timeFormat == MILITARY -> hour.toInt()

                // When timeFormat Is Standard//
                timeFormat == STANDARD && hour.toInt() == 0  -> 12
                timeFormat == STANDARD && hour.toInt() == 12 -> 12
                timeFormat == STANDARD && hour.toInt() >  12 -> hour.toInt() - 12
                timeFormat == STANDARD && hour.toInt() <  12 -> hour.toInt()

                // When The Inputted Time Is Wrong//
                else -> FORMAT_ERROR
            }
        }

        /**.
         * Function That Takes In The Time And Gets The Minute
         * @param [time] The time (hh:mm) or (hh:mm aa)
         * @return The minute (M)
         */
        fun fullTimeToMinute(time: String): Int {

            // Returns The Minute//
            return time.split(":")[1].split(" ")[0].toInt()
        }

        /**.
         * Function That Gets The Current Hour Of The Day
         * @param [timeFormat] The format for the current hour
         * @return The current hour (H)
         */
        fun getCurrentHour(timeFormat: JZTimeFormat): Int {

            // Gets The Current Hour Of The Day//
            val hour = Calendar.getInstance().get(HOUR_OF_DAY)

            // When timeFormat Is Military//
            if (timeFormat == MILITARY) return hour

            // Converts The Hour To Standard Time//
            return when {
                hour == 0  -> 12
                hour == 12 -> 12
                hour >  12 -> hour - 12
                hour <  12 -> hour
                else       -> hour
            }
        }

        /**.
         * Function That Gets The Current Minute Of The Day
         * @return The current minute (M)
         */
        fun getCurrentMinute(): Int {

            // Returns The Minute//
            return Calendar.getInstance().get(MINUTE)
        }

        /**.
         * Function That Gets The Current Time Of The Day
         * @param [timeFormat] The format for the current time
         * @return The current time
         */
        fun getCurrentTime(timeFormat: JZTimeFormat): String {

            // Gets The Current Hour And Minute Of The Day//
            val hour   = Calendar.getInstance().get(HOUR_OF_DAY)
            val minute = Calendar.getInstance().get(MINUTE)

            // What Happens Based On The timeFormat//
            return when (timeFormat) {
                MILITARY -> convertMilitaryTime(hour, minute)
                STANDARD -> convertStandardTime(hour, minute)
            }
        }

        /**.
         * Function That Converts Standard Time To Military Time
         * @param [hour]   The hour of day (H)
         * @param [minute] The minute of day (MM)
         * @return the time in military time
         */
        private fun convertMilitaryTime(hour: Int, minute: Int): String {

            // Returns The Time In Military Time//
            return when {
                minute <  10 -> "$hour:0$minute"
                minute >= 10 -> "$hour:$minute"
                else -> error("The inputted time format is not recognizable")
            }
        }

        /**.
         * Function That Converts Military Time To Standard Time
         * @param [hour]   The hour of day (H)
         * @param [minute] The minute of day (MM)
         * @return The time in standard time
         */
        private fun convertStandardTime(hour: Int, minute: Int): String {

            // Returns The Time In Standard Time//
            return when {
                hour == 0  && minute <  10 -> "12:0$minute AM"
                hour == 0  && minute >= 10 -> "12:$minute AM"
                hour == 12 && minute <  10 -> "12:0$minute PM"
                hour == 12 && minute >= 10 -> "12:$minute PM"
                hour <  12 && minute <  10 -> "$hour:0$minute AM"
                hour <  12 && minute >= 10 -> "$hour:$minute AM"
                hour >  12 && minute <  10 -> "${hour - 12}:0$minute PM"
                hour >  12 && minute >= 10 -> "${hour - 12}:$minute PM"
                else -> error("The inputted time format is not recognizable")
            }
        }
    }

    /**.
     * Function That Shows The Time Difference Between Start-Time And End-Time In Minutes
     * @return Time difference in minutes
     */
    fun getTimeDifferenceInMin(): Int {

        // Gets The Time Difference In Milliseconds//
        val timeDiffInMills = calculateTimeDiffInMilliseconds()

        // When No Input Was Detected//
        if (timeDiffInMills == NO_INPUT) return 0

        // Formats The Time Difference//
        when {

            // When Time Difference Is Less Than Zero//
            timeDiffInMills < 0 -> {

                // Gets The Hour And The Minute//
                val hour   = (24 - abs(timeDiffInMills / (60 * 60 * 1000) % 24)) - 1
                val minute = fixMinute((60 - abs(timeDiffInMills / (60 * 1000) % 60)))

                // Returns The Time Difference//
                return (hour * 60) + minute.toInt()
            }

            // When Time Difference Is Zero Or More//
            else -> {

                // Gets The Hour And The Minute//
                val hour   = timeDiffInMills / (60 * 60 * 1000) % 24
                val minute = fixMinute((timeDiffInMills / (60 * 1000) % 60))

                // Returns The Time Difference//
                return (hour * 60) + minute.toInt()
            }
        }
    }

    /**.
     * Function That Gets The End-Time In Hours And Minutes
     * @param [timeFormat] The format for the returned time
     * @return The end-time
     */
    fun getEndTime(timeFormat: JZTimeFormat): String {

        // When An EndTime Value Is Not Initialized//
        if (endTimeHour == NO_INPUT || endTimeMinute == NO_INPUT) {

            // Shows The Error//
            error("[endTimeHour] or [endTimeMinute] is not initialized")
        }

        // Returns The End-Time//
        return when (timeFormat) {
            MILITARY -> convertMilitaryTime(endTimeHour, endTimeMinute)
            STANDARD -> convertStandardTime(endTimeHour, endTimeMinute)
        }
    }

    /**.
     * Function That Gets The Start-Time In Hours And Minutes
     * @param [timeFormat] The format for the returned time
     * @return The start-time
     */
    fun getStartTime(timeFormat: JZTimeFormat): String {

        // When A StartTime Value Is Not Initialized//
        if (startTimeHour == NO_INPUT || startTimeMinute == NO_INPUT) {

            // Shows The Error//
            error("[startTimeHour] or [startTimeMinute] is not initialized")
        }

        // Returns The Start-Time//
        return when (timeFormat) {
            MILITARY -> convertMilitaryTime(startTimeHour, startTimeMinute)
            STANDARD -> convertStandardTime(startTimeHour, startTimeMinute)
        }
    }

    /**.
     * Function That Shows The Time Difference Between Start-Time And End-Time In Hours And Minutes
     * @return Time difference in hours and minutes
     */
    fun getTimeDifference(): String {

        // Gets The Time Difference In Milliseconds//
        val timeDiffInMills = calculateTimeDiffInMilliseconds()

        // When No Input Was Detected//
        if (timeDiffInMills == NO_INPUT) return "0:00"

        // Formats The Time Difference//
        when {

            // When Time Difference Is Less Than Zero//
            timeDiffInMills < 0 -> {

                // Gets The Hour And The Minute//
                val hour   = fixHour(24 - abs(timeDiffInMills / (60 * 60 * 1000) % 24))
                val minute = fixMinute((60 - abs(timeDiffInMills / (60 * 1000) % 60)))

                // Returns The Time Difference//
                return "$hour:$minute"
            }

            // When Time Difference Is Zero Or More//
            else -> {

                // Gets The Hour And The Minute//
                val hour   = timeDiffInMills / (60 * 60 * 1000) % 24
                val minute = fixMinute((timeDiffInMills / (60 * 1000) % 60))

                // Returns The Time Difference//
                return "$hour:$minute"
            }
        }
    }

    /**.
     * Function That Gets The Time Difference Between Start-Time And End-Time In Milliseconds
     *
     * @return Time difference
     */
    private fun calculateTimeDiffInMilliseconds(): Int {

        // When One Of The Time Inputs Are Not Initialized//
        val tag     = "JZTime"
        val message = "Were all the start-time and end-time values initialized correctly?"
        when {
            endTimeHour     == NO_INPUT -> {Log.e(tag, message); return NO_INPUT}
            endTimeMinute   == NO_INPUT -> {Log.e(tag, message); return NO_INPUT}
            startTimeHour   == NO_INPUT -> {Log.e(tag, message); return NO_INPUT}
            startTimeMinute == NO_INPUT -> {Log.e(tag, message); return NO_INPUT}
        }

        // Gets The End Time In Milliseconds//
        val timeEndHourMilli   = endTimeHour * 60 * 60 * 1000
        val timeEndMinuteMilli = endTimeMinute * 60 * 1000
        val timeEndMilli       = timeEndHourMilli + timeEndMinuteMilli

        // Gets The Start Time In Milliseconds//
        val timeStartHourMilli   = startTimeHour * 60 * 60 * 1000
        val timeStartMinuteMilli = startTimeMinute * 60 * 1000
        val timeStartMilli       = timeStartHourMilli + timeStartMinuteMilli

        // Returns The Time Difference In Milliseconds//
        return timeEndMilli - timeStartMilli
    }

    /**.
     * Function That Adds A Leading Zero To The Hour When Necessary
     * @param [hour] The hour
     * @return The hour with a leading zero or not
     */
    private fun fixHour(hour: Int): String {

        // Returns The Hour//
        return when {
            endTimeMinute < startTimeMinute -> "${hour - 1}"
            endTimeMinute > startTimeMinute -> "${hour - 1}"
            else                            -> "$hour"
        }
    }

    /**.
     * Function That Adds A Leading Zero To The Minute When Necessary
     * @param [minute] The minute
     * @return The minute with a leading zero or not
     */
    private fun fixMinute(minute: Int): String {

        // Returns The Minute//
        return when {
            minute == 60 -> "00"
            minute < 10  -> "0$minute"
            else         -> minute.toString()
        }
    }
}