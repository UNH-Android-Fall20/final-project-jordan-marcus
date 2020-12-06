package dev.jzdevelopers.cstracker.event.controller

import android.icu.util.Calendar
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.models.Event
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.libs.JZDate
import dev.jzdevelopers.cstracker.libs.JZDateFormat.AMERICAN
import dev.jzdevelopers.cstracker.settings.Theme
import kotlinx.android.synthetic.main.ui_event_add.*


class EventAdd : JZActivity(), DatePickerDialog.OnDateSetListener {

    // Defines Secondary User ID Variable//
    private lateinit var secondaryUserId : String

    // Defines Date Picker Dialog Attributes//
    private lateinit var calendar: Calendar
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog

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

        // When eventDatePicker Is Clicked//
        click(eventDatePicker) {

            // Initializes A Calendar Instance//
            calendar = Calendar.getInstance()

            // Define And Initializes Date Properties//
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            // Initializes Date Picker Dialog//
            datePickerDialog = DatePickerDialog.newInstance(this@EventAdd, year, month, day)
            datePickerDialog.isThemeDark = false
            datePickerDialog.setTitle("Date Picker")
            datePickerDialog.showYearPickerFirst(false)
            datePickerDialog.version = DatePickerDialog.Version.VERSION_1

            // Show Date Picker Dialog//
            datePickerDialog.show(supportFragmentManager, "DatePickerDialog")
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

        val eventDatePicker = findViewById<View>(R.id.eventDatePicker) as TextView
        eventDatePicker.text = date
    }
}