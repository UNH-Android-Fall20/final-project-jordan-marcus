package dev.jzdevelopers.cstracker.event

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity

class SeniorTechHelp : JZActivity() {

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_senior_tech_help) {

            // Sets The Theme For The Activity//
            theme(R.style.GreenTheme, false)
        }
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When The System Back Button Is Clicked//
        clickBack {
            exitActivity(R.anim.faze_in, R.anim.faze_out)
        }
    }
}