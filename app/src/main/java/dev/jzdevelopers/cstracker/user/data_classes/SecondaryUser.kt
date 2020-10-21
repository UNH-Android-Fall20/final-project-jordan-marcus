package dev.jzdevelopers.cstracker.user.data_classes

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import kotlinx.coroutines.tasks.await
import java.util.Locale.getDefault
import java.util.UUID.randomUUID

/** Data Class SecondaryUser
 *  Data class that handles the secondary user objects
 *  @author Jordan Zimmitti, Marcus Novoa
 */
data class SecondaryUser(
    var goal          : Int               = 0,
    var goalProgress  : Int               = 0,
    var grade         : Int               = 0,
    var nameLetter    : String            = "",
    var organization  : String            = "",
    val icon          : Uri?              = null,
    val eventIds      : ArrayList<String> = ArrayList(),
): User() {

    /**.
     * Function That Signs Up A Secondary User To The Database
     * @param [context]     Gets the instance from the caller activity
     * @param [progressBar] Circular progress bar to alert the user when the sign-up is in progress
     */
    suspend fun signUp(context: Context, progressBar: ProgressBar) {
        try {

            // Checks If The User Input Is Valid//
            if (!super.signUp(context))        return
            if (!isValidGoal(context))         return
            if (!isValidGrade(context))        return
            if (!isValidOrganization(context)) return

            // Shows The Progress Bar//
            progressBar.visibility = View.VISIBLE

            // Saves The Icon To FireBase Storage If It Exists//
            var iconId = ""
            val generatedIconId = randomUUID().toString()
            val iconToSave = storage.child("android/cs-tracker/$generatedIconId")
            if (icon != null) {
                iconToSave.putFile(icon).await()
                iconId = generatedIconId
            }

            // Takes The User Data And Prepares It For The Database//
            userToSave["goal"]          = goal
            userToSave["goalProgress"]  = goalProgress
            userToSave["grade"]         = grade
            userToSave["iconId"]        = iconId
            userToSave["nameLetter"]    = nameLetter
            userToSave["organization"]  = organization

            // Sends The User Data To The Database//
            val collection = fireStore.collection("SecondaryUsers").add(userToSave).await()
            val document = collection.get().await()

            // Gets The Newly Created Secondary User Id//
            val id = document?.id ?: throw Error()

            // Adds The Secondary User Id To The Primary User//
            val primaryUser = PrimaryUser().get(context)
            primaryUser.secondaryUserIds.add(id)

            // Updates The Primary User//
            primaryUser.updateData(context, progressBar)

            // Hides The Progress Bar//
            progressBar.visibility = View.GONE

            // Logs That A Secondary User Was Added//
            Log.v("Secondary_User", "Secondary user [$firstName $lastName] has been added to ${primaryUser.email}")
        }
        catch (_: Exception) {
            progressBar.visibility = View.GONE
            showGeneralError(context)
        }
    }

    /**.
     * Function That Checks The Goal For Validity
     * @param [context] Gets the instance from the caller activity
     * @return whether the goal is valid
     */
    private fun isValidGoal(context: Context): Boolean {

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
     * @param [context] Gets the instance from the caller activity
     * @return whether the grade is valid
     */
    private fun isValidGrade(context: Context): Boolean {

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
     * @param [context] Gets the instance from the caller activity
     * @return whether the organization is valid
     */
    private fun isValidOrganization(context: Context): Boolean {

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