package it.sevensolutions.chatbot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                addToBackStack("ChatFragment")
                add<ChatFragment>(R.id.frag_container, "ChatFragment")
            }
        }

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.drawable.logo_imola)
        supportActionBar?.setDisplayUseLogoEnabled(true)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount <= 1)
            finish()
    }
}