package dev.jzdevelopers.cstracker.event.controller

import android.icu.util.Calendar
import com.afollestad.materialdialogs.MaterialDialog
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.models.Event
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.libs.JZDate
import dev.jzdevelopers.cstracker.libs.JZTime
import dev.jzdevelopers.cstracker.libs.JZTimeFormat.MILITARY
import dev.jzdevelopers.cstracker.libs.JZTimeFormat.STANDARD
import dev.jzdevelopers.cstracker.settings.Theme
import kotlinx.android.synthetic.main.ui_event_add.*


class EventAdd: JZActivity() {

    // Defines Secondary User ID Variable//
    private lateinit var secondaryUserId : String

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
            val startTime      = jzTime.getStartTime(MILITARY)
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

        // When Pickers Are Clicked//
        pickDate()
        pickEndTime()
        pickStartTime()
    }

    private fun pickDate() {

        // Define And Instantiate TimePickerDialog Value//
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->

        }

        // When eventDatePicker Is Clicked//
        click(eventDatePicker) {

            // Define And Initializes Date Properties//
            val year  = JZDate.getCurrentYear()
            val month = calendar.get(java.util.Calendar.MONTH)
            val day   = calendar.get(java.util.Calendar.DAY_OF_MONTH)

//            // Initializes Date Picker Dialog//
//            datePickerDialog = DatePickerDialog.newInstance(this@EventAdd, year, month, day)
//            datePickerDialog.isThemeDark = false
//            datePickerDialog.setTitle("Date Picker")
//            datePickerDialog.showYearPickerFirst(false)
//            datePickerDialog.version = DatePickerDialog.Version.VERSION_1
//
//            // Show Date Picker Dialog//
//            datePickerDialog.show(supportFragmentManager, "DatePickerDialog")
        }
    }

    private fun pickEndTime() {

        // Define And Instantiate TimePickerDialog Value//
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute, _ ->

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
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute, _ ->

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

            // Shows The Time Picker//
            TimePickerDialog.newInstance(timeSetListener, currentHour, currentMinute, false)
                .show(supportFragmentManager, "TimePickerDialog")
        }
    }
}