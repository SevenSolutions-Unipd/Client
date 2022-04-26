package it.sevensolutions.chatbot

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment

class ApiKeyDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_api_key_dialog, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.window?.setWindowAnimations(android.R.style.Animation_Dialog)

        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ConstraintLayout>(R.id.apiKeyDialogLayout).clipToOutline = true

        val settings = view.findViewById<ImageButton>(R.id.button_settings)
        settings.setOnClickListener {
            dismiss()

            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        val window  = dialog?.window
        val displayMetrics = resources.displayMetrics

        window?.setLayout(displayMetrics.widthPixels - 32*2, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
    }

    companion object {
        @JvmStatic fun newInstance() = ApiKeyDialogFragment()
    }
}