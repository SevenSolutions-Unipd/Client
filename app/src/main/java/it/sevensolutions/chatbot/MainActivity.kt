package it.sevensolutions.chatbot

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit

class MainActivity : FragmentActivity() {
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
    }

    override fun onBackPressed() {
        val isAppClosable = if(supportFragmentManager.findFragmentByTag("ChatFragment") != null) {
            val chat = supportFragmentManager.findFragmentByTag("ChatFragment") as ChatFragment
            chat.onBackPressed()
        } else
            true

        if (supportFragmentManager.backStackEntryCount <= 1 && isAppClosable)
            finish()
    }
}