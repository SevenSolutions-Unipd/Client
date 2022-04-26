package it.sevensolutions.chatbot.models

import com.stfalcon.chatkit.commons.models.IUser

class User(private val id: String, private val name: String, private val avatar: String) : IUser {

    override fun getId(): String = id
    override fun getName(): String = name
    override fun getAvatar(): String = avatar
}