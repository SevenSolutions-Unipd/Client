package it.sevensolutions.chatbot.models

import com.stfalcon.chatkit.commons.models.IMessage
import java.util.*

class Message(private val id: String, private val user: User, private val text: String, private val createdAt: Date) :
    IMessage {

    override fun getId(): String = id
    override fun getText(): String = text
    override fun getCreatedAt(): Date = createdAt
    override fun getUser(): User = user
}