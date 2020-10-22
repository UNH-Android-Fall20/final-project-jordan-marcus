package dev.jzdevelopers.cstracker.user.data_classes

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.UserOrder
import dev.jzdevelopers.cstracker.user.UserOrder.*
import dev.jzdevelopers.cstracker.user.UserTheme
import dev.jzdevelopers.cstracker.user.UserTheme.GREEN
import kotlinx.coroutines.tasks.await
import java.util.Locale.getDefault
import java.util.UUID.randomUUID

/** Kotlin Class SecondaryUser,
 *  Class That Handles The Secondary-User Objects
 *  @author Jordan Zimmitti, Marcus Novoa
 *  @param [context]       The instance from the caller activity
 *  @param [firstName]     The first-name of the secondary-user
 *  @param [lastName]      The last-name of the secondary-user
 *  @param [theme]         The custom theme for the secondary-user
 *  @param [goal]          The amount of hours the secondary-user wants to achieve
 *  @param [goalProgress]  The progress the secondary-user is at towards their goal
 *  @param [grade]         The grade of the secondary-user
 *  @param [nameLetter]    The first letter of the secondary-user's first-name
 *  @param [organization]  The organization the secondary-user is completing hours for
 *  @param [primaryUserId] The id of the primary-user that the secondary-user belongs to
 *  @param [totalTime]     The total amount of hours the secondary-user has completed so far
 *  @param [icon]          The secondary-user's profile icon
 */
class SecondaryUser(
    context           : Context,
    firstName         : String    = "",
    lastName          : String    = "",
    theme             : UserTheme = GREEN,
    var goal          : Int       = 0,
    var goalProgress  : Int       = 0,
    var grade         : Int       = 0,
    var nameLetter    : String    = "",
    var organization  : String    = "",
    val primaryUserId : String    = "",
    var totalTime     : String    = "0:00",
    var icon          : Uri?      = null,
): User(context, firstName, lastName, theme) {

    /**.
     * Configures Static Functions And Variables
     */
    companion object {

        // Gets The Different FireBase Instances//
        private val fireStore   = FirebaseFirestore.getInstance()
        private val fireStorage = FirebaseStorage.getInstance()
        private val storage = fireStorage.getReferenceFromUrl("gs://cs-tracker-5b4d1.appspot.com")

        /**.
         * Function That Creates The Query For Getting All Of The Secondary-Users Under A Primary-User
         * @param [primaryUserId] The id of the primary-user
         * @param [order]         The order in which the secondary-users will be displayed
         * @return The query for getting all of the secondary-users under a primary-user
         */
        fun getAll(primaryUserId: String, order: UserOrder): Query {

            // Gets How To Order The Secondary-Users//
            val orderField = when(order) {
                FIRST_NAME   -> "firstName"
                GRADE        -> "grade"
                LAST_NAME    -> "lastName"
                ORGANIZATION -> "organization"
                TOTAL_TIME   -> "totalTime"
            }

            // Returns The Query//
            return fireStore
                .collection("SecondaryUsers")
                .whereIn("primaryUserId", mutableListOf(primaryUserId))
                .orderBy(orderField)
        }

        /**.
         * Function That Gets The Secondary-User
         * @param [context] Gets the instance from the caller activity
         * @param [id]      The id of the secondary-user
         * @return The secondary-user instance
         */
        suspend fun get(context: Context, id: String): SecondaryUser {
            try {

                // Gets The Secondary-User Data From The Database//
                val collection = fireStore.collection("SecondaryUsers").document(id)
                val document   = collection.get().await()

                // Gets The Basic Secondary-User Data//
                val goal          = document.data?.get("goal")          as Long
                val goalProgress  = document.data?.get("goalProgress")  as Long
                val grade         = document.data?.get("grade")         as Long
                val firstName     = document.data?.get("firstName")     as String
                val lastName      = document.data?.get("lastName")      as String
                val nameLetter    = document.data?.get("nameLetter")    as String
                val organization  = document.data?.get("organization")  as String
                val primaryUserId = document.data?.get("primaryUserId") as String
                val theme         = document.data?.get("theme")         as String
                val totalTime     = document.data?.get("totalTime")     as String

                // Gets The Icon Data//
                var icon: Uri? = null
                val iconId = document.data?.get("iconId") as String
                if (iconId != "") {
                    val iconToGet = storage.child("android/cs-tracker/$iconId")
                    icon = iconToGet.downloadUrl.await()
                }

                // Gets The Theme Enum From The Saved Theme String//
                val themeEnum = UserTheme.valueOf(theme)

                // Re-Creates The Primary-User Instance//
                val secondaryUser = SecondaryUser(context, firstName, lastName, themeEnum,
                    goal.toInt(),
                    goalProgress.toInt(),
                    grade.toInt(),
                    nameLetter,
                    organization,
                    primaryUserId,
                    totalTime,
                    icon
                )
                secondaryUser.id = id

                // Returns The Secondary User//
                Log.v("Primary_User", "Returned secondary user [$firstName $lastName]")
                return  secondaryUser
            }
            catch (_: Exception) {
                showGeneralError(context)
                return SecondaryUser(context)
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
     * Function That Signs-Up A Secondary-User To The Database
     * @param [progressBar] Circular progress bar to alert the user when the sign-up is in progress
     * @return Whether the sign-up was successful
     */
    suspend fun signUp(progressBar: ProgressBar): Boolean {
        try {

            // Checks If The User Input Is Valid//
            if (!super.signUp())        return false
            if (!isValidGoal())         return false
            if (!isValidGrade())        return false
            if (!isValidOrganization()) return false

            // Shows The Progress Bar//
            progressBar.visibility = VISIBLE

            // Saves The Icon To FireBase Storage If It Exists//
            val icon   = icon
            var iconId = ""
            val generatedIconId = randomUUID().toString()
            val iconToSave = storage.child("android/cs-tracker/$generatedIconId")
            if (icon != null) {
                iconToSave.putFile(icon).await()
                iconId = generatedIconId
            }

            // Takes The Secondary-User Data And Prepares It For The Database//
            userToSave["goal"]          = goal
            userToSave["goalProgress"]  = goalProgress
            userToSave["grade"]         = grade
            userToSave["iconId"]        = iconId
            userToSave["nameLetter"]    = nameLetter
            userToSave["organization"]  = organization
            userToSave["primaryUserId"] = primaryUserId
            userToSave["totalTime"]     = totalTime

            // Sends The Secondary-User Data To The Database//
            fireStore.collection("SecondaryUsers").add(userToSave).await()

            // Hides The Progress Bar//
            progressBar.visibility = GONE

            // Logs That A Secondary-User Was Added//
            Log.v("Secondary_User", "Secondary user [$firstName $lastName] has been added to primary user id: $primaryUserId")
            return true
        }
        catch (_: Exception) {
            progressBar.visibility = GONE
            showGeneralError()
            return false
        }
    }

    /**.
     * Function That Checks The Goal For Validity
     * @return Whether the goal is valid
     */
    private fun isValidGoal(): Boolean {

        // Checks The Goal For Validity//
        return when {

            // When The Goal Is Greater Than 100,000//
            goal > 100000 -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_goal_long
                )
                false
            }

            // When The Goal Is Valid
            else -> true
        }
    }

    /**.
     * Function That Checks The Grade For Validity
     * @return Whether the grade is valid
     */
    private fun isValidGrade(): Boolean {

        // Checks The Grade For Validity//
        return when {

            // When The Grade Is Greater Than 12//
            grade > 12 -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_grade_long
                )
                false
            }

            // When The Grade Is Valid
            else -> true
        }
    }

    /**.
     * Function That Checks The Organization For Validity
     * @return Whether the organization is valid
     */
    private fun isValidOrganization(): Boolean {

        // Checks The Organization For Validity//
        return when {

            // When The Organization Is Blank//
            organization.isBlank() -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_organization_blank
                )
                false
            }

            // When The Organization Has A Length Greater Than 30//
            organization.length > 40 -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_organization_long
                )
                false
            }

            // When The Organization Is Valid//
            else -> {
                organization = organization
                    .trim()
                    .toLowerCase(getDefault())
                    .split(" ")
                    .joinToString(" ") { it.capitalize(getDefault()) }
                true
            }
        }
    }
}