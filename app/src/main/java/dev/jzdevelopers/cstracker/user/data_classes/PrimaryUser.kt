package dev.jzdevelopers.cstracker.user.data_classes

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.core.util.PatternsCompat
import com.google.firebase.auth.FirebaseAuth
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.libs.JZPrefs
import dev.jzdevelopers.cstracker.libs.JZPrefs.savePref
import dev.jzdevelopers.cstracker.user.MultiUser
import dev.jzdevelopers.cstracker.user.MultiUser.SIGNED_OUT
import dev.jzdevelopers.cstracker.user.UserTheme
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

/** Data Class PrimaryUser
 *  Data class that handles the primary user objects
 *  @author Jordan Zimmitti, Marcus Novoa
 */
data class PrimaryUser(
    var multiUser        : MultiUser         = SIGNED_OUT,
    var email            : String            = "",
    val secondaryUserIds : ArrayList<String> = ArrayList()
) : User() {

    /**.
     * Configures Static Variables And Functions
     */
    companion object {

        // Define And Initialize Saved Preferences Value//
        private const val PREF_MULTI_USER = "dev.jzdevelopers.cstracker.prefMultiUser"

        // Gets The FireBase Authorization Instances//
        private val firebaseAuth = FirebaseAuth.getInstance()

        /**.
         * Function That Checks If The User Is Logged In
         * @param [context] Gets the instance from the caller activity
         */
        fun isSignedIn(context: Context): Boolean {

            // Checks If The User Is Signed In//
            val isSignedIn = isSignedIn()
            if (isSignedIn) return true

            // Signs Out The User//
            savePref(context, PREF_MULTI_USER, SIGNED_OUT.ordinal)
            return false
        }

        /**.
         * Function That Gets The Cached MultiUser
         * @param [context] Gets the instance from the caller activity
         * @return The multi-user value as an ordinal
         */
        fun getCachedMultiUser(context: Context): Int {

            // Returns The MultiUser Value As An Ordinal//
            return JZPrefs.getPref(context, PREF_MULTI_USER, SIGNED_OUT.ordinal)
        }

        /**.
         * Function That Signs Out The User
         * @param [context] Gets the instance from the caller activity
         */
        fun signOut(context: Context) {

            // Clears Primary User's Multi-User Preference//
            savePref(context, PREF_MULTI_USER, SIGNED_OUT.ordinal)
            firebaseAuth.signOut()

            // Logs That The Primary User Was Signed Out//
            Log.v("Primary_User", "Primary user was signed out")
        }

        /**.
         * Function That Checks Whether The Primary User's Account Is Activated
         * @param [context] Gets the instance from the caller activity
         * @return Whether the primary user's Account is activated
         */
        suspend fun isActivated(context: Context): Boolean {
            return try {

                // Send An Email Verification To The User//
                firebaseAuth.currentUser?.reload()?.await()
                val user = firebaseAuth.currentUser ?: throw Error()
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

                // Shows The Error Dialog//
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_general
                )
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

                // Shows The Error Dialog//
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_general
                )
            }
        }
    }

    /**.
     * Function That Gets The Logged In User
     * @param [context] Gets the instance from the caller activity
     * @return The primary user instance
     */
    suspend fun get(context: Context): PrimaryUser {
        try {

            // Gets The User's Id//
            val userId = firebaseAuth.currentUser?.uid ?: throw Error()

            // Gets The User Data From The Database//
            val collection = fireStore.collection("PrimaryUsers").document(userId)
            val document   = collection.get().await()

            // Sets The Basic User Data//
            this.id          = userId
            this.firstName   = document.data?.get("firstName") as String
            this.lastName    = document.data?.get("lastName")  as String
            this.email       = document.data?.get("email")     as String

            // Sets The Enum Data//
            this.multiUser   = MultiUser.valueOf((document.data?.get("multiUser") as String))
            this.theme       = UserTheme.valueOf((document.data?.get("theme") as String))

            // Sets The Secondary User Ids//
            val secondaryUserIds = document.data?.get("secondaryUserIds") as ArrayList<*>
            secondaryUserIds.forEach { id -> this.secondaryUserIds.add(id as String) }

            // Returns The Logged In User//
            Log.v("Primary_User", "Returned primary user [$email]")
            return this
        }
        catch (_: Exception) {
            showGeneralError(context)
            return PrimaryUser()
        }
    }

    /**.
     * Function That Sends A Password Reset Request To The Inputted Email Address
     * @param [context] Gets the instance from the caller activity
     * @param [email]   The inputted email address of the user that needs their password reset
     */
    suspend fun passwordReset(context: Context, email: String) {
        try {

            // Sets The Email//
            this.email = email

            // Checks If The Email Is Valid//
            if (!isValidEmail(context)) return

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
     * Function That Authenticates A Primary User
     * @param [context]     Gets the instance from the caller activity
     * @param [progressBar] Circular progress bar to alert the user when the sign-up is in progress
     * @param [password]    The password of the user
     */
    suspend fun signIn(context: Context, progressBar: ProgressBar, password: String) {
        try {

            // Checks If The Email Is Valid//
            if (!isValidEmail(context)) return

            // Shows The Progress Bar//
            progressBar.visibility = View.VISIBLE

            // Authenticate The User//
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            if (!isSignedIn()) return

            // Hides The Progress Bar//
            progressBar.visibility = View.GONE

            // Gets The User Data//
            get(context)

            // Saves Primary User's Multi-User Preference//
            savePref(context, PREF_MULTI_USER, multiUser.ordinal)

            // Logs That The Primary User Was Signed In//
            Log.v("Primary_User", "Primary user [$email] was signed in")
        }
        catch (_: Exception) {
            progressBar.visibility = View.GONE
            showGeneralError(context)
        }
    }

    /**.
     * Function That Signs Up And Authenticates A Primary User To The Database
     * @param [context]         Gets the instance from the caller activity
     * @param [progressBar]     Circular progress bar to alert the user when the sign-up is in progress
     * @param [password]        The password of the user
     * @param [confirmPassword] The password to confirm that the first password was entered in correctly
     */
    suspend fun signUp(context: Context, progressBar: ProgressBar, password: String, confirmPassword: String) {
        try {

            // Checks If The User Input Is Valid//
            if (!super.signUp(context)) return
            if (!isValidEmail(context)) return
            if(!isValidPassword(context, password, confirmPassword)) return

            // Takes The User Data And Prepares It For The Database//
            userToSave["multiUser"]        = multiUser
            userToSave["email"]            = email
            userToSave["secondaryUserIds"] = secondaryUserIds

            // Shows The Progress Bar//
            progressBar.visibility = View.VISIBLE

            // Signs Up The User//
            firebaseAuth.createUserWithEmailAndPassword(email, password.trim()).await()

            // Gets The User Id//
            val userId = firebaseAuth.currentUser?.uid ?: throw Error()

            // Sends The User Data To The Database//
            val collection = fireStore.collection("PrimaryUsers").document(userId)
            collection.set(userToSave).await()

            // Hides The Progress Bar//
            progressBar.visibility = View.GONE

            // Saves Primary User's Multi-User Preference//
            savePref(context, PREF_MULTI_USER, multiUser.ordinal)

            // Logs That The Primary User Was Signed In//
            Log.v("Primary_User", "Primary user [$email] has signed up")
        }
        catch (_: Exception) {
            progressBar.visibility = View.GONE
            showGeneralError(context)
        }
    }

    /**.
     * Function That Updates The Primary User
     * @param [context]     Gets the instance from the caller activity
     * @param [progressBar] Circular progress bar to alert the user when the sign-up is in progress
     */
    suspend fun updateData(context: Context, progressBar: ProgressBar) {
        try {

            // Checks If The User Input Is Valid//
            if (!super.update(context)) return

            // Takes The User Data And Prepares It For The Database//
            userToUpdate["multiUser"]        = multiUser
            userToUpdate["secondaryUserIds"] = secondaryUserIds

            // Shows The Progress Bar//
            progressBar.visibility = View.VISIBLE

            // Updates The User Data//
            val collection = fireStore.collection("PrimaryUsers").document(id)
            collection.update(userToUpdate).await()

            // Hides The Progress Bar//
            progressBar.visibility = View.GONE

            // Saves Primary User's Multi-User Preference//
            savePref(context, PREF_MULTI_USER, multiUser.ordinal)

            // Logs That The Primary User Was Signed In//
            Log.v("Primary_User", "Primary user [$email] has been updated")
        }
        catch (_: Exception) {
            progressBar.visibility = View.GONE
            showGeneralError(context)
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
     * @param [context]         Gets the instance from the caller activity
     * @param [confirmPassword] Used for making sure the password was inputted properly
     * @return whether the password is valid
     */
    private fun isValidPassword(context: Context, password: String, confirmPassword: String): Boolean {

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