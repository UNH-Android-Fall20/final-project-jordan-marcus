package dev.jzdevelopers.cstracker.user

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import androidx.core.util.PatternsCompat
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import java.util.*
import kotlin.collections.ArrayList

/** Data Class PrimaryUser
 *  Data class that handles the primary user objects
 *  @author Jordan Zimmitti, Marcus Novoa
 */
data class PrimaryUser(
    var isMultiUser      : Boolean        = false,
    var email            : String         = "",
    val secondaryUserIds : ArrayList<Int> = ArrayList(),
) : User() {

    /**.
     * Function That Authenticates And Saves A Primary User To The Database
     * @param [context]         Gets the instance from the caller activity
     * @param [progressBar]     Circular progress bar to alert the user when the sign-up is in progress
     * @param [onSuccess]       The invoked function for when the primary user is saved successfully (lambda)
     */
    fun save(
        context         : Context,
        progressBar     : ProgressBar,
        password        : String,
        confirmPassword : String,
        onSuccess       : () -> Unit
    ) {

        // Checks If The User Input Is Valid//
        if (!super.save(context))   return
        if (!isValidEmail(context)) return
        if (!isValidPassword(context, password, confirmPassword)) return

        // Takes The User Data And Prepares It For The Database//
        userToSave["isMultiUser"]      = isMultiUser
        userToSave["email"]            = email
        userToSave["secondaryUserIds"] = secondaryUserIds

        // Shows The Progress Bar//
        progressBar.visibility = View.VISIBLE

        // Signs Up The User//
        firebaseAuth.createUserWithEmailAndPassword(email, password.trim())
            .addOnFailureListener {

                // Hides The Progress Bar//
                progressBar.visibility = View.GONE

                // Hides The Error Dialog//
                JZActivity.showErrorDialog(
                    context,
                    R.string.user_sign_up_error_title,
                    R.string.error_general
                )
            }
            .addOnSuccessListener {

                // Gets The Newly Created User Id//
                id = firebaseAuth.currentUser?.uid ?: return@addOnSuccessListener

                // Saves The User Data To The Database//
                val primaryUser = fireStore.collection("Users").document(id)
                primaryUser.set(userToSave)

                // Hides The Progress Bar//
                progressBar.visibility = View.GONE

                // The User Was Signed In Successfully//
                onSuccess.invoke()
            }
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
    private fun isValidPassword(
        context         : Context,
        password        : String,
        confirmPassword : String
    ): Boolean {

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
            else -> true
        }
    }
}