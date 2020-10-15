package dev.jzdevelopers.cstracker.event

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.EventAdd
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
            theme(R.style.GreenTheme, false)
        }
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When eventTextView Is Clicked//
        click(eventTextView) {

            // Starts The Activity EventAdd//
            startActivity(EventAdd::class, R.anim.faze_in, R.anim.faze_out)
        }
    }
}