package dev.jzdevelopers.cstracker.user

import android.content.Context
import androidx.core.util.PatternsCompat
import com.google.firebase.auth.FirebaseAuth
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import java.util.*
import kotlin.collections.ArrayList

/** Data Class PrimaryUser
 *  Data class that handles the primary user objects
 *  @author Jordan Zimmitti, Marcus Novoa
 */
data class PrimaryUser(
            var isMultiUser      : Boolean = false,
            var email            : String  = "",
            var password         : String  = "",
            var secondaryUserIds : ArrayList<Int> = ArrayList(),
) : User() {

    // Gets The Firebase Authorization Instance//
    private val firebaseAuth = FirebaseAuth.getInstance()

    /**.
     * Function That Saves A Primary User To The Database
     * @param [context]         Gets the instance from the caller activity
     * @param [confirmPassword] Used for making sure the password was inputted properly
     * @param [onSuccess]       The invoked function for when the primary user is saved successfully (lambda)
     * @param [onFail]          The invoked function for when the primary user is not saved successfully (lambda)
     */
    fun save(context: Context, confirmPassword: String, onSuccess:() -> Unit, onFail:() -> Unit) {

        // Checks If The User Input Is Valid//
        if (!super.save(context))   return
        if (!isValidEmail(context)) return
        if (!isValidPassword(context, confirmPassword)) return

        // Takes The User Data And Prepares It For The Database//
        userToSave["email"]            = email
        userToSave["password"]         = password
        userToSave["secondaryUserIds"] = secondaryUserIds

        // Saves The New User To The Database//
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess.invoke() }
            .addOnFailureListener { onFail.invoke() }
    }

    /**.
     * Function That Checks And Formats The Email For Validity
     * @param [context] Gets the instance from the caller activity
     * @return whether the email is valid
     */
    private fun isValidEmail(context: Context): Boolean {

        // Uses Android Email Pattern For Checking Email Syntax//
        val emailValidator = PatternsCompat.EMAIL_ADDRESS

        // Checks The Email For Validity//
        return when {

            // When The Email Is Blank//
            email.isBlank() -> {
                JZActivity.showErrorDialog(
                    context,
                    R.string.user_sign_up_error_title,
                    R.string.error_email_blank
                )
                false
            }

            // When The Email Does Not Have Valid Syntax//
            !emailValidator.matcher(email).matches() -> {
                JZActivity.showErrorDialog(
                    context,
                    R.string.user_sign_up_error_title,
                    R.string.error_email_match
                )
                false
            }

            // When The Email Is Valid//
            else -> {
                email = email
                    .trim()
                    .toLowerCase(Locale.getDefault())
                true
            }
        }
    }

    /**.
     * Function That Checks And Formats The Password For Validity
     * @param [context]         Gets the instance from the caller activity
     * @param [confirmPassword] Used for making sure the password was inputted properly
     * @return whether the password is valid
     */
    private fun isValidPassword(context: Context, confirmPassword: String): Boolean {

        // Checks The Password For Validity//
        return when {

            // When The Password Is Blank//
            password.isBlank() -> {
                JZActivity.showErrorDialog(
                    context,
                    R.string.user_sign_up_error_title,
                    R.string.error_password_blank
                )
                false
            }

            // When The Password Is Less Than Twelve Characters//
            password.length < 12 -> {
                JZActivity.showErrorDialog(
                    context,
                    R.string.user_sign_up_error_title,
                    R.string.error_password_short
                )
                false
            }

            // When The Two Passwords Do Not Match//
            password != confirmPassword -> {
                JZActivity.showErrorDialog(
                    context,
                    R.string.user_sign_up_error_title,
                    R.string.error_password_match
                )
                false
            }

            // When The Password Is Valid
            else -> {
                password = password.trim()
                true
            }
        }
    }
}