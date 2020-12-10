package dev.jzdevelopers.cstracker.user.controller.authentication

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.settings.Theme.Companion.getAppStatusBarColor
import dev.jzdevelopers.cstracker.settings.Theme.Companion.getAppTheme
import dev.jzdevelopers.cstracker.user.common.UserTheme.GREEN
import dev.jzdevelopers.cstracker.user.models.PrimaryUser
import dev.jzdevelopers.cstracker.user.models.PrimaryUser.Companion.activate
import dev.jzdevelopers.cstracker.user.models.PrimaryUser.Companion.isSignedIn
import kotlinx.android.synthetic.main.ui_user_sign_up.*

/** Android Activity UserSignUp,
 *  Activity That Signs Up A New User
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class UserSignUp: JZActivity() {

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_user_sign_up) {

            // Sets The Theme//
            val theme = getAppTheme(this@UserSignUp)
            theme(theme)

            // Sets The Bar Colors And Icon Colors//
            val barColor = getAppStatusBarColor(this@UserSignUp)
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

        // When signUp Is Clicked//
        click(signUp) {

            // Gets The Inputted Data//
            val firstName       = firstName.text.toString()
            val lastName        = lastName.text.toString()
            val email           = email.text.toString()
            val password        = password.text.toString()
            val confirmPassword = confirmPassword.text.toString()
            val isMultiUser     = signUpCheckBox.isChecked

            // Signs The New Primary-User Up//
            val primaryUser  = PrimaryUser(this, firstName, lastName, GREEN, isMultiUser, email)
            val isSuccessful = primaryUser.add(progressBar, password, confirmPassword)
            if (!isSuccessful) return@click
            if (!isSignedIn(this)) {

                // Shows The Error Dialog//
                showGeneralDialog(
                    this,
                    R.string.title_error,
                    R.string.error_general
                )
                return@click
            }

            // Starts The UserActivation Activity//
            activate(this)
            startActivity(UserActivation::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When uiUserSignUp Is Clicked//
        click(uiUserSignUp) {
            firstName.clearFocus()
            lastName.clearFocus()
            email.clearFocus()
            password.clearFocus()
            confirmPassword.clearFocus()
        }
    }
}