package dev.jzdevelopers.common

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.libs.JZRecyclerAdapter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}