package net.mrkaan.httpserverapp

import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mrkaan.httpserverapp.models.Password
import net.mrkaan.httpserverapp.utils.data.Constants
import net.mrkaan.httpserverapp.utils.db.DatabaseUtil
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private var mHttpServer: HttpServer? = null
    private var serverUp = false
    private lateinit var serverButton: Button
    private lateinit var serverTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serverTextView = findViewById(R.id.serverTextView)
        serverButton = findViewById(R.id.serverButton)

        DatabaseUtil.initDatabaseUtil(this)

        serverButton.setOnClickListener {
            serverUp = if (!serverUp) {
                startServer()
                true
            } else {
                stopServer()
                false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun streamToString(inputStream: InputStream): String {
        val s = Scanner(inputStream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

    private fun sendResponse(httpExchange: HttpExchange, responseText: String) {
        httpExchange.sendResponseHeaders(200, responseText.length.toLong())
        val os = httpExchange.responseBody
        os.write(responseText.toByteArray())
        os.close()
    }

    private fun startServer() {
        try {
            mHttpServer = HttpServer.create(InetSocketAddress(Constants.PORT), 0)
            mHttpServer!!.executor = Executors.newCachedThreadPool()

            mHttpServer!!.createContext("/", rootHandler)
            mHttpServer!!.createContext("/index", rootHandler)
            // Handle /messages endpoint
            mHttpServer!!.createContext("/save_password", passwordSaver)
            mHttpServer!!.createContext("/get_password", passwordGetter)
            mHttpServer!!.createContext("/delete", deleter)
            mHttpServer!!.createContext("/save_array", arraySaver)
            mHttpServer!!.start()//startServer server;
            serverTextView.text = getString(R.string.server_running)
            serverButton.text = getString(R.string.stop_server)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun stopServer() {
        if (mHttpServer != null) {
            mHttpServer!!.stop(0)
            serverTextView.text = getString(R.string.server_down)
            serverButton.text = getString(R.string.start_server)
        }
    }

    // Handler for root endpoint
    private val rootHandler = HttpHandler { exchange ->
        run {
            // Get request method
            when (exchange!!.requestMethod) {
                "GET" -> {
                    sendResponse(exchange, "<h1>Welcome </h1>to my server")
                }
            }
        }
    }

    private val passwordSaver = HttpHandler { httpExchange ->
        run {
            passwordSaverFun(httpExchange)
        }
    }
    private val passwordGetter = HttpHandler { httpExchange ->
        run {
            passwordGetterFun(httpExchange)
        }
    }

    private val deleter = HttpHandler { httpExchange ->
        run {
            deleterFun(httpExchange)
        }
    }

    private val arraySaver = HttpHandler { httpExchange ->
        run {
            arraySaverFun(httpExchange)
        }
    }

    private fun arraySaverFun(httpExchange: HttpExchange) {
        if (httpExchange.requestMethod.equals("POST")) {
            val inputStream = httpExchange.requestBody
            val requestBody = streamToString(inputStream)
            val jsonBody = JSONArray(requestBody)
            // save message to database
            for (i in 0 until jsonBody.length()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val password: Password =
                        Password.getPasswordFromJson(JSONObject(jsonBody[i].toString()))
                    val passwords = DatabaseUtil.getPasswordDatabase().passwordDao()
                        .getSelectedSite(password.url)
                    if (passwords.isEmpty()) {
                        DatabaseUtil.getPasswordDatabase().passwordDao().addNewSite(password)
                    } else {
                        var found = false
                        for (p in passwords) {
                            if (p.userName == password.userName) {
                                val pass =
                                    Password(
                                        p.uid,
                                        p.url,
                                        p.userName,
                                        password.password,
                                        Date().time,
                                        p.grouping,
                                        "${p.password}, ${p.oldPasswords}"
                                    )
                                DatabaseUtil.getPasswordDatabase().passwordDao().updateSite(pass)
                                found = true
                                break
                            }
                        }
                        if (!found) {
                            DatabaseUtil.getPasswordDatabase().passwordDao().addNewSite(password)
                        }
                    }
                }
            }
            sendResponse(httpExchange, "Successfully saved")
        } else {
            sendResponse(httpExchange, "This method is not supported")
        }
    }

    private fun deleterFun(httpExchange: HttpExchange) {
        if (httpExchange.requestMethod.equals("POST")) {
            val jsonBody = JSONObject(streamToString(httpExchange.requestBody))
            if (jsonBody.getString("method").equals("deleteall")) {
                CoroutineScope(Dispatchers.IO).launch {
                    DatabaseUtil.getPasswordDatabase().passwordDao().deleteAllSites()
                }

                sendResponse(httpExchange, "All websites deleted")
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val passwords = DatabaseUtil.getPasswordDatabase().passwordDao()
                        .getSelectedSite(jsonBody.getString("url"))
                    for (p in passwords) {
                        if (p.userName == jsonBody.getString("username")) {
                            DatabaseUtil.getPasswordDatabase().passwordDao().deleteSite(p)
                            break
                        }
                    }
                }
                sendResponse(
                    httpExchange,
                    "${jsonBody.getString("url")} ${jsonBody.getString("username")} deleted"
                )
            }
        } else {
            sendResponse(httpExchange, "This method is not supported")
        }
    }

    private fun passwordGetterFun(httpExchange: HttpExchange) {
        if (httpExchange.requestMethod.equals("POST")) {
            val jsonBody = JSONObject(streamToString(httpExchange.requestBody))
            if (jsonBody.getString("method").equals("getall")) {
                CoroutineScope(Dispatchers.IO).launch {
                    val passwords =
                        DatabaseUtil.getPasswordDatabase().passwordDao().getAllSites()
                    sendResponse(httpExchange, Password.toJsonArray(passwords).toString())
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val passwords = DatabaseUtil.getPasswordDatabase().passwordDao()
                        .getSelectedSite(jsonBody.getString("url"))
                    sendResponse(httpExchange, Password.toJsonArray(passwords).toString())
                }
            }
        } else {
            sendResponse(httpExchange, "This method is not supported")
        }
    }

    private fun passwordSaverFun(httpExchange: HttpExchange) {
        if (httpExchange.requestMethod.equals("POST")) {
            val inputStream = httpExchange.requestBody
            val requestBody = streamToString(inputStream)
            val jsonBody = JSONObject(requestBody)
            val password: Password = Password.getPasswordFromJson(jsonBody)
            CoroutineScope(Dispatchers.IO).launch {
                val passwords = DatabaseUtil.getPasswordDatabase().passwordDao()
                    .getSelectedSite(password.url)
                if (passwords.isEmpty()) {
                    DatabaseUtil.getPasswordDatabase().passwordDao().addNewSite(password)
                } else {
                    var found = false
                    for (p in passwords) {
                        if (p.userName == password.userName) {
                            val pass =
                                Password(
                                    p.uid,
                                    p.url,
                                    p.userName,
                                    password.password,
                                    Date().time,
                                    p.grouping,
                                    "${p.password}, ${p.oldPasswords}"
                                )
                            DatabaseUtil.getPasswordDatabase().passwordDao().updateSite(pass)
                            found = true
                            break
                        }
                    }
                    if (!found) {
                        DatabaseUtil.getPasswordDatabase().passwordDao().addNewSite(password)
                    }
                }
            }
            sendResponse(httpExchange, "Successfully saved")
        } else {
            sendResponse(httpExchange, "This method is not supported")
        }
    }


}