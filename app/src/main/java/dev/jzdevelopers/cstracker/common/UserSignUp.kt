package dev.jzdevelopers.cstracker.common

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.EventView
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.PrimaryUser
import dev.jzdevelopers.cstracker.user.SecondaryUserAdd
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
        clickBack { exitApp() }

        // When uiUserSignUp Is Clicked//
        click(uiUserSignUp) {
            firstName.clearFocus()
            lastName.clearFocus()
            email.clearFocus()
            password.clearFocus()
            confirmPassword.clearFocus()
        }

        // When signUp Is Clicked//
        click(signUp) {

            // Gets The Confirm Password Text//
            val confirmPassword = confirmPassword.text.toString()
            val isMultiUser     = signUpCheckBox.isChecked

            // Define And Instantiates The Primary User//
            val primaryUser = PrimaryUser()
            primaryUser.firstName   = firstName.text.toString()
            primaryUser.lastName    = lastName.text.toString()
            primaryUser.email       = email.text.toString()
            primaryUser.password    = password.text.toString()
            primaryUser.isMultiUser = isMultiUser

            // Code For When The Sign-Up Is Successful//
            val onSuccess = {

                // Hides The Progress Bar//
                progressBar.hide()

                // Logs The User In//
                when(isMultiUser) {
                    true  -> startActivity(SecondaryUserAdd::class, R.anim.faze_in, R.anim.faze_out)
                    false -> startActivity(EventView::class, R.anim.faze_in, R.anim.faze_out)
                }
            }

            // Code For When The Sign-Up Is Not Successful//
            val onFail = {

                // Hides The Progress Bar//
                progressBar.hide()

                // Hides The Error Dialog//
                showErrorDialog(
                    this,
                    R.string.user_sign_up_error_title,
                    R.string.error_general
                )
            }

            // Saves The New User//
            progressBar.show()
            primaryUser.save(this, confirmPassword, onSuccess, onFail)
        }
    }
}