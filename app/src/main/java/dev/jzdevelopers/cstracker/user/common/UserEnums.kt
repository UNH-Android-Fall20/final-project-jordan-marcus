package dev.jzdevelopers.cstracker.user.common

/** Kotlin Enum UserTheme,
 *  Enum for getting the theme for a specific secondary-user or primary-user
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

/** Kotlin Enum UserTheme,
 *  Enum for getting the ordering of the secondary-users
 */
enum class UserOrder {
    FIRST_NAME,
    GRADE,
    LAST_NAME,
    ORGANIZATION,
    TOTAL_TIME
}