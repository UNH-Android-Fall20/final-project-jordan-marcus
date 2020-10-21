package dev.jzdevelopers.cstracker.common

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.EventView
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.MultiUser.*
import dev.jzdevelopers.cstracker.user.SecondaryUserView
import dev.jzdevelopers.cstracker.user.authentication.UserActivation
import dev.jzdevelopers.cstracker.user.authentication.UserSignIn
import dev.jzdevelopers.cstracker.user.data_classes.PrimaryUser
import dev.jzdevelopers.cstracker.user.data_classes.PrimaryUser.Companion.isActivated
import dev.jzdevelopers.cstracker.user.data_classes.PrimaryUser.Companion.isSignedIn

/** Android Activity MainActivity
 *  Activity That Starts When The Application Is Open
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class MainActivity: JZActivity() {

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_blank) {

            // Sets The Icon Color of The System Bars//
            navigationColor(R.color.white, true)
        }
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {}

    /**.
     * Function That Handles All API Calls For The Activity
     */
    override suspend fun apiCalls() {

        // Checks To See If The User Is Already Signed In//
        checkLoginStatus()
    }

    /**.
     * Function That Checks To See If The User Is Already Signed In To Bypass The Login Screen
     */
    private suspend fun checkLoginStatus() {

        // When The Primary User Is Not Signed In//
        if (!isSignedIn(this)) {
            startActivity(UserSignIn::class, false)
            return
        }
        if (!isActivated(this)) {
            startActivity(UserActivation::class, false)
        }

        // Gets The Primary User's Multi-User Preference//
        val multiUser = PrimaryUser.getCachedMultiUser(this)

        // Starts The Activity Based On The User Mode//
        when(multiUser) {
            YES.ordinal        -> startActivity(SecondaryUserView::class, false)
            NO.ordinal         -> startActivity(EventView::class, false)
            SIGNED_OUT.ordinal -> startActivity(UserSignIn::class, false)
        }
    }
}