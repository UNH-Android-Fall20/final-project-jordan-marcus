package dev.jzdevelopers.cstracker.common

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.EventView
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.libs.JZPrefs.getPref
import dev.jzdevelopers.cstracker.user.MultiUser.*
import dev.jzdevelopers.cstracker.user.oject.PrimaryUser.Companion.PREF_MULTI_USER
import dev.jzdevelopers.cstracker.user.oject.PrimaryUser.Companion.activate
import dev.jzdevelopers.cstracker.user.oject.PrimaryUser.Companion.isActivated
import dev.jzdevelopers.cstracker.user.oject.PrimaryUser.Companion.isSignedIn
import dev.jzdevelopers.cstracker.user.SecondaryUserAdd
import dev.jzdevelopers.cstracker.user.authentication.UserActivation

/** Android Activity MainActivity
 *  Activity That Starts When The Application Is Open
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class MainActivity: JZActivity() {

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {}

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
        if (!isSignedIn(this)) return
        if (!isActivated(this)) {
            activate(this)
            startActivity(UserActivation::class, R.anim.faze_in, R.anim.faze_out)
        }

        // Gets The Primary User's Multi-User Preference//
        val multiUser = getPref(this, PREF_MULTI_USER, SIGNED_OUT.ordinal)

        // Starts The Activity Based On The User Mode//
        when(multiUser) {
            YES.ordinal        -> startActivity(SecondaryUserAdd::class, false)
            NO.ordinal         -> startActivity(EventView::class, false)
            SIGNED_OUT.ordinal -> return
        }
    }
}