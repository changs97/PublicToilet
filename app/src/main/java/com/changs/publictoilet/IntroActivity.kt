package com.changs.publictoilet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.changs.publictoilet.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {
    val binding by lazy{ActivityIntroBinding.inflate(layoutInflater)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        var handler = Handler()
        handler.postDelayed( {
            var intent = Intent( this, MapsActivity::class.java)
            startActivity(intent) },
            4000) }

    override fun onPause() {
        super.onPause()
        finish()
    }

}
