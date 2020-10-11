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

            // Gets The Primary User Data//
            val firstName       = firstName.text.toString()
            val lastName        = lastName.text.toString()
            val email           = email.text.toString()
            val password        = password.text.toString()
            val confirmPassword = confirmPassword.text.toString()
            val isMultiUser     = signUpCheckBox.isChecked

            // Define And Instantiates The Primary User//
            val primaryUser = PrimaryUser(isMultiUser, email)
            primaryUser.firstName = firstName
            primaryUser.lastName  = lastName

            // Saves The New User//
            primaryUser.save(this, progressBar, password, confirmPassword) {

                // Logs The User In//
                when(isMultiUser) {
                    true  -> startActivity(SecondaryUserAdd::class, R.anim.faze_in, R.anim.faze_out)
                    false -> startActivity(EventView::class, R.anim.faze_in, R.anim.faze_out)
                }
            }
        }
    }
}