package dev.jzdevelopers.cstracker.user

/** Kotlin Enum MultiUser
 *  Enum for getting the state of whether the user is keeping track of other people's hours
 */
enum class MultiUser {
    SIGNED_OUT,
    YES,
    NO
}

/** Kotlin Enum UserTheme
 *  Enum for getting the theme for a specific user profile or primary user
 */
enum class UserTheme {
    DEFAULT,
    RED,
    ORANGE,
    YELLOW,
    GREEN,
    BLUE,
    INDIGO,
    VIOLET,
    PINK,
    TEAL,
    BROWN,
    BLACK
}