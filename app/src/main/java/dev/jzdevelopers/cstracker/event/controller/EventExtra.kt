package dev.jzdevelopers.cstracker.event.controller

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.settings.Theme
import dev.jzdevelopers.cstracker.user.common.UserTheme
import kotlinx.android.synthetic.main.ui_event_extra.*

class EventExtra: JZActivity() {

    // Defines Lateinit Variables//
    private lateinit var eventName           : String
    private lateinit var eventPeopleInCharge : String
    private lateinit var eventPhoneNumber    : String
    private lateinit var eventNotes          : String
    private lateinit var secondaryUserTheme  : UserTheme

    // Define And Initializes Boolean Variable//
    private var isThemeDark = false

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Gets The Event Data//
        eventName           = intent.extras?.get("EVENT_NAME")             as String
        eventPeopleInCharge = intent.extras?.get("EVENT_PEOPLE_IN_CHARGE") as String
        eventPhoneNumber    = intent.extras?.get("EVENT_PHONE_NUMBER")     as String
        eventNotes          = intent.extras?.get("EVENT_NOTES")            as String
        secondaryUserTheme  = intent.extras?.get("SECONDARY_USER_THEME")   as UserTheme

        // Creates The UI//
        createUI(R.layout.ui_event_extra) {

            // Sets The Theme//
            val theme = Theme.getUserTheme(this@EventExtra, secondaryUserTheme)
            theme(theme)

            // Sets The Status Bar Color And Icon Color And Gets Whether The Theme Is Dark//
            val statusBarColor = Theme.getStatusBarColor(this@EventExtra)
            isThemeDark = when(statusBarColor) {
                R.color.white -> {statusBarColor(statusBarColor, true); false}
                else          -> {statusBarColor(statusBarColor, false); true}
            }

            // Sets The Values From The Event Data//
            extraTitle.text              = eventName
            extraPeopleInChargeText.text = eventPeopleInCharge
            extraPhoneText.text          = eventPhoneNumber
            extraNotesText.text          = eventNotes
        }
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When Back Is Clicked//
        clickBack {

            // Exits The EventExtra Activity//
            exitActivity(R.anim.faze_in, R.anim.faze_out)
        }

        // When fabSaveEvent Is Clicked//
        click(fabCloseExtra) {

            // Exits The EventExtra Activity//
            exitActivity(R.anim.faze_in, R.anim.faze_out)
        }
    }
}