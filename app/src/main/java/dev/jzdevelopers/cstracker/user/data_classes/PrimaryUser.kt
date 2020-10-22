package dev.jzdevelopers.cstracker.user.data_classes

import android.content.Context
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import androidx.core.util.PatternsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.libs.JZPrefs
import dev.jzdevelopers.cstracker.libs.JZPrefs.savePref
import dev.jzdevelopers.cstracker.user.UserTheme
import dev.jzdevelopers.cstracker.user.UserTheme.GREEN
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

/** Kotlin Class PrimaryUser,
 *  Class That Handles The Primary-User Objects
 *  @author Jordan Zimmitti, Marcus Novoa
 *  @param [context]     The instance from the caller activity
 *  @param [firstName]   The first-name of the primary-user
 *  @param [lastName]    The last-name of the primary-user
 *  @param [theme]       The theme for the primary-user and default theme for all secondary-users
 *  @param [isMultiUser] Whether The Primary-User Is Keeping Track Of Secondary-Users
 *  @param [email]       The email-address of the primary-user
 */
class PrimaryUser(
    context         : Context,
    firstName       : String    = "",
    lastName        : String    = "",
    theme           : UserTheme = GREEN,
    var isMultiUser : Boolean   = false,
    var email       : String    = "",
): User(context, firstName, lastName, theme) {

    //<editor-fold desc="Class Variables">

    /**
     * A List Of Secondary-User Profiles Under The Primary-User
     */
    var secondaryUsers: List<SecondaryUser> = ArrayList()
        private set

    // Define And Instantiate ArrayList Value//
    private val secondaryUserIds = ArrayList<String>()

    //</editor-fold>

    /**.
     * Configures Static Functions And Variables
     */
    companion object {

        // Define And Initialize Saved Preference Value//
        private const val PREF_MULTI_USER = "dev.jzdevelopers.cstracker.prefMultiUser"

        // Gets The Different FireBase Instances//
        private val firebaseAuth = FirebaseAuth.getInstance()
        private val fireStore    = FirebaseFirestore.getInstance()

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
         * Function That Gets The Signed-In User
         * @param [context] Gets the instance from the caller activity
         * @return The primary-user instance
         */
        suspend fun get(context: Context): PrimaryUser {
            try {

                // Gets The User's Id//
                val userId = firebaseAuth.currentUser?.uid ?: throw Error()

                // Gets The Primary User Data From The Database//
                val collection = fireStore.collection("PrimaryUsers").document(userId)
                val document   = collection.get().await()

                // Gets The Basic Primary-User Data//
                val secondaryUserIds = document.data?.get("secondaryUserIds") as ArrayList<*>
                val isMultiUser      = document.data?.get("isMultiUser")      as Boolean
                val email            = document.data?.get("email")            as String
                val firstName        = document.data?.get("firstName")        as String
                val lastName         = document.data?.get("lastName")         as String
                val theme            = document.data?.get("theme")            as String

                // Gets The Theme Enum From The Saved Theme String//
                val themeEnum = UserTheme.valueOf(theme)

                // Define And Initialize ArrayList Value For All The Found Secondary-Users//
                val foundSecondaryUsers = ArrayList<SecondaryUser>()

                // Re-Creates The Primary-User Instance//
                val primaryUser = PrimaryUser(context, firstName, lastName, themeEnum, isMultiUser, email)
                primaryUser.id = userId
                secondaryUserIds.forEach { id ->

                    // Adds The Secondary-User Id To The Current Instance//
                    primaryUser.secondaryUserIds.add(id as String)

                    // Adds The Found Secondary-User Instance To Be Saved//
                    foundSecondaryUsers.add(SecondaryUser.get(context, id))
                }
                primaryUser.secondaryUsers = foundSecondaryUsers

                // Returns The Signed-In User//
                Log.v("Primary_User", "Returned primary user [$email]")
                return primaryUser
            }
            catch (_: Exception) {
                showGeneralError(context)
                return PrimaryUser(context)
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
         * @param [progressBar] Circular progress bar to alert the user when the sign-in is in progress
         * @param [password]    The password of the user
         * @return Whether the sign-in was successful
         */
        suspend fun signIn(context: Context, progressBar: ProgressBar, email: String, password: String): Boolean {
            try {

                // Shows The Progress Bar//
                progressBar.visibility = VISIBLE

                // Authenticates The User//
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                if (!isSignedIn()) return false

                // Gets The Primary-User's Id//
                val userId = firebaseAuth.currentUser?.uid ?: throw Error()

                // Gets The Primary-User Document From The Database//
                val collection = fireStore.collection("PrimaryUsers").document(userId)
                val document   = collection.get().await()

                // Gets Whether The Primary-User Is Keeping Track Of Secondary-Users//
                val isMultiUser = document.data?.get("isMultiUser") as Boolean

                // Hides The Progress Bar//
                progressBar.visibility = GONE

                // Saves Primary-User's Multi-User Preference//
                savePref(context, PREF_MULTI_USER, isMultiUser)

                // Logs That The Primary-User Was Signed In//
                Log.v("Primary_User", "Primary user [$email] was signed in")
                return true
            }
            catch (_: Exception) {
                progressBar.visibility = GONE
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
     * Function That Authenticates And Signs-Up A Primary-User To The Database
     * @param [progressBar]     Circular progress bar to alert the user when the sign-up is in progress
     * @param [password]        The password of the user
     * @param [confirmPassword] The password to confirm that the first password was entered in correctly
     * @return Whether the sign-up was successful
     */
    suspend fun signUp(progressBar: ProgressBar, password: String, confirmPassword: String): Boolean {
        try {

            // Checks If The User Input Is Valid//
            if (!super.signUp()) return false
            if (!isValidEmail()) return false
            if (!isValidPassword(password, confirmPassword)) return false

            // Takes The Primary-User Data And Prepares It For The Database//
            userToSave["isMultiUser"]      = isMultiUser
            userToSave["email"]            = email
            userToSave["secondaryUserIds"] = secondaryUserIds

            // Shows The Progress Bar//
            progressBar.visibility = VISIBLE

            // Signs-Up The User//
            firebaseAuth.createUserWithEmailAndPassword(email, password.trim()).await()

            // Gets The Newly Created Primary-User's Id//
            val userId = firebaseAuth.currentUser?.uid ?: throw Error()

            // Sends The Primary-User Data To The Database//
            val collection = fireStore.collection("PrimaryUsers").document(userId)
            collection.set(userToSave).await()

            // Hides The Progress Bar//
            progressBar.visibility = GONE

            // Saves Primary-User's Multi-User Preference//
            savePref(context, PREF_MULTI_USER, isMultiUser)

            // Logs That The Primary-User Has Signed-Up//
            Log.v("Primary_User", "Primary user [$email] has signed up")
            return true
        }
        catch (_: Exception) {
            progressBar.visibility = GONE
            showGeneralError()
            return false
        }
    }

    /**.
     * Function That Updates The Primary-User Data
     * @param [progressBar] Circular progress bar to alert the user when the update is in progress
     * @return Whether the update was successful
     */
    suspend fun updateData(progressBar: ProgressBar, secondaryUserId: String = ""): Boolean {
        try {

            // Checks If The User Input Is Valid//
            if (!super.update()) return false

            // When There Is A New Secondary-User Id//
            if (secondaryUserId != "") {

                // Adds The New Secondary-User Id//
                secondaryUserIds.add(secondaryUserId)
            }

            // Takes The User Data And Prepares It For The Database//
            userToUpdate["isMultiUser"]      = isMultiUser
            userToUpdate["secondaryUserIds"] = secondaryUserIds

            // Shows The Progress Bar//
            progressBar.visibility = VISIBLE

            // Sends The Updated User Data To The Database//
            val document = fireStore.collection("PrimaryUsers").document(id)
            document.update(userToUpdate).await()

            // Hides The Progress Bar//
            progressBar.visibility = GONE

            // Saves Primary-User's Multi-User Preference//
            savePref(context, PREF_MULTI_USER, isMultiUser)

            // Logs That The Primary User Was Signed In//
            Log.v("Primary_User", "Primary user [$email] has been updated")
            return true
        }
        catch (_: Exception) {
            progressBar.visibility = GONE
            showGeneralError(context)
            return false
        }
    }

    /**.
     * Function That Checks And Formats The Email For Validity
     * @return Whether the email is valid
     */
    private fun isValidEmail(): Boolean {

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
     * @param [confirmPassword] The password to confirm that the first password was entered in correctly
     * @return Whether the password is valid
     */
    private fun isValidPassword(password: String, confirmPassword: String): Boolean {

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