package dev.jzdevelopers.cstracker.user.models

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.core.util.PatternsCompat
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.libs.JZPrefs
import dev.jzdevelopers.cstracker.user.common.UserTheme
import dev.jzdevelopers.cstracker.user.common.UserTheme.GREEN
import kotlinx.coroutines.tasks.await
import java.util.*

/** Kotlin Class PrimaryUser,
 *  Class That Handles The Primary-User Objects
 *  @author Jordan Zimmitti, Marcus Novoa
 *  @param [context]     Gets the instance from the caller activity
 *  @param [firstName]   The first-name of the primary-user
 *  @param [lastName]    The last-name of the primary-user
 *  @param [theme]       The theme for the primary-user and default theme for all secondary-users
 *  @param [isMultiUser] Whether The Primary-User Is Keeping Track Of Secondary-Users
 *  @param [email]       The email-address of the primary-user
 */
class PrimaryUser(
    context         : Context?  = null,
    firstName       : String    = "",
    lastName        : String    = "",
    theme           : UserTheme = GREEN,
    var isMultiUser : Boolean   = false,
    var email       : String    = "",
): User(context, firstName, lastName, theme) {

    /**.
     * Configures Static Functions And Variables
     */
    companion object {

        // Define And Initialize Saved Preference Value//
        private const val PREF_MULTI_USER = "dev.jzdevelopers.cstracker.prefMultiUser"

        /**.
         * Function That Gets The Cached Multi-User Preference
         * @param [context] Gets the instance from the caller activity
         * @return Whether the primary-user is keeping track of secondary-users
         */
        fun getCachedMultiUser(context: Context): Boolean {

            // Returns The Multi-User Preference//
            return JZPrefs.getPref(context, PREF_MULTI_USER, false)
        }

        /**.
         * Function That Gets The Primary User's Id
         * @param [context] Gets the instance from the caller activity
         * @return The primary user's id
         */
        fun getId(context: Context): String {
            return try {

                // Gets The User's Id//
                firebaseAuth.currentUser?.uid ?: throw Error()
            }
            catch (_: Exception) {
                showGeneralError(context)
                ""
            }
        }

        /**.
         * Function That Signs The User Out
         */
        fun signOut() {

            // Signs The User Out//
            firebaseAuth.signOut()

            // Logs That The Primary-User Was Signed-Out//
            Log.v("Primary_User", "Primary user was signed out")
        }

        /**.
         * Function That Checks Whether The Primary-User's Account Is Activated
         * @param [context] Gets the instance from the caller activity
         * @return Whether the primary-user's Account is activated
         */
        suspend fun isActivated(context: Context): Boolean {
            return try {

                // Reloads The Current State Of The Primary-User//
                firebaseAuth.currentUser?.reload()?.await()

                // Gets The Primary-User's Id//
                val user = firebaseAuth.currentUser ?: throw Error()

                // Gets Whether The Primary-User's Email Is Verified//
                val isVerified = user.isEmailVerified

                // Checks If The Email Is Verified//
                if (isVerified) true
                else {

                    // Shows The Error Dialog//
                    JZActivity.showGeneralDialog(
                        context,
                        R.string.title_error,
                        R.string.error_email_verification
                    )
                    false
                }
            }
            catch (_: Exception) {
                showGeneralError(context)
                false
            }
        }

        /**.
         * Function That Sends A Verification Email To The User
         * @param [context] Gets the instance from the caller activity
         */
        suspend fun activate(context: Context) {
            try {

                // Send An Email Verification To The User//
                val user = firebaseAuth.currentUser ?: throw Error()
                user.sendEmailVerification().await()

                // Logs That An Email Verification Was Sent//
                Log.v("Primary_User", "An email verification was sent")
            }
            catch (_: Exception) {
                showGeneralError(context)
            }
        }

        /**.
         * Function That Sends A Password Reset Request To The Inputted Email-Address
         * @param [context] Gets the instance from the caller activity
         * @param [email]   The inputted email-address of the user that needs their password reset
         */
        suspend fun passwordReset(context: Context, email: String) {
            try {

                // Sends The Password Reset Prompt//
                firebaseAuth.sendPasswordResetEmail(email).await()

                // Shows The Confirmation Dialog//
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_request,
                    R.string.general_reset_password
                )

                // Logs That A Password Reset Request Was Sent//
                Log.v("Primary_User", "A password reset request was sent for $email")
            }
            catch (_: Exception) {
                showGeneralError(context)
            }
        }

        /**.
         * Function That Authenticates A Primary-User
         * @param [loadingBar] Circular progress bar to alert the user when the sign-in is in progress
         * @param [password]   The password of the user
         * @return Whether the sign-in was successful
         */
        suspend fun signIn(context: Context, loadingBar: ProgressBar, email: String, password: String): Boolean {
            try {

                // Shows The Progress Bar//
                loadingBar.visibility = View.VISIBLE

                // Authenticates The User//
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                if (!isSignedIn()) return false

                // Gets The Primary-User's Id//
                val userId = firebaseAuth.currentUser?.uid ?: throw Error()

                // Gets The Primary-User Document From The Database//
                val collection = fireStore.collection("PrimaryUsers").document(userId)
                val document   = collection.get().await()

                // Gets Whether The Primary-User Is Keeping Track Of Secondary-Users//
                val isMultiUser = document.data?.get("multiUser") as Boolean

                // Hides The Loading Bar//
                loadingBar.visibility = View.GONE

                // Saves Primary-User's Multi-User Preference//
                JZPrefs.savePref(context, PREF_MULTI_USER, isMultiUser)

                // Logs That The Primary-User Was Signed In//
                Log.v("Primary_User", "Primary user [$email] was signed in")
                return true
            }
            catch (_: Exception) {
                loadingBar.visibility = View.GONE
                showGeneralError(context)
                signOut()
                return false
            }
        }

        /**.
         * Function That Shows A General Error Dialog
         * @param [context] The instance from the caller activity
         */
        private fun showGeneralError(context: Context) {

            // Shows The Error Dialog//
            JZActivity.showGeneralDialog(
                context,
                R.string.title_error,
                R.string.error_general
            )
        }
    }

    /**.
     * Function That Authenticates And Adds A Primary-User To The Database
     * @param [loadingBar]      Circular progress bar to alert the user when the sign-up is in progress
     * @param [password]        The password of the user
     * @param [confirmPassword] The password to confirm that the first password was entered in correctly
     * @return Whether the primary-user was added successfully
     */
    suspend fun add(loadingBar: ProgressBar, password: String, confirmPassword: String): Boolean {
        try {

            // When Context Is Null//
            if (context == null) {

                // Throws A Runtime Error//
                throw NullPointerException("Context must not be null")
            }

            // Checks If The User Input Is Valid//
            if (!super.add(loadingBar)) return false
            if (!isValidEmail()) return false
            if (!isValidPassword(password, confirmPassword)) return false

            // Shows The Loading Bar//
            loadingBar.visibility = View.VISIBLE

            // Signs-Up The User//
            firebaseAuth.createUserWithEmailAndPassword(email, password.trim()).await()

            // Gets The Newly Created Primary-User's Id//
            val userId = firebaseAuth.currentUser?.uid ?: throw Error()

            // Sends The Primary-User Data To The Database//
            val document = fireStore.collection("PrimaryUsers").document(userId)
            document.set(this).await()

            // Hides The Loading Bar//
            loadingBar.visibility = View.GONE

            // Saves Primary-User's Multi-User Preference//
            JZPrefs.savePref(context, PREF_MULTI_USER, isMultiUser)

            // Logs That The Primary-User Was Added Successfully//
            Log.v("Primary_User", "Primary user [$email] was added successfully")
            return true
        }
        catch (_: Exception) {
            loadingBar.visibility = View.GONE
            showGeneralError()
            return false
        }
    }

    /**.
     * Function That Deletes A Primary-User In The Database
     * @return Whether the primary-user was deleted successfully
     */
    suspend fun delete(): Boolean {
        try {

            // When Context Is Null//
            if (context == null) {

                // Throws A Runtime Error//
                throw NullPointerException("Context must not be null")
            }

            // Gets The Signed-In User//
            val user = firebaseAuth.currentUser ?: throw Error()

            // Deletes The Primary-User And Its Data From The Database//
            val document = fireStore.collection("PrimaryUsers").document(user.uid)
            document.delete().await()
            user.delete()

            // Saves Primary-User's Multi-User Preference//
            JZPrefs.savePref(context, PREF_MULTI_USER, isMultiUser)

            // Logs That The Primary-User Was Deleted Successfully//
            Log.v("Primary_User", "Primary user [$email] has been deleted")
            return true
        }
        catch (_: Exception) {
            showGeneralError()
            return false
        }
    }

    /**.
     * Function That Edits A Primary-User In The Database
     * @param [loadingBar] Circular progress bar to alert the user when the edit is in progress
     * @return Whether the primary-user was edited successfully
     */
    suspend fun edit(loadingBar: ProgressBar): Boolean {
        try {

            // When Context Is Null//
            if (context == null) {

                // Throws A Runtime Error//
                throw NullPointerException("Context must not be null")
            }

            // Checks If The User Input Is Valid//
            if (!super.edit("", loadingBar)) return false

            // Shows The Loading Bar//
            loadingBar.visibility = View.VISIBLE

            // Gets The Primary-Users Id//
            val id = getId(context)

            // Sends The Edited User Data To The Database//
            val document = fireStore.collection("PrimaryUsers").document(id)
            document.set(this)

            // Hides The Loading Bar//
            loadingBar.visibility = View.GONE

            // Saves Primary-User's Multi-User Preference//
            JZPrefs.savePref(context, PREF_MULTI_USER, isMultiUser)

            // Logs That The Primary-User Was Edited Successfully//
            Log.v("Primary_User", "Primary user [$email] has been edited")
            return true
        }
        catch (_: Exception) {
            loadingBar.visibility = View.GONE
            showGeneralError()
            return false
        }
    }

    /**.
     * Function That Checks And Formats The Email For Validity
     * @return Whether the email is valid
     */
    private fun isValidEmail(): Boolean {

        // When Context Is Null//
        if (context == null) {

            // Throws A Runtime Error//
            throw NullPointerException("Context must not be null")
        }

        // Uses The Android Email Pattern For Checking Email Syntax//
        val emailValidator = PatternsCompat.EMAIL_ADDRESS

        // Checks The Email For Validity//
        return when {

            // When The Email Is Blank//
            email.isBlank() -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_email_blank
                )
                false
            }

            // When The Email Does Not Have Valid Syntax//
            !emailValidator.matcher(email).matches() -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
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
     * @param [password]        The password of the user
     * @param [confirmPassword] The password to confirm that the first password was entered in correctly
     * @return Whether the password is valid
     */
    private fun isValidPassword(password: String, confirmPassword: String): Boolean {

        // When Context Is Null//
        if (context == null) {

            // Throws A Runtime Error//
            throw NullPointerException("Context must not be null")
        }

        // Checks The Password For Validity//
        return when {

            // When The Password Is Blank//
            password.isBlank() -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_password_blank
                )
                false
            }

            // When The Password Is Less Than Twelve Characters//
            password.length < 12 -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_password_short
                )
                false
            }

            // When The Two Passwords Do Not Match//
            password != confirmPassword -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_password_match
                )
                false
            }

            // When The Password Is Valid
            else -> true
        }
    }
}