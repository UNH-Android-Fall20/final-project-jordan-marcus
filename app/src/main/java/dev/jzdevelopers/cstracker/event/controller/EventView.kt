package dev.jzdevelopers.cstracker.event.controller

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.models.SeniorTechHelp
import dev.jzdevelopers.cstracker.libs.JZActivity
import kotlinx.android.synthetic.main.ui_event_view.*

class EventView : JZActivity() {

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_event_view) {

            // Sets The Theme For The Activity//
            theme(R.style.GreenTheme)
        }
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When addEventTextView Is Clicked//
        click(addEventTextView) {

            // Starts The Activity EventAdd//
            startActivity(EventAdd::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When seniorTextView Is Clicked//
        click(seniorTextView) {

            // Starts The Activity SeniorTechHelp//
            startActivity(SeniorTechHelp::class, R.anim.faze_in, R.anim.faze_out)
        }
    }
}