package dev.jzdevelopers.cstracker.user

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity

/** Android Activity SecondaryUserAdd
 *  Activity That Adds A Secondary User Profile To The Signed-In Primary User
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class SecondaryUserAdd: JZActivity() {

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_secondary_user_add) {

            // Sets The Theme For The Activity//
            theme(R.style.GreenTheme, false)
        }
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

    }
}