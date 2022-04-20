package it.sevensolutions.chatbot

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.ArrayMap
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import it.sevensolutions.chatbot.models.Message
import it.sevensolutions.chatbot.models.User
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class ChatFragment : Fragment(), MessageInput.InputListener {
    private val url = "https://imola-bot4me.herokuapp.com/api/chatterbot/"
    private lateinit var requestQueue: RequestQueue

    private val user = User("user", "user", "user")
    private val chatbot = User("chatbot", "chatbot", "bot_icon")

    private var imageLoader: ImageLoader = ImageLoader { imageView, url, _ ->
        val resID = resources.getIdentifier(url, "drawable", requireActivity().packageName)
        imageView.setImageResource(resID)
    }

    private var messagesAdapter: MessagesListAdapter<Message>? = null
    private lateinit var sessionID: String
    private lateinit var messagesList: MessagesList

    private val successListener =
        Response.Listener { response: JSONObject ->
            try {
                val reply = response.getString("text")
                val message =
                    Message(
                        "chatbot_output",
                        chatbot,
                        reply.trimStart('\n').trimEnd('\n'),
                        Date()
                    )

                Handler(Looper.getMainLooper()).postDelayed({
                    messagesAdapter!!.addToStart(message, true)
                }, 300)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

    private val errorListener =
        Response.ErrorListener {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.message_error),
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
                url,
                request,
                successListener,
                errorListener
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = mutableMapOf<String, String>()

                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val apiKey = sharedPreferences.getString("api_key", "")

                    headers["Authorization"] = apiKey!!

                    headers["Content-Type"] = "application/json"
                    headers["Cookie"] = "sessionid=$sessionID"
                    return headers
                }
            }

            requestQueue.add(objectRequest)

            val message = Message("user_input", user, input.toString(), Date())
            messagesAdapter!!.addToStart(message, true)
            return@InputListener true
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_actions_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(activity, SettingsActivity::class.java)
                startActivity(intent)
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

        requestQueue = Volley.newRequestQueue(requireContext())

        val objectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            successListener,
            errorListener
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = mutableMapOf<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                if (response!!.headers!!.containsKey("Set-Cookie")
                    && response.headers!!["Set-Cookie"]!!.startsWith("sessionid")
                ) {
                    val cookie = response.headers!!["Set-Cookie"]
                    if (cookie!!.isNotEmpty()) {
                        val splitCookie = cookie.split(";")
                        val splitSessionId = splitCookie[0].split("=")
                        sessionID = splitSessionId[1]
                    }
                }
                return super.parseNetworkResponse(response)
            }
        }

        objectRequest.retryPolicy = DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(objectRequest)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val apiKey = sharedPreferences.getString("api_key", "")

        if (apiKey.isNullOrEmpty()) {
            val transaction = parentFragmentManager
                .beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack("ApiKeyDialogFragment")

            ApiKeyDialogFragment.newInstance().show(transaction, "ApiKeyDialogFragment")
        }
    }

    private fun initAdapter() {
        messagesAdapter = MessagesListAdapter<Message>("user", MessageHolders(), imageLoader)
        messagesList.setAdapter(messagesAdapter!!)
    }

    override fun onSubmit(input: CharSequence?): Boolean {
        if (input.isNullOrEmpty())
            return false
        return true
    }
}