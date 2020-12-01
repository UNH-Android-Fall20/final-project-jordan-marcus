package dev.jzdevelopers.cstracker.libs

import java.util.Calendar.*

// The Available Formats For The JZDate Library//
enum class JZDateFormat { AMERICAN, EUROPEAN, REVERSED }

/** Kotlin Object JZDate
 *  Object That That Handles Various Date Parsing
 *  @author Jordan Zimmitti
 */
@Suppress("unused")
object JZDate {

    /**.
     * Function That Takes In The Date And Gets The Day
     * @param [date]       The date
     * @param [dateFormat] The format for the inputted date
     * @return The day (D)
     */
    fun fullDateToDay(date: String, dateFormat: JZDateFormat): Int {
        try {

            // Splits The Date//
            val splitDate = date.split("/")

            // Sets The Day Based On The Date Format//
            val day = when(dateFormat) {
                JZDateFormat.AMERICAN -> splitDate[1]
                JZDateFormat.EUROPEAN -> splitDate[0]
                JZDateFormat.REVERSED -> splitDate[2]
            }

            // Returns The Day//
            return day.toInt()
        }
        catch (_: Exception) {

            // Shows The Error When The Inputted Date Format Does Not Match A JZDate Format//
            error("The inputted date does not match [mm/dd/yyyy] [dd/mm/yyyy] [yyyy/mm/dd]")
        }
    }

    /**.
     * Function That Takes In The Date And Gets The Month
     * @param [date]       The date
     * @param [dateFormat] The format for the inputted date
     * @return The month (M)
     */
    fun fullDateToMonth(date: String, dateFormat: JZDateFormat): Int {
        try {

            // Splits The Date//
            val splitDate = date.split("/")

            // Sets The Month Based On The Date Format//
            val month = when (dateFormat) {
                JZDateFormat.AMERICAN -> splitDate[0]
                JZDateFormat.EUROPEAN -> splitDate[1]
                JZDateFormat.REVERSED -> splitDate[1]
            }

            // Returns The Month//
            return month.toInt()
        }
        catch (_: Exception) {

            // Shows The Error When The Inputted Date Format Does Not Match A JZDate Format//
            error("The inputted date does not match [mm/dd/yyyy] [dd/mm/yyyy] [yyyy/mm/dd]")
        }
    }

    /**.
     * Function That Takes In The Date And Gets The Year
     * @param [date]       The date
     * @param [dateFormat] The format for the inputted date
     * @return The year (YYYY)
     */
    fun fullDateToYear(date: String, dateFormat: JZDateFormat): Int {
        try {

            // Splits The Date//
            val splitDate = date.split("/")

            // Sets The Year Based On The Date Format//
            val year = when (dateFormat) {
                JZDateFormat.AMERICAN -> splitDate[2]
                JZDateFormat.EUROPEAN -> splitDate[2]
                JZDateFormat.REVERSED -> splitDate[0]
            }

            // Returns The Year//
            return year.toInt()
        }
        catch (_: Exception) {

            // Shows The Error When The Inputted Date Format Does Not Match A JZDate Format//
            error("The inputted date does not match [mm/dd/yyyy] [dd/mm/yyyy] [yyyy/mm/dd]")
        }
    }

    /**.
     * Function That Gets The Current Day Of The Month
     * @return The current day of the month (D)
     */
    fun getCurrentDay(): Int {

        // Returns The Current Day Of The Month//
        return getInstance().get(DAY_OF_MONTH)
    }

    /**.
     * Function That Gets The Current Month Of The Year
     * @return The current month of the year (M)
     */
    fun getCurrentMonth(): Int {

        // Returns The Current Month Of The Year//
        return getInstance().get(MONTH) + 1
    }

    /**.
     * Function That Gets The Current Year
     * @return The current year (YYYY)
     */
    fun getCurrentYear(): Int {

        // Returns The Current Year//
        return getInstance().get(YEAR)
    }

    /**.
     * Function That Gets The Current Date
     * @param [dateFormat] The format for the returned date
     * @return The date
     */
    fun getCurrentDate(dateFormat: JZDateFormat): String {

        // Gets The Current Day, Month, And Year//
        val day   = fixDatePart(getInstance().get(DAY_OF_MONTH))
        val month = fixDatePart(getInstance().get(MONTH) + 1)
        val year  = fixDatePart(getInstance().get(YEAR))

        // Returns The Date//
        return when(dateFormat) {
            JZDateFormat.AMERICAN -> "$month/$day/$year"
            JZDateFormat.EUROPEAN -> "$day/$month/$year"
            JZDateFormat.REVERSED -> "$year/$month/$day"
        }
    }

    /**.
     * Function That Gets The Date In The Preferred Format
     *
     * @param [dateFormat] The format for the returned date
     *
     * @return The date
     */
    fun getDate(day: Int, month: Int, year: Int, dateFormat: JZDateFormat): String {

        // Gets The Day And Month//
        val fixedDay   = fixDatePart(day)
        val fixedMonth = fixDatePart(month)

        // returns The Date//
        return when(dateFormat) {

            JZDateFormat.AMERICAN -> "$fixedMonth/$fixedDay/$year"
            JZDateFormat.EUROPEAN -> "$fixedDay/$fixedMonth/$year"
            JZDateFormat.REVERSED -> "$year/$fixedMonth/$fixedDay"
        }
    }

    /**.
     * Function That Takes The Inputted Date Format And Switches It To Another Date Format
     * @param [date]              The date
     * @param [currentDateFormat] The format for the inputted date
     * @param [newDateFormat]     The format for the returned date
     * @return The date in the new format
     */
    fun switchDateFormat(date: String, currentDateFormat: JZDateFormat, newDateFormat: JZDateFormat): String {

        // Splits The Date//
        val splitDate = date.split("/")

        // Define And Initialize Date Parts//
        var day   = ""
        var month = ""
        var year  = ""

        // Sets The Day, Month, And Year Based On The Current Format//
        when(currentDateFormat) {
            JZDateFormat.AMERICAN -> { day = splitDate[1]; month = splitDate[0]; year = splitDate[2] }
            JZDateFormat.EUROPEAN -> { day = splitDate[0]; month = splitDate[1]; year = splitDate[2] }
            JZDateFormat.REVERSED -> { day = splitDate[2]; month = splitDate[1]; year = splitDate[0] }
        }

        // Returns The Date In The New Format//
        return when(newDateFormat) {
            JZDateFormat.AMERICAN -> "$month/$day/$year"
            JZDateFormat.EUROPEAN -> "$day/$month/$year"
            JZDateFormat.REVERSED -> "$year/$month/$day"
        }
    }

    /**.
     * Function That Adds Leading Zeros To The Parts Of The Date When Necessary
     * @param [datePart] The part of the date
     * @return The part of the date with a leading zero or not
     */
    private fun fixDatePart(datePart: Int): String {

        // Returns The Part Of The Date With The Leading Zero Or Not//
        return when {
            datePart < 10 -> "0$datePart"
            else          -> "$datePart"
        }
    }
}