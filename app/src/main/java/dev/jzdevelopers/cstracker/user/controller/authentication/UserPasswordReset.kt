package dev.jzdevelopers.cstracker.user.controller.authentication

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.settings.Theme.Companion.getAppStatusBarColor
import dev.jzdevelopers.cstracker.settings.Theme.Companion.getAppTheme
import dev.jzdevelopers.cstracker.user.models.PrimaryUser
import kotlinx.android.synthetic.main.ui_user_password_reset.*

/** Android Activity UserPasswordReset,
 *  Activity That Resets A User's Password
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class UserPasswordReset: JZActivity() {

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_user_password_reset) {

            // Sets The Theme//
            val theme = getAppTheme(this@UserPasswordReset)
            theme(theme)

            // Sets The Bar Colors And Icon Colors//
            val barColor = getAppStatusBarColor(this@UserPasswordReset)
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
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When Back Is Clicked//
        clickBack {

            // Goes Back To Activity UserSignIn//
            exitActivity(R.anim.faze_in, R.anim.faze_out)
        }

        // When resetPassword Is Clicked//
        click(resetPassword) {

            // Gets The Inputted Data//
            val email = email.text.toString()

            // Sends A Reset Password Request To The Inputted Email Address//
            PrimaryUser.passwordReset(this, email)
        }

        // When uiUserPasswordReset Is Clicked//
        click(uiUserPasswordReset) {
            email.clearFocus()
        }
    }
}