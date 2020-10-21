package dev.jzdevelopers.cstracker.user.authentication

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.EventView
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.MultiUser.*
import dev.jzdevelopers.cstracker.user.SecondaryUserAdd
import dev.jzdevelopers.cstracker.user.data_classes.PrimaryUser
import dev.jzdevelopers.cstracker.user.data_classes.PrimaryUser.Companion.isActivated
import kotlinx.android.synthetic.main.ui_user_activation.*

/** Android Activity UserActivation
 *  Activity That Verifies A User
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class UserActivation: JZActivity() {

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_user_activation) {

            // Sets The Icon Color of The System Bars//
            navigationColor(R.color.white, true)
            statusBarColor(isDarkIcons  = true)
        }

        // Shows The Info Dialog//
        showVerificationDialog()
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When checkActivation Is Clicked//
        click(checkActivation) {

            // Checks The The Primary User Is Activated//
            val isActivated = isActivated(this)
            if (!isActivated) return@click

            // Gets The Primary User's Multi-User Preference//
            val multiUser = PrimaryUser.getCachedMultiUser(this)

            // Starts The Activity Based On The User Mode//
            when(multiUser) {
                YES.ordinal        -> startActivity(SecondaryUserAdd::class, R.anim.faze_in, R.anim.faze_out)
                NO.ordinal         -> startActivity(EventView::class, R.anim.faze_in, R.anim.faze_out)
                SIGNED_OUT.ordinal -> return@click
            }
        }

        // When resendEmail Is Clicked//
        click(resendEmail) {

            // Resends The Activation Email//
            PrimaryUser.activate(this)

            // Shows The Info Dialog//
            showVerificationDialog()
        }
    }

    /**.
     * Function That Shows The User That The Verification Email Has Been Sent
     */
    private fun showVerificationDialog() {
        showGeneralDialog(
            this,
            R.string.title_verification_sent,
            R.string.general_email_verification
        )
    }
}