package dev.jzdevelopers.cstracker.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.jzdevelopers.cstracker.R
import kotlinx.android.synthetic.main.ui_secondary_user_add.*

class SecondaryUserAdd : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_secondary_user_add)

        textView.setOnClickListener {
            PrimaryUser.signOut(this)
        }
    }
}