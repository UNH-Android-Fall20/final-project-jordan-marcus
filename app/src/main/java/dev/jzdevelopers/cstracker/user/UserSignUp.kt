package dev.jzdevelopers.cstracker.user

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.MultiUser.NO
import dev.jzdevelopers.cstracker.user.MultiUser.YES
import dev.jzdevelopers.cstracker.user.PrimaryUser.Companion.activate
import dev.jzdevelopers.cstracker.user.PrimaryUser.Companion.isSignedIn
import kotlinx.android.synthetic.main.ui_user_sign_up.*

/** Android Activity UserSignUp
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

            // Sets The Theme For The Activity//
            theme(R.style.GreenTheme, false)
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

            // Gets The Multi-User Enum State//
            val multiUser = if (isMultiUser) YES else NO

            // Define And Instantiates The New Primary User//
            val primaryUser = PrimaryUser(multiUser, email)
            primaryUser.firstName = firstName
            primaryUser.lastName  = lastName

            // Signs Up The New Primary User//
            primaryUser.signUp(this, progressBar, password, confirmPassword)

            // When The Primary User Is Not Signed In//
            if (!isSignedIn(this)) {

                // Shows The Error Dialog//
                showGeneralDialog(
                    this,
                    R.string.title_user_sign_up_error,
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