package dev.jzdevelopers.cstracker.common

import android.content.Context
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.InputStream
import java.io.Serializable

@GlideModule
class FireBaseGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(
            StorageReference::class.java, InputStream::class.java,
            FirebaseImageLoader.Factory()
        )
    }
}

/** Kotlin Abstract Class FireBaseModel,
 *  Class That Handles Common FireBase Functions/Properties
 *  @author Jordan Zimmitti, Marcus Novoa
 */
abstract class FireBaseModel: Serializable {

    /**.
     * Configures Static Functions And Variables
     */
    companion object {

        // Gets The Different FireBase Instances//
        val firebaseAuth        = FirebaseAuth.getInstance()
        val fireStore           = FirebaseFirestore.getInstance()
        private val fireStorage = FirebaseStorage.getInstance()

        // Get Storage Reference From FireBase//
        val appStorage = fireStorage.getReferenceFromUrl("gs://cs-tracker-5b4d1.appspot.com")
    }

    /**.
     * Abstract Function For Adding A Model To The Database
     * @param [loadingBar] Circular progress bar to alert the user when the addition is in progress
     * @return Whether the model was added successfully
     */
    abstract suspend fun add(loadingBar: ProgressBar): Boolean

    /**.
     * Abstract Function For Deleting A Model In The Database
     * @param [id]         The id of the model
     * @param [loadingBar] Circular progress bar to alert the user when the deletion is in progress
     * @return Whether the model was deleted successfully
     */
    protected abstract suspend fun delete(id: String): Boolean

    /**.
     * Abstract Function For Editing A Model In The Database
     * @param [id]         The id of the model
     * @param [loadingBar] Circular progress bar to alert the user when the edit is in progress
     * @return Whether the model was edited successfully
     */
    protected abstract suspend fun edit(id: String, loadingBar: ProgressBar): Boolean
}