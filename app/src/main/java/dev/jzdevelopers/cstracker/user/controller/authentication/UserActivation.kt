package dev.jzdevelopers.cstracker.user.controller.authentication

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.controller.EventView
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.settings.Theme
import dev.jzdevelopers.cstracker.user.controller.crud.SecondaryUserAdd
import dev.jzdevelopers.cstracker.user.models.PrimaryUser
import dev.jzdevelopers.cstracker.user.models.PrimaryUser.Companion.getCachedMultiUser
import dev.jzdevelopers.cstracker.user.models.PrimaryUser.Companion.isActivated
import kotlinx.android.synthetic.main.ui_user_activation.*

/** Android Activity UserActivation,
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

            // Sets The Theme//
            val theme = Theme.getAppTheme(this@UserActivation)
            theme(theme)

            // Sets The Bar Colors And Icon Colors//
            val barColor = Theme.getStatusBarColor(this@UserActivation)
            when(barColor) {
                R.color.white -> {
                    navigationColor(barColor, true)
                    statusBarColor(barColor, true)
                }
                else -> {
                    navigationColor(barColor, false)
                    statusBarColor(barColor, false)
                }
            }
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

            // Checks Whether The Primary-User Is Activated//
            val isActivated = isActivated(this)
            if (!isActivated) return@click

            // Gets The Primary-User's Multi-User Preference//
            val isMultiUser = getCachedMultiUser(this)

            // Starts The Activity Based On The User Mode//
            when(isMultiUser) {
                true  -> startActivity(SecondaryUserAdd::class, R.anim.faze_in, R.anim.faze_out)
                false -> startActivity(EventView::class, R.anim.faze_in, R.anim.faze_out)
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