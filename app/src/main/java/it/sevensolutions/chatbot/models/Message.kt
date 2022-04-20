package it.sevensolutions.chatbot.models

import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.MessageContentType
import java.util.*

class Message(private val id: String,
              private val user: User,
              private val text: String,
              private val createdAt: Date) :
    IMessage, MessageContentType.Image, MessageContentType {

    private var image: Image? = null

    override fun getId(): String {
        return id
    }

    override fun getText(): String {
        return text
    }

    override fun getCreatedAt(): Date {
        return createdAt
    }

    override fun getUser(): User {
        return user
    }

    override fun getImageUrl(): String? {
        return if (image == null) null else image!!.url
    }

    class Image(val url: String)


}