package dev.jzdevelopers.cstracker.event.controller

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.common.DatePickerFragment
import dev.jzdevelopers.cstracker.event.models.Event
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.models.PrimaryUser
import kotlinx.android.synthetic.main.ui_event_add.*

class EventAdd : JZActivity() {

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_event_add) {

            // Sets The Theme For The Activity//
            theme(R.style.GreenTheme)
        }
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When uiEventAdd Is Clicked//
        click(uiEventAdd) {
            eventName.clearFocus()
            location.clearFocus()
            peopleInCharge.clearFocus()
            phoneNumber.clearFocus()
            notes.clearFocus()
        }

        // When eventDatePicker Is Clicked//
        click (eventDatePicker) {
            // Initialize a new DatePickerFragment
            val newFragment = DatePickerFragment()
            // Show the date picker dialog
            newFragment.show(fragmentManager, "Date Picker")
        }

        // When fabSaveEvent Is Clicked//
        click (fabSaveEvent) {

            // Gets The Inputted String Data//
            val date           = eventDatePicker.text.toString()
            val endTime        = endTimeValue.text.toString()
            val eventName      = eventName.text.toString()
            val location       = location.text.toString()
            val notes          = notes.text.toString()
            val peopleInCharge = peopleInCharge.text.toString()
            val phoneNumber    = phoneNumber.text.toString()
            val startTime      = startTimeValue.text.toString()
            val userId         = PrimaryUser.getId(this)

            // Adds The New User Event//
            val event = Event(this, date, endTime, location, eventName, notes, peopleInCharge, phoneNumber, startTime, userId)
            val isSuccessful  = event.add(progressBar)
            if (!isSuccessful) return@click

            // Starts The EventView Activity//
            startActivity(EventView::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When The System Back Button Is Clicked//
        clickBack {
            exitActivity(R.anim.faze_in, R.anim.faze_out)
        }
    }
}