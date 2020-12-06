package dev.jzdevelopers.cstracker.event.controller

import android.icu.util.Calendar
import android.util.Log
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.models.Event
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.libs.JZDate
import dev.jzdevelopers.cstracker.libs.JZDateFormat.AMERICAN
import dev.jzdevelopers.cstracker.libs.JZTime
import dev.jzdevelopers.cstracker.settings.Theme
import kotlinx.android.synthetic.main.ui_event_add.*


class EventAdd : JZActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    // Defines Secondary User ID Variable//
    private lateinit var secondaryUserId : String

    // Defines Date Picker Dialog Attributes//
    private lateinit var calendar         : Calendar
    private lateinit var datePickerDialog : DatePickerDialog
    private lateinit var timePickerDialog : TimePickerDialog

    // Define And Initializes JZTime Variable//
    private val jzTime = JZTime()

    // Define And Initializes The Time Button Clicked Last//
    private var lastTimeIdClicked = 0

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_event_add) {

            // Sets The Theme//
            val theme = Theme.getAppTheme(this@EventAdd)
            theme(theme)

            // Initializes A Calendar Instance//
            calendar = Calendar.getInstance()

            // Gets The Secondary User's ID//
            secondaryUserId = intent.extras?.get("SECONDARY_USER_ID") as String

            // Sets The Status Bar Color And Icon Color//
            val statusBarColor = Theme.getStatusBarColor(this@EventAdd)
            when(statusBarColor) {
                R.color.white -> statusBarColor(statusBarColor, true)
                else          -> statusBarColor(statusBarColor, false)
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

                    // Starts The EventView Activity//
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

        // When eventDatePicker Is Clicked//
        click(eventDatePicker) {

            // Define And Initializes Date Properties//
            val year  = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day   = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            // Initializes Date Picker Dialog//
            datePickerDialog = DatePickerDialog.newInstance(this@EventAdd, year, month, day)
            datePickerDialog.isThemeDark = false
            datePickerDialog.setTitle("Date Picker")
            datePickerDialog.showYearPickerFirst(false)
            datePickerDialog.version = DatePickerDialog.Version.VERSION_1

            // Show Date Picker Dialog//
            datePickerDialog.show(supportFragmentManager, "DatePickerDialog")
        }

        // When startTime Or startTimeValue Are Clicked//
        click(startTime, startTimeValue) {

            // Set The Time Button Clicked Last To startTimeValue//
            lastTimeIdClicked = R.id.startTimeValue

            // Opens The Time Picker Dialog//
            openTimePicker()
        }

        // When endTime Or endTimeValue Are Clicked//
        click(endTime, endTimeValue) {

            // Set The Time Button Clicked Last To endTimeValue//
            lastTimeIdClicked = R.id.endTimeValue

            // Opens The Time Picker Dialog//
            openTimePicker()
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
            val userId         = secondaryUserId

            // Adds The New User Event//
            val event = Event(
                this,
                date,
                endTime,
                location,
                eventName,
                notes,
                peopleInCharge,
                phoneNumber,
                startTime,
                totalTime,
                userId
            )
            val isSuccessful  = event.add(progressBar)
            if (!isSuccessful) return@click

            // Starts The EventView Activity//
            startActivity(EventView::class, R.anim.faze_in, R.anim.faze_out)
        }
    }

    /**.
     * Function That Runs When The Date Is Set Using The Date Picker//
     */
    override fun onDateSet(view: DatePickerDialog?, year: Int, month: Int, day: Int) {
        val date = JZDate.getDate(day, month + 1, year, AMERICAN)

        // Logs That The Event Date Was Updated Successfully//
        Log.v("EventAdd", "Event date has been updated: $date")

        // Set The Text Value For eventDatePicker//
        val eventDatePicker  = findViewById<View>(R.id.eventDatePicker) as TextView
        eventDatePicker.text = date
    }

    /**.
     * Function That Runs When Any Of The Times Are Set Using The Time Picker//
     */
    override fun onTimeSet(view: TimePickerDialog?, hour: Int, minute: Int, second: Int) {

        // Set Time Value. If Minute Is 0, Use Two Zeros Instead//
        val time : String = if (minute == 0) "${hour}:00"
                            else "${hour}:${minute}"

        // Logs That The Event Time Was Updated Successfully And Updates Time Values//
        when(lastTimeIdClicked) {
            R.id.startTimeValue -> {
                Log.v("EventAdd", "Event start time has been updated: $time")
                jzTime.startTimeHour   = hour
                jzTime.startTimeMinute = minute
            }
            R.id.endTimeValue -> {
                Log.v("EventAdd", "Event end time has been updated: $time")
                jzTime.endTimeHour   = hour
                jzTime.endTimeMinute = minute
            }
        }

        // Set The UI Text Value For The lastTimeIdClicked (startTimeValue / endTimeValue)//
        val timeValue  = findViewById<View>(lastTimeIdClicked) as TextView
        timeValue.text = time

        // Calculate Total Time If Both Times Are Selected
        if (jzTime.startTimeHour > -1 && jzTime.startTimeMinute > -1 &&
            jzTime.endTimeHour   > -1 && jzTime.endTimeMinute   > -1) {
                val totalTime  = findViewById<View>(R.id.totalTimeValue) as TextView
                totalTime.text = jzTime.getTimeDifference()
        }
    }

    /**.
     * Function That Handles Opening The Time Picker//
     */
    private fun openTimePicker() {

        // Define And Initializes Time Properties//
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)

        // Initializes Time Picker Dialog//
        timePickerDialog = TimePickerDialog.newInstance(this@EventAdd, hour, minute,false )
        timePickerDialog.isThemeDark = false
        timePickerDialog.title = "Time Picker"
        timePickerDialog.version = TimePickerDialog.Version.VERSION_1

        // Show Date Picker Dialog//
        timePickerDialog.show(supportFragmentManager, "TimePickerDialog")
    }
}