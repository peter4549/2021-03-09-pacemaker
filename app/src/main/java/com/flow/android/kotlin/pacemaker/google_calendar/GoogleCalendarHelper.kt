package com.flow.android.kotlin.pacemaker.google_calendar

import android.Manifest
import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.flow.android.kotlin.pacemaker.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.time.LocalDate
import java.util.*

class GoogleCalendarHelper private constructor(private val googleAccountCredential: GoogleAccountCredential) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val compatibleTransport = AndroidHttp.newCompatibleTransport()
    private val jsonFactory = JacksonFactory.getDefaultInstance()

    sealed class AvailableCode(val value: Int) {
        class Available : AvailableCode(1529)
        class GooglePlayServicesUnavailable : AvailableCode(1530)
        class NetworkUnavailable : AvailableCode(1531)
    }

    object RequestCode {
        const val ChooseAccount = 1057
        const val GetAccounts = 1058
        const val GooglePlayServices = 1059
        const val UserRecoverableAuth = 1060
    }

    private var calendar: Calendar? = null

    fun googleAccountCredential() = googleAccountCredential

    private fun getResultsFromApi(activity: Activity): String? {
        if (isGooglePlayServicesAvailable(activity).not()) { // Google Play Services를 사용할 수 없는 경우
            acquireGooglePlayServices(activity)
        } else if (isNetworkAvailable(activity).not()) {    // 인터넷을 사용할 수 없는 경우
            // showError todo.
        } else {

            // Google Calendar API 호출
            //MakeRequestTask(this, mCredential).execute()
        }

        return null
    }

    fun s(activity: Activity) {

        coroutineScope.launch {
            googleAccountCredential.allAccounts.map { it.name }.forEach { accountName ->
                googleAccountCredential.selectedAccountName = accountName
                calendar = Calendar.Builder(compatibleTransport, jsonFactory, googleAccountCredential)
                        .setApplicationName(activity.getString(R.string.app_name))
                        .build()

                getCalendarIds(activity).forEach { calendarId ->

                }
            }
        }
    }

    fun getEventsByDate(activity: Activity, localData: LocalDate) {
        googleAccountCredential.allAccounts

        var pageToken: String? = null

        do {
            var calendarList: CalendarList? = null

            try {
                calendarList = calendar?.calendarList()?.list()?.setPageToken(pageToken)?.execute()
            } catch (e: UserRecoverableAuthIOException) {
                activity.startActivityForResult(e.intent, RequestCode.UserRecoverableAuth)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val items = calendarList?.items

            for (calendarListEntry in items ?: listOf()) {
                id = calendarListEntry.id.toString()

            }
            pageToken = calendarList?.nextPageToken
        } while (pageToken != null)
    }

    fun getEventsByMonth(month: Int) {

    }

    private fun getCalendarIds(activity: Activity): List<String> {
        val list = mutableListOf<String>()
        var pageToken: String? = null

        do {
            var calendarList: CalendarList? = null

            try {
                calendarList = calendar?.calendarList()?.list()?.setPageToken(pageToken)?.execute()
            } catch (e: UserRecoverableAuthIOException) {
               activity.startActivityForResult(e.intent, RequestCode.UserRecoverableAuth)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val items = calendarList?.items

            for (calendarListEntry in items ?: listOf())
                list.add(calendarListEntry.id.toString())

            pageToken = calendarList?.nextPageToken
        } while (pageToken != null)

        return list
    }

    private fun getEventsByDate(calendarId: String, date: Date): List<Event> {
        val list = mutableListOf<Event>()

        val s = DateTime(date)

        calendar?.let { calendar ->
            val events = calendar.events()
                    .list(calendarId)
                    .setTimeMax()
                    .setTimeMin()
                    .execute()

            list.addAll(events.items)
        }

        return list
    }

    private fun isAvailable(activity: Activity): AvailableCode {
        if (isGooglePlayServicesAvailable(activity).not())
            return AvailableCode.GooglePlayServicesUnavailable()
        else if (isNetworkAvailable(activity).not())
            return AvailableCode.NetworkUnavailable()

        return AvailableCode.Available()
    }

    private fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        return googleApiAvailability.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    private fun acquireGooglePlayServices(activity: Activity) {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = googleApiAvailability.isGooglePlayServicesAvailable(activity)

        if (googleApiAvailability.isUserResolvableError(connectionStatusCode)) {
            googleApiAvailability.getErrorDialog(activity, connectionStatusCode, 1).show()
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activityNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activityNetwork) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }

    companion object {

        private var INSTANCE: GoogleCalendarHelper? = null

        fun getInstance(context: Context): GoogleCalendarHelper {
            return INSTANCE ?: run {
                val googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                        context.applicationContext,
                        listOf(CalendarScopes.CALENDAR)
                ).setBackOff(ExponentialBackOff())

                return GoogleCalendarHelper(googleAccountCredential)
            }
        }
    }
}

/* todo calendar provider 참고.
private val EVENT_PROJECTION: Array<String> = arrayOf(
            CalendarContract.Calendars._ID,                     // 0
            CalendarContract.Calendars.ACCOUNT_NAME,            // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
            CalendarContract.Calendars.OWNER_ACCOUNT            // 3
    )

    // The indices for the projection array above.
    private val PROJECTION_ID_INDEX: Int = 0
    private val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
    private val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
    private val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Run query
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val selection: String = "((${CalendarContract.Calendars.ACCOUNT_NAME} = ?) AND (" +
                "${CalendarContract.Calendars.ACCOUNT_TYPE} = ?) AND (" +
                "${CalendarContract.Calendars.OWNER_ACCOUNT} = ?))"
        val selectionArgs: Array<String> = arrayOf("hera@example.com", "com.example", "hera@example.com")
        //val cur = contentResolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null)
        val cur = contentResolver.query(uri, EVENT_PROJECTION, null, null, null)

        cur?.let {
            while (cur.moveToNext()) {
                // Get the field values
                val calID: Long = cur.getLong(PROJECTION_ID_INDEX)
                val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                val ownerName: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
                // Do something with the values...

                println("calId: $calID")
                println("dipName: $displayName")
                println("accName: $accountName")
                println("ownerName: $ownerName")
            }
        }

        findViewById<Button>(R.id.button).setOnClickListener {
            val calID: Long = 12
            val startMillis: Long = Calendar.getInstance().run {
                set(2021, 0, 14, 7, 30)
                timeInMillis
            }
            val endMillis: Long = Calendar.getInstance().run {
                set(2021, 0, 14, 8, 45)
                timeInMillis
            }

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, "Jazzercise hoyaya")
                put(CalendarContract.Events.DESCRIPTION, "Group workout")
                put(CalendarContract.Events.CALENDAR_ID, calID)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
             val eventID = uri?.lastPathSegment?.toLong()
            println("EIDDDDDd: $eventID")
            // 이게 널이 아니면 찾아서 삽입하기도 될듯.

        }
 */