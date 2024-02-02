package dev.su.kotlinvideotoimage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button


class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        // Find the button by its ID
        val btnNavigate = findViewById<Button>(R.id.button1)

        // Set an OnClickListener for the button
        btnNavigate.setOnClickListener {
            // When the button is clicked, navigate to AnotherActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}