package dev.jzdevelopers.cstracker.user.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.common.UserSort
import dev.jzdevelopers.cstracker.user.common.UserSort.*
import dev.jzdevelopers.cstracker.user.common.UserTheme
import dev.jzdevelopers.cstracker.user.common.UserTheme.GREEN
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.UUID.randomUUID

/** Kotlin Class SecondaryUser,
 *  Class That Handles The Secondary-User Objects
 *  @author Jordan Zimmitti, Marcus Novoa
 *  @param [context]       Gets the instance from the caller activity
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
 */
class SecondaryUser(
    context           : Context?  = null,
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
): User(context, firstName, lastName, theme) {

    /**
     * The Id Of The Icon
     */
    var profileImageId = ""
        private set

    /**.
     * Configures Static Functions And Variables
     */
    companion object {

        /**.
         * Function That Creates The Query For Getting All Of The Secondary-Users Under A Primary-User
         * @param [primaryUserId] The id of the primary-user
         * @param [sort]          How the secondary-users will be sorted
         * @return The query for getting all of the secondary-users under a primary-user
         */
        fun getAll(primaryUserId: String, sort: UserSort): Query {

            // Returns The Query//
            return when(sort) {
                FIRST_NAME -> {
                    fireStore
                        .collection("SecondaryUsers")
                        .whereIn("primaryUserId", mutableListOf(primaryUserId))
                        .orderBy("firstName")
                        .orderBy("lastName")
                        .orderBy("grade")
                        .orderBy("organization")
                }
                GRADE -> {
                    fireStore
                        .collection("SecondaryUsers")
                        .whereIn("primaryUserId", mutableListOf(primaryUserId))
                        .orderBy("grade")
                        .orderBy("firstName")
                        .orderBy("lastName")
                        .orderBy("organization")
                }
                LAST_NAME -> {
                    fireStore
                        .collection("SecondaryUsers")
                        .whereIn("primaryUserId", mutableListOf(primaryUserId))
                        .orderBy("lastName")
                        .orderBy("firstName")
                        .orderBy("grade")
                        .orderBy("organization")
                }
                ORGANIZATION -> {
                    fireStore
                        .collection("SecondaryUsers")
                        .whereIn("primaryUserId", mutableListOf(primaryUserId))
                        .orderBy("organization")
                        .orderBy("firstName")
                        .orderBy("lastName")
                        .orderBy("grade")
                }
            }
        }
    }

    /**.
     * Function That Deletes A Secondary-User In The Database
     * @param [id]     The id of the secondary-user
     * @return Whether the secondary-user was deleted successfully
     */
    public override suspend fun delete(id: String): Boolean {
        return try {

            // Deletes The Profile-Image From FireBase Storage If It Exists//
            if (profileImageId != "") {
                appStorage.child("android/cs-tracker/$profileImageId").delete()
            }

            // Deletes The Primary-User And Its Data From The Database//
            val document = fireStore.collection("SecondaryUsers").document(id)
            document.delete().await()

            // Logs That The Primary-User Was Deleted Successfully//
            Log.v("Secondary_User", "Secondary user [$firstName $lastName] has been deleted")
            true
        }
        catch (_: Exception) {
            showGeneralError()
            false
        }
    }

    /**.
     * Function That Gets The Profile-Image Reference
     * @return The profile-image reference
     */
    fun profileImageReference(): StorageReference? {

        // When The Icon Id Is Empty//
        if (profileImageId == "") return null

        // Returns The Icon Reference//
        return appStorage.child("android/cs-tracker/$profileImageId")
    }

    /**.
     * Function That Adds A Secondary-User To The Database
     * @param [loadingBar]   Circular progress bar to alert the user when the addition is in progress
     * @param [profileImage] The secondary-user's profile icon
     * @return Whether the secondary-user was added successfully
     */
    suspend fun add(loadingBar: ProgressBar, profileImage: Drawable?): Boolean {
        try {

            // When Context Is Null//
            if (context == null) {

                // Throws A Runtime Error//
                throw NullPointerException("Context must not be null")
            }

            // Checks If The User Input Is Valid//
            if (!super.add(loadingBar)) return false
            if (!isValidGoal()) return false
            if (!isValidGrade()) return false
            if (!isValidOrganization()) return false

            // Shows The Loading Bar//
            loadingBar.visibility = View.VISIBLE

            // When A Profile-Image Needs To Be Saved//
            if (profileImage != null) {

                // Prepares The Image To Be Saved//
                val generatedImageId = randomUUID().toString()
                val imageToSave = appStorage.child("android/cs-tracker/$generatedImageId")

                // Compresses The Image To A Smaller Size//
                val compressedImage = compressImage(profileImage)

                // Saves The Image//
                imageToSave.putBytes(compressedImage).await()
                profileImageId = generatedImageId
            }

            // Adds The Secondary-User Data To The Database//
            fireStore.collection("SecondaryUsers").add(this).await()

            // Hides The Loading Bar//
            loadingBar.visibility = View.GONE

            // Logs That The Secondary-User Was Added Successfully//
            Log.v("Secondary_User", "Secondary user [$firstName $lastName] has been added to primary user id: $primaryUserId")
            return true
        }
        catch (_: Exception) {
            loadingBar.visibility = View.GONE
            showGeneralError()
            return false
        }
    }

    /**.
     * Function That Edits A Secondary-User In The Database
     * @param [id]         The id of the secondary-user
     * @param [loadingBar] Circular progress bar to alert the user when the edit is in progress
     * @return Whether the secondary-user was edited successfully
     */
    suspend fun edit(id: String, loadingBar: ProgressBar, profileImage: Drawable?): Boolean {
        try {

            // Checks If The User Input Is Valid//
            if (!super.edit(id, loadingBar)) return false

            // Shows The Loading Bar//
            loadingBar.visibility = View.VISIBLE

            // Deletes The Profile-Image From FireBase Storage If It Exists//
            if (profileImageId != "") {
                appStorage.child("android/cs-tracker/$profileImageId").delete()
                profileImageId = ""
            }

            // When A Profile-Image Needs To Be Saved//
            if (profileImage != null) {

                // Prepares The Image To Be Saved//
                val generatedImageId = randomUUID().toString()
                val imageToSave = appStorage.child("android/cs-tracker/$generatedImageId")

                // Compresses The Image To A Smaller Size//
                val compressedImage = compressImage(profileImage)

                // Saves The Image//
                imageToSave.putBytes(compressedImage).await()
                profileImageId = generatedImageId
            }

            // Sends The Edited User Data To The Database//
            val document = fireStore.collection("SecondaryUsers").document(id)
            document.set(this)

            // Hides The Loading Bar//
            loadingBar.visibility = View.GONE

            // Logs That The Secondary-User Was Edited Successfully//
            Log.v("Secondary_User", "Secondary user [$firstName $lastName] has been edited")
            return true
        }
        catch (_: Exception) {
            loadingBar.visibility = View.GONE
            showGeneralError()
            return false
        }
    }

    /**.
     * Function That Takes An Image And Compresses It For Storage
     * @param [image] The image to compress
     * @return The compressed image in bytes
     */
    private fun compressImage(image: Drawable): ByteArray {

        // Converts The Drawable To A Bitmap//
        val bitmap = (image as BitmapDrawable).bitmap

        // Gets The New, Smaller Dimensions For The Bitmap//
        val bitmapDimensions = (bitmap.height * (500.0 / bitmap.width)).toInt()

        // Scales The Bitmap Down To Save Space And Allow For Smoother Scrolling//
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 500, bitmapDimensions, true)

        // Compresses The Bitmap//
        val stream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 10, stream)

        // Returns The Compressed Image In Bytes//
        return stream.toByteArray()
    }

    /**.
     * Function That Checks The Goal For Validity
     * @return Whether the goal is valid
     */
    private fun isValidGoal(): Boolean {

        // Gets The Context If It Exists//
        val context = context ?: throw NullPointerException("Context must not be null")

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

        // Gets The Context If It Exists//
        val context = context ?: throw NullPointerException("Context must not be null")

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

        // Gets The Context If It Exists//
        val context = context ?: throw NullPointerException("Context must not be null")

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
                    .toLowerCase(Locale.getDefault())
                    .split(" ")
                    .joinToString(" ") { it.capitalize(Locale.getDefault()) }
                true
            }
        }
    }
}