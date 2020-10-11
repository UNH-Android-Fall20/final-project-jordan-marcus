package dev.jzdevelopers.cstracker.user

import android.graphics.Bitmap

data class SecondaryUser(
    var icon          : Bitmap? = null,
    var goal          : Int     = 0,
    var goalProgress  : Int     = 0,
    var grade         : Int     = 0,
    var primaryUserId : Int     = 0,
    var themeId       : Int     = 0,
    var organization  : String  = ""
): User() {



//    override fun save(context: Context) {
//        super.save(context)
//
//        // Takes The User Data And Prepares It For The Database//
//        userToSave["goal"]         = goal
//        userToSave["goalProgress"] = goalProgress
//        userToSave["grade"]        = grade
//        userToSave["themeId"]      = themeId
//        userToSave["organization"] = organization
//    }
}