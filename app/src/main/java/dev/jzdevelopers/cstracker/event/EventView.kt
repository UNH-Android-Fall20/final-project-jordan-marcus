package dev.jzdevelopers.cstracker.event

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.jzdevelopers.cstracker.R

class EventView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_event_view)
    }
}