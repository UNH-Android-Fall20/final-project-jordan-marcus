package dev.jzdevelopers.cstracker.event.controller

import com.afollestad.materialdialogs.MaterialDialog
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog.OnTimeSetListener
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.models.Event
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.libs.JZDate
import dev.jzdevelopers.cstracker.libs.JZDateFormat.AMERICAN
import dev.jzdevelopers.cstracker.libs.JZTime
import dev.jzdevelopers.cstracker.libs.JZTimeFormat.MILITARY
import dev.jzdevelopers.cstracker.libs.JZTimeFormat.STANDARD
import dev.jzdevelopers.cstracker.settings.Theme
import dev.jzdevelopers.cstracker.user.common.UserTheme
import kotlinx.android.synthetic.main.ui_event_add.*

class EventAdd: JZActivity() {

    // Define And Initializes Boolean Variable//
    private var isThemeDark = false

    // Define And Initializes JZTime Variable//
    private val jzTime = JZTime()

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Gets The Secondary User's Data//
        val secondaryUserTheme = intent.extras?.get("SECONDARY_USER_THEME") as UserTheme

        // Creates The UI//
        createUI(R.layout.ui_event_add) {

            // Sets The Theme//
            val theme = Theme.getUserTheme(this@EventAdd, secondaryUserTheme)
            theme(theme)

            // Sets The Status Bar Color And Icon Color And Gets Whether The Theme Is Dark//
            val statusBarColor = Theme.getStatusBarColor(this@EventAdd)
            isThemeDark = when(statusBarColor) {
                R.color.white -> {statusBarColor(statusBarColor, true); false}
                else          -> {statusBarColor(statusBarColor, false); true}
            }
        }
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When Back Is Clicked//
        clickBack {

            // Shows The Error Dialog//
            MaterialDialog(this).show {
                title(R.string.title_double_check)
                message(R.string.positive_no_save)
                cancelOnTouchOutside(false)
                cornerRadius(16.0f)
                negativeButton(R.string.button_negative)
                positiveButton(R.string.button_positive) {

                    // Exits The EventAdd Activity//
                    exitActivity(R.anim.faze_in, R.anim.faze_out)
                }
            }
        }

        // When uiEventAdd Is Clicked//
        click(uiEventAdd) {
            eventName.clearFocus()
            location.clearFocus()
            peopleInCharge.clearFocus()
            phoneNumber.clearFocus()
            notes.clearFocus()
        }

        // When fabSaveEvent Is Clicked//
        click(fabSaveEvent) {

            // Gets The Inputted String Data//
            val date           = eventDatePicker.text.toString()
            val endTime        = endTimeValue.text.toString()
            val eventName      = eventName.text.toString()
            val location       = location.text.toString()
            val notes          = notes.text.toString()
            val peopleInCharge = peopleInCharge.text.toString()
            val phoneNumber    = phoneNumber.text.toString()
            val startTime      = startTimeValue.text.toString()
            val totalTime      = totalTimeValue.text.toString()
            val totalTimeInMin = jzTime.getTimeDifferenceInMin()
            val userId         = intent.extras?.get("SECONDARY_USER_ID") as String

            // Creates The New Event//
            val event = Event(
                this,
                totalTimeInMin,
                userId,
                date,
                endTime,
                location,
                eventName,
                notes,
                peopleInCharge,
                phoneNumber,
                startTime,
                totalTime,
            )

            // Adds The Event To The Database//
            val isSuccessful = event.add(progressBar)
            if (!isSuccessful) return@click

            // Exits The EventAdd Activity//
            exitActivity(R.anim.faze_in, R.anim.faze_out)
        }

        // When Pickers Are Clicked//
        pickDate()
        pickEndTime()
        pickStartTime()
    }

    /**.
     * Function That Picks The Date
     */
    private fun pickDate() {

        // Gets The Current Year, Month And Day//
        var currentYear  = JZDate.getCurrentYear()
        var currentMonth = JZDate.getCurrentMonth() - 1
        var currentDay   = JZDate.getCurrentDay()

        // Define And Instantiate DatePickerDialog Value//
        val dateSetListener = OnDateSetListener { _, year, month, day ->

            // Saves The Date Preset Set By The User//
            currentYear  = year
            currentMonth = month
            currentDay   = day

            // Shows Date Values//
            eventDatePicker.text = JZDate.getDate(day, month + 1, year, AMERICAN)
        }

        // When eventDatePicker Is Clicked//
        click(eventDate, eventDatePicker) {

            // Creates The Date-Picker//
            val datePicker = DatePickerDialog.newInstance(
                dateSetListener,
                currentYear,
                currentMonth,
                currentDay
            )

            // Sets Date-Picker Preferences//
            datePicker.isThemeDark = isThemeDark
            datePicker.vibrate(false)

            // When Date-Picker Is Dismissed//
            datePicker.setOnDismissListener {

                // Sets The Status Bar Color And Icon Color//
                val statusBarColor = Theme.getStatusBarColor(this@EventAdd)
                when(statusBarColor) {
                    R.color.white -> UI().statusBarColor(statusBarColor, true)
                    else          -> UI().statusBarColor(statusBarColor, false)
                }
            }

            // Shows The Date-Picker//
            datePicker.show(supportFragmentManager, "DatePickerDialog")
        }
    }

    /**.
     * Function That Picks The End-Time
     */
    private fun pickEndTime() {

        // Gets The Current Hour And Minute//
        var currentHour   = JZTime.getCurrentHour(MILITARY)
        var currentMinute = JZTime.getCurrentMinute()

        // Define And Instantiate TimePickerDialog Value//
        val timeSetListener = OnTimeSetListener { _, hour, minute, _ ->

            // Saves The Time Preset Set By The User//
            currentHour   = hour
            currentMinute = minute

            // Sets The Picked Time Values//
            jzTime.endTimeHour   = hour
            jzTime.endTimeMinute = minute

            // Shows Start-Time And Total-Time Values//
            endTimeValue.text   = jzTime.getEndTime(STANDARD)
            totalTimeValue.text = jzTime.getTimeDifference()
        }

        // When endTime And endTimeValue Are Clicked//
        click(endTime, endTimeValue) {

            // Creates The Time-Picker//
            val timePicker = TimePickerDialog.newInstance(
                timeSetListener,
                currentHour,
                currentMinute,
                false
            )

            // Sets Time-Picker Preferences//
            timePicker.isThemeDark = isThemeDark
            timePicker.vibrate(false)

            // Shows The Time-Picker//
            timePicker.show(supportFragmentManager, "TimePickerDialog")
        }
    }

    /**.
     * Function That Picks The Start-Time
     */
    private fun pickStartTime() {

        // Gets The Current Hour And Minute//
        var currentHour   = JZTime.getCurrentHour(MILITARY)
        var currentMinute = JZTime.getCurrentMinute()

        // Define And Instantiate TimePickerDialog Value//
        val timeSetListener = OnTimeSetListener { _, hour, minute, _ ->

            // Saves The Time Preset Set By The User//
            currentHour   = hour
            currentMinute = minute

            // Sets The Picked Time Values//
            jzTime.startTimeHour   = hour
            jzTime.startTimeMinute = minute

            // Shows Start-Time And Total-Time Values//
            startTimeValue.text = jzTime.getStartTime(STANDARD)
            totalTimeValue.text = jzTime.getTimeDifference()
        }

        // When startTime And startTimeValue Are Clicked//
        click(startTime, startTimeValue) {

            // Creates The Time-Picker//
            val timePicker = TimePickerDialog.newInstance(
                timeSetListener,
                currentHour,
                currentMinute,
                false
            )

            // Sets Time-Picker Preferences//
            timePicker.isThemeDark = isThemeDark
            timePicker.vibrate(false)

            // Shows The Time-Picker//
            timePicker.show(supportFragmentManager, "TimePickerDialog")
        }
    }
}