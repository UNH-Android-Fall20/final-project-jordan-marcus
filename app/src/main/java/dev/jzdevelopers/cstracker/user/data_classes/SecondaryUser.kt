package dev.jzdevelopers.cstracker.user.data_classes

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.UserTheme
import dev.jzdevelopers.cstracker.user.UserTheme.GREEN
import kotlinx.coroutines.tasks.await
import java.util.Locale.getDefault
import java.util.UUID.randomUUID

/** Kotlin Class SecondaryUser,
 *  Class That Handles The Secondary-User Objects
 *  @author Jordan Zimmitti, Marcus Novoa
 *  @param [context]      The instance from the caller activity
 *  @param [firstName]    The first-name of the secondary-user
 *  @param [lastName]     The last-name of the secondary-user
 *  @param [theme]        The custom theme for the secondary-user
 *  @param [goal]         The amount of hours the secondary-user wants to achieve
 *  @param [goalProgress] The progress the secondary-user is at towards their goal
 *  @param [grade]        The grade of the secondary-user
 *  @param [nameLetter]   The first letter of the secondary-user's first-name
 *  @param [organization] The organization the secondary-user is completing hours for
 *  @param [totalTime]    The total amount of hours the secondary-user has completed so far
 *  @param [icon]         The secondary-user's profile icon
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
    var totalTime     : String    = "0:00",
    var icon          : Uri?      = null,
): User(context, firstName, lastName, theme) {

    //<editor-fold desc="Class Variables">

    /**
     * A List Of Event Ids Under The Secondary-User
     */
    var eventIds: List<String> = ArrayList()
        private set

    //</editor-fold>

    /**.
     * Configures Static Functions And Variables
     */
    companion object {

        // Gets The Different FireBase Instances//
        private val fireStore   = FirebaseFirestore.getInstance()
        private val fireStorage = FirebaseStorage.getInstance()
        private val storage = fireStorage.getReferenceFromUrl("gs://cs-tracker-5b4d1.appspot.com")

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
                val eventIds     = document.data?.get("eventIds")     as ArrayList<*>
                val goal         = document.data?.get("goal")         as Long
                val goalProgress = document.data?.get("goalProgress") as Long
                val grade        = document.data?.get("grade")        as Long
                val firstName    = document.data?.get("firstName")    as String
                val lastName     = document.data?.get("lastName")     as String
                val nameLetter   = document.data?.get("nameLetter")   as String
                val organization = document.data?.get("organization") as String
                val theme        = document.data?.get("theme")        as String
                val totalTime    = document.data?.get("totalTime")    as String

                // Gets The Icon Data//
                var icon: Uri? = null
                val iconId = document.data?.get("iconId") as String
                if (iconId != "") {
                    val iconToGet = storage.child("android/cs-tracker/$iconId")
                    icon = iconToGet.downloadUrl.await()
                }

                // Gets The Theme Enum From The Saved Theme String//
                val themeEnum = UserTheme.valueOf(theme)

                // Gets All Of The Saved Event Ids//
                val foundEventIds = ArrayList<String>()
                eventIds.forEach { eventId -> foundEventIds.add(eventId as String) }

                // Re-Creates The Primary-User Instance//
                val secondaryUser = SecondaryUser(context, firstName, lastName, themeEnum,
                    goal.toInt(),
                    goalProgress.toInt(),
                    grade.toInt(),
                    nameLetter,
                    organization,
                    totalTime,
                    icon
                )
                secondaryUser.id       = id
                secondaryUser.eventIds = foundEventIds

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
            userToSave["totalTime"]     = totalTime
            userToSave["eventIds"]      = eventIds

            // Sends The Secondary-User Data To The Database//
            val collection = fireStore.collection("SecondaryUsers").add(userToSave).await()
            val document = collection.get().await()

            // Gets The Newly Created Secondary-User Id//
            val id = document?.id ?: throw Error()

            // Updates The Primary-User With The Secondary-User Id//
            val primaryUser  = PrimaryUser.get(context)
            val isSuccessful = primaryUser.updateData(progressBar, id)
            if (!isSuccessful) return false

            // Hides The Progress Bar//
            progressBar.visibility = GONE

            // Logs That A Secondary-User Was Added//
            Log.v("Secondary_User", "Secondary user [$firstName $lastName] has been added to ${primaryUser.email}")
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