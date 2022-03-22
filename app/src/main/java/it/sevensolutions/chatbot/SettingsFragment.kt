package it.sevensolutions.chatbot

import android.os.Bundle
import android.text.InputType

import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val apiKeyPreference: EditTextPreference? = findPreference("api_key")
        apiKeyPreference?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_TEXT
        }
    }
}