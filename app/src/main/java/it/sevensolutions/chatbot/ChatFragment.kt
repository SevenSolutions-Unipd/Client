package it.sevensolutions.chatbot

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.ArrayMap
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import it.sevensolutions.chatbot.holders.IncomingVoiceMessageViewHolder
import it.sevensolutions.chatbot.holders.OutcomingVoiceMessageViewHolder
import it.sevensolutions.chatbot.models.Message
import it.sevensolutions.chatbot.models.User
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment(),
    MessagesListAdapter.SelectionListener,
    MessageInput.InputListener,
    MessageInput.AttachmentsListener,
    MessageHolders.ContentChecker<Message>,
    DialogInterface.OnClickListener {

    private val URL = "https://imola-bot4me.herokuapp.com/api/chatterbot/"
    private lateinit var  requestQueue: RequestQueue
    private val CONTENT_TYPE_VOICE: Byte = 1

    private var imageLoader: ImageLoader? = null
    private var messagesAdapter: MessagesListAdapter<Message>? = null
    private lateinit var sessionID: String

    private val successListener =
        Response.Listener { response: JSONObject ->
            try {
                val reply = response.getString("text")
                val message =
                    Message(
                        "prova",
                        User("chatbot", "chatbot", "chatbot", true),
                        reply.trimStart('\n').trimEnd('\n'),
                        Date()
                    )
                messagesAdapter!!.addToStart(message, true)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

    private val errorListener =
        Response.ErrorListener { error: VolleyError ->
            Toast.makeText(
                requireContext(),
                error.toString(),
                Toast.LENGTH_LONG
            ).show()
        }

    private val listener =
        MessageInput.InputListener { input ->
            val text: MutableMap<String?, String?> = ArrayMap()
            text["text"] = input.toString()
            val request = JSONObject(text as Map<*, *>)
            val objectRequest = object : JsonObjectRequest(
                Method.POST,
                URL,
                request,
                successListener,
                errorListener
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = mutableMapOf<String, String>()
                    headers["Authorization"] = "d7918028-8a60-4138-8319-a29b7d75c647"
                    headers["Content-Type"] = "application/json"
                    headers["Cookie"] = "sessionid=$sessionID"
                    return headers
                }
            }

            requestQueue.add(objectRequest)

            val message =
                Message("prova", User("user", "user", "user", true), input.toString(), Date())
            messagesAdapter!!.addToStart(message, true)
            return@InputListener true
        }

    private lateinit var messagesList: MessagesList
    private var selectionCount = 0
    private var lastLoadedDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageLoader =
            ImageLoader { imageView: ImageView?, url: String?, _: Any? ->
                Picasso.get().load(url).into(imageView)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_actions_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        onSelectionChanged(0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> messagesAdapter!!.deleteSelectedMessages()
            R.id.action_copy -> {
                messagesAdapter!!.copySelectedMessagesText(requireContext(), getMessageStringFormatter(), true)
                Toast.makeText(requireContext(), R.string.copied_message, Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        messagesList = view.findViewById(R.id.messagesList)
        initAdapter()

        val input: MessageInput = view.findViewById(R.id.input)
        input.setInputListener(listener)
        input.setAttachmentsListener(this)

        requestQueue =  Volley.newRequestQueue(requireContext())

        val objectRequest = object: JsonObjectRequest(
            Method.GET,
            URL,
            null,
            successListener,
            errorListener
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = mutableMapOf<String,String>()
                headers["Authorization"] = "d7918028-8a60-4138-8319-a29b7d75c647"
                headers["Content-Type"] = "application/json"
                return headers
            }

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                if (response!!.headers!!.containsKey("Set-Cookie")
                    && response!!.headers!!["Set-Cookie"]!!.startsWith("sessionid")) {
                    var cookie = response!!.headers!!["Set-Cookie"]
                    if (cookie!!.isNotEmpty()) {
                        val splitCookie = cookie.split(";")
                        val splitSessionId = splitCookie[0].split("=")
                        sessionID = splitSessionId[1]
                    }
                }

                return super.parseNetworkResponse(response)
            }
        }

        objectRequest.retryPolicy = DefaultRetryPolicy(10000,1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(objectRequest)
    }

    override fun onAddAttachments() {
        AlertDialog.Builder(requireContext())
            .setItems(resources.getStringArray(R.array.view_types_dialog), this)
            .show()
    }

    fun onBackPressed(): Boolean {
        return if (selectionCount > 0) {
            messagesAdapter!!.unselectAllItems()
            false
        } else
            true
    }

    override fun onSelectionChanged(count: Int) {
        selectionCount = count
//        menu!!.findItem(R.id.action_delete).isVisible = count > 0
//        menu!!.findItem(R.id.action_copy).isVisible = count > 0
    }

    private fun getMessageStringFormatter(): MessagesListAdapter.Formatter<Message>? {
        return MessagesListAdapter.Formatter { message: Message ->
            val createdAt =
                SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                    .format(message.createdAt)
            var text = message.text
            if (text == null) text = "[attachment]"
            java.lang.String.format(
                Locale.getDefault(), "%s: %s (%s)",
                message.user.name, text, createdAt
            )
        }
    }

    private fun initAdapter() {
        val holders = MessageHolders()
            .registerContentType(CONTENT_TYPE_VOICE,
                IncomingVoiceMessageViewHolder::class.java,
                R.layout.item_custom_incoming_voice_message,
                OutcomingVoiceMessageViewHolder::class.java,
                R.layout.item_custom_outcoming_voice_message,
                this
            )

        messagesAdapter = MessagesListAdapter<Message>("user", holders, null)
        messagesAdapter!!.enableSelectionMode(this)
        messagesAdapter!!.setLoadMoreListener(null)
        messagesList!!.setAdapter(messagesAdapter!!)
    }

    override fun onSubmit(input: CharSequence?): Boolean {
        return true
    }

    override fun hasContentFor(message: Message, type: Byte): Boolean {
        return if (type == CONTENT_TYPE_VOICE) {
            message.voice != null && message.voice!!.url != null && message.voice!!.url.isNotEmpty()
        } else false
    }

    override fun onClick(p0: DialogInterface?, p1: Int) {
//        when (p1) {
//            0 -> messagesAdapter!!.addToStart(MessagesFixtures.getImageMessage(), true)
//            1 -> messagesAdapter!!.addToStart(MessagesFixtures.getVoiceMessage(), true)
//        }
    }

//    companion object {
//        @JvmStatic
//        fun newInstance() = ChatFragment()
//    }
}