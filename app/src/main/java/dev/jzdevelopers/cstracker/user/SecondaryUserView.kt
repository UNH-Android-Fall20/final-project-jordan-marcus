package dev.jzdevelopers.cstracker.user

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import kotlinx.android.synthetic.main.ui_secondary_user_view.*

/** Android Activity SecondaryUserView
 *  Activity That Shows All Of The Secondary Users Under The Signed In Primary User
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class SecondaryUserView: JZActivity() {

    //<editor-fold desc="Class Variables">

    //</editor-fold>

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_secondary_user_view) {

            // Sets The Icon Color of The System Bars//
            statusBarColor(isDarkIcons = true)
        }
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When fabAddProfile Is Clicked//
        click(fabAddProfile) {

            // Starts The SecondaryUserAdd Activity//
            startActivity(SecondaryUserAdd::class, R.anim.faze_in, R.anim.faze_out)
        }
    }
}