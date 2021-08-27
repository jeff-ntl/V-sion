package com.example.v_sion.fragments

import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.v_sion.R
import com.example.v_sion.adapters.ResultAdapter
import com.example.v_sion.main.MainApp
import com.example.v_sion.models.HistoryModel
import com.example.v_sion.models.ResultModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.dialog_timer.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.timerTask
import com.example.v_sion.fragments.TimerFragment as TimerFragment1


// Parameters
private var hour_in_mil = (1000 * 60 * 60).toLong()
private var end_time = System.currentTimeMillis()
//private var start_time = end_time - hour_in_mil
private val start_time: Calendar = Calendar.getInstance()
private val updateCal: Calendar = Calendar.getInstance()

private var strMsg : String = ""

// for recyclerView
private lateinit var recyclerView: RecyclerView
private lateinit var viewAdapter: RecyclerView.Adapter<*>
private lateinit var viewManager: RecyclerView.LayoutManager

//for storing result obtained from userStatsManager
private var results = mutableListOf<ResultModel>()
//for search view
private var searchResults = mutableListOf<ResultModel>()

private var totalTime : String = ""

//for SharedPreferences
private val TARGET = "target"

//will be reassign with either 'true' or 'false' when 1)onCreate 2)refreshed 3)target time is changed (when compareTimeSpent is called)
private var targetAchieved = "N/A"

lateinit var app: MainApp
//user id
private lateinit var uid:String
private lateinit var user_email:String

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = requireActivity().application as MainApp

        //get current user id when MainActivity is created
        uid = app.auth.currentUser!!.uid
        user_email = app.auth.currentUser!!.email.toString()

        //for showing the search icon on menu
        setHasOptionsMenu(true)
        //get the today date at 0:00:00.000 (beginning of the day)
        start_time.add(Calendar.DATE, 0)
        start_time.set(Calendar.HOUR_OF_DAY, 0)
        start_time.set(Calendar.MINUTE, 0)
        start_time.set(Calendar.SECOND, 0)
        start_time.set(Calendar.MILLISECOND, 0)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Inflate the layout for this fragment
        return view
    }

    // for showing the search icon on menu
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        //menu.findItem(R.id.menu_search)?.setVisible(true)

        val searchItem = menu.findItem(R.id.menu_search)
        val timerItem = menu.findItem(R.id.item_timer)
        //these two menu items are only visible on home fragment
        searchItem.setVisible(true)
        timerItem.setVisible(true)

        //for searching
        if(searchItem != null){
            val searchView = searchItem.actionView as SearchView
            //set search hint here
            val searchTextHint = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchTextHint.hint = "Search for an app..."

            searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    //not using it at the moment
                    return true
                }

                //do these... when text in the search box is changing
                override fun onQueryTextChange(p0: String?): Boolean {

                    //do these... when there's input in the search box
                    if(p0!!.isNotEmpty()){
                        searchResults.clear()
                        val search = p0.toLowerCase()
                        results.forEach{
                            if(it.appName!!.toLowerCase().contains(search)){
                                searchResults.add(it)
                            }
                        }
                        recyclerView.adapter?.notifyDataSetChanged()
                    }else{
                        searchResults.clear()
                        searchResults.addAll(results)
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                    return true
                }
            })
        }
        //return true
        //return super.onCreateOptionsMenu(menu)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //do these if user has granted permission
        if (checkUsageStatsPermission(activity?.applicationContext)) {
            info("Permission Granted.")

            totalTime = getUsageStatistics(start_time.timeInMillis, end_time)
            showUsageStats()
            showTimeTracking()
            //load target time saved, if any.
            loadTargetTime()
            compareTimeSpent(targetTimeCount.text.toString(),totalTimeCount.text.toString())
            scheduleGetUsageStats()
        } else {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        //set the colors of the Pull To Refresh View
        itemsswipetorefresh.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(),R.color.cyan))
        itemsswipetorefresh.setColorSchemeColors(Color.WHITE)
        //do these... when user swipes down
        itemsswipetorefresh.setOnRefreshListener{
            totalTime = getUsageStatistics(start_time.timeInMillis, end_time)
            showUsageStats()
            showTimeTracking()
            compareTimeSpent(targetTimeCount.text.toString(),totalTimeCount.text.toString())
            itemsswipetorefresh.isRefreshing = false
            scheduleGetUsageStats()
        }
    }

    //for recyclerview...
    private fun showUsageStats(){
        viewManager = LinearLayoutManager(activity)
        viewAdapter = ResultAdapter(searchResults)

        recyclerView = my_recycler_view.apply {

            // use a linear layout manager
            layoutManager = viewManager
            // specify an viewAdapter
            adapter = viewAdapter
        }

    }

    // to check if user has granted permission to the app.
    private fun checkUsageStatsPermission(context: Context?): Boolean {
        val appOpsManager: AppOpsManager?
        appOpsManager = context?.getSystemService(Context.APP_OPS_SERVICE)!! as AppOpsManager
        //use unsafeCheckOpNoThrow for API 29....?????????!!!
        val mode: Int = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        //val mode: Int = appOpsManager.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun scheduleGetUsageStats(){
        /*
        * timer work as expected, set to 23,59,0 (11:59:00 PM) to allow 1min for the app to run the method,
        * NOT SETTING to 23,59,59,999 (11:59:00.999 PM) because when the app calls the method, it might has already been the next day and you will lose all usage data
        * 23,29,59 makes it not tracking 24hrs usage but provide durability to the app
        * interval is set to 24hrs, if the user opened the app on ytd, but never open tdy, u still get to call the method on same time.
        * bug found: only work in foreground & background but not after app being killed. User can still open the app after 23,59,0 and the usage data will be uploaded to firestore
        * */
        var totalTime: String
        var date : String

        //the time when usage data be uploaded to firestore(LOCAL TIME)
        updateCal.add(Calendar.DATE, 0)
        updateCal.set(Calendar.HOUR_OF_DAY, 23)
        updateCal.set(Calendar.MINUTE, 59)
        updateCal.set(Calendar.SECOND, 0)
        updateCal.set(Calendar.MILLISECOND, 0)

        val timer = Timer()
        timer.scheduleAtFixedRate(timerTask {
            date = convertTime3(updateCal.timeInMillis)
            totalTime = getUsageStatistics(start_time.timeInMillis, end_time)
            app.results.addUsage(HistoryModel(uid,user_email,date,totalTime,targetAchieved))
        },updateCal.time,24*60*60*60)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun getUsageStatistics(start_time: Long, end_time: Long): String {
        var currentEvent: UsageEvents.Event
        //  List<UsageEvents.Event> allEvents = new ArrayList<>();
        val map: HashMap<String, ResultModel> = HashMap()
        val sameEvents: HashMap<String, MutableList<UsageEvents.Event>> = HashMap()

        // access to usage data
        val mUsageStatsManager = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // store total screen time
        var totalTime : Long = 0

        results.clear()
        searchResults.clear()

        if (mUsageStatsManager != null) {
            // query events data from starting time to end time
            val usageEvents = mUsageStatsManager.queryEvents(start_time, System.currentTimeMillis())
            info("Usage Events: " + usageEvents)

            // Put these data into the map
            // Iterate through each events
            while (usageEvents.hasNextEvent()) {
                currentEvent = UsageEvents.Event()
                info("Current Usage Events: " + currentEvent)
                usageEvents.getNextEvent(currentEvent)
                if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED
                ) {
                    //  allEvents.add(currentEvent);
                    val key = currentEvent.packageName
                    // these code will be executed if the app (key is the package name) is not found in map
                    if (map[key] == null) {
                        //ResultModel: (packagename), 0, 0
                        // add the app into map
                        map[key] = ResultModel(key)
                        // an arrayList is added as a value of the app into sameEvents
                        sameEvents[key] = ArrayList<UsageEvents.Event>()
                    }
                    // if we already have an entry of the app in map, add the event into the arraylist in sameEvents
                    sameEvents[key]!!.add(currentEvent)
                }
            }
            info("map: "+ map)
            info("sameEvents map: "+ sameEvents)

            // Traverse through each app data which is grouped together and count launch, calculate duration
            for ((_, value) in sameEvents.entries) {
                val totalEvents = value.size
                // if the app has >1 events...
                // eventType = 1: RESUMED (moved to foreground); 2: PAUSED (moved to background)
                if (totalEvents > 1) {
                    for (i in 0 until totalEvents - 1) {
                        val E0 = value[i]
                        val E1 = value[i + 1]
                        // calculate launch count whenever the ACTIVITY is RESUMED (moved to foreground)
                        // E0 is very likely to be 1, so we're looking at E1 only here to calculate launch count. We're neglecting the very first event as the default value of launchCount =1
                        if (E1.eventType == 1 ) {
                            var appLaunchCount : Long = map.get(E0.packageName)!!.launchCount
                            appLaunchCount += 1
                            map[E1.packageName]!!.launchCount = appLaunchCount
                        }
                        // calculate screen time => E0 : the app is opened (moved to foreground); E1: moved to background
                        if (E0.eventType == 1 && E1.eventType == 2) {
                            val diff : Long = E1.timeStamp - E0.timeStamp
                            var appTime : Long = map.get(E0.packageName)!!.timeInForeground
                            appTime += diff
                            map[E0.packageName]!!.timeInForeground = appTime
                            totalTime += diff
                        }
                    }
                }

                // If First eventtype is ACTIVITY_PAUSED then added the difference of start_time and Event occuring time because the application is already running.
                if (value[0].eventType == 2) {
                    val diff = value[0].timeStamp - start_time
                    map[value[0].packageName]!!.timeInForeground += diff
                    totalTime += diff
                }
                /*
                TODO("seems like not working" )
                // If Last eventtype is ACTIVITY_RESUMED then added the difference of end_time and Event occuring time because the application is still running.
                // This is a rare case designed specifically for the V-sion app only.
                if (value[totalEvents - 1].eventType == 1) {
                    val diff = end_time - value[totalEvents - 1].timeStamp
                    map[value[totalEvents - 1].packageName]!!.timeInForeground += diff
                }
                 */
            }

            var smallInfoList: ArrayList<ResultModel> = ArrayList(map.values)

            // Concatenating data to show in a text view. You may do according to your requirement
            for (appUsageInfo in smallInfoList) {
                //strMsg += (convertToAppName(appUsageInfo.packageName.toString()) + " : " + convertTime(appUsageInfo.timeInForeground) + " : " + appUsageInfo.launchCount + "\n\n")
                results.add(ResultModel(appUsageInfo.packageName.toString(), getAppIcon(appUsageInfo.packageName.toString()), convertToAppName(
                    appUsageInfo.packageName.toString()), appUsageInfo.timeInForeground, convertTime(appUsageInfo.timeInForeground), appUsageInfo.launchCount))
            }
            //info("strMsg: " + strMsg)
            info("strMsg: " + results)
            results.sortByDescending { it.timeInForeground }
            searchResults.addAll(results)
        } else {
            Toast.makeText(context, "Sorry...", Toast.LENGTH_SHORT).show()
        }

        return convertTime(totalTime)
    }

    //only called when there's a change in target time (hit confirm button in the target dialog)
    fun saveTargetTime(){
        //MODE_PRIVATE: no other app can change our shared preferences
        val sharedPref: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(TARGET,targetTimeCount.text.toString())

        editor.apply()
        info("sharedprefs: " + sharedPref.getString(TARGET,"2h 0m"))
    }

    fun loadTargetTime(){
        val sharedPref: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        //default target time: 2h 0m
        if(targetTimeCount!=null){
            targetTimeCount.text = sharedPref.getString(TARGET,"2h 0m")
        }
    }

    //convert packagename(eg: ie.wit.tracko for this app) obtained from UsageStatsManager to app name (eg: Tracko)
    private fun convertToAppName(packageName: String):String{
        //val packageManager: PackageManager = applicationContext.packageManager
        val packageManager: PackageManager = requireContext().packageManager

        var applicationInfo: ApplicationInfo?
        lateinit var applicationName: String

        try{
            applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        }catch(e: PackageManager.NameNotFoundException){
            applicationInfo = null
        }
        if(applicationInfo == null){
            applicationName = "Unknown"
        }else{
            applicationName = packageManager.getApplicationLabel(applicationInfo).toString()
        }
        return applicationName
    }

    //get app icon by giving known package name.
    private fun getAppIcon(packageName: String): Bitmap {
        val appIconBitMap: Bitmap?
        //info("icon is: " + packageManager.getApplicationIcon(packageName))
        //appIconBitMap = packageManager.getApplicationIcon(packageName).toBitmap()
        //info("icon is: " + requireContext().packageManager.getApplicationIcon(packageName))
        appIconBitMap = requireContext().packageManager.getApplicationIcon(packageName).toBitmap()
        return appIconBitMap
    }

    // Convert Time in ms (Long) to "1h 2m 3s" (String)
    private fun convertTime(count: Long): String{
        val hours = ((count / (1000*60*60)) % 24).toInt()
        val minutes = ((count / (1000*60)) % 60).toInt()
        val seconds = ((count/ 1000) % 60).toInt()
        return "" + hours + "h " + minutes + "m " + seconds + "s"
    }

    private fun convertTime2(lastTimeUsed: Long):String{
        val date = Date(lastTimeUsed)
        val format = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        return format.format(date)
    }

    //for displaying date in past usage fragment
    private fun convertTime3(lastTimeUsed: Long):String{
        Date(lastTimeUsed)
        val format = SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH)
        return format.format(lastTimeUsed)
    }

    //update current time and total time spent on phone.
    private fun showTimeTracking(){
        startTimeCount.text = convertTime2(start_time.timeInMillis)
        currentTimeCount.text = convertTime2(System.currentTimeMillis())
        totalTimeCount.text = totalTime
    }

    fun compareTimeSpent(targetTime:String, timeSpent:String){
        //the regex to find any character A-Z in the string
        val re = Regex("[a-z]")

        //convert "?h ?m ?s" into ["?", "?", "?"] and "?h ?m" into ["?", "?"]
        val extractedTargetTime = re.replace(targetTime, "")
        val foundInTargetTime = extractedTargetTime.split(" ").toTypedArray()

        val extractedTimeSpent = re.replace(timeSpent, "")
        val foundInTimeSpent = extractedTimeSpent.split(" ").toTypedArray()

        //return the results obtained from comparing target time with actual time spent (true: if target achieved)
        val result = convertTimeToMs(foundInTargetTime) > convertTimeToMs(foundInTimeSpent)
        info("result of comparing: $result")

        targetAchieved = result.toString()
    }

    //calculation: convert the time into ms (to ease the compare process)
    private fun convertTimeToMs(timeInString:Array<String>):Int{
        var timeInMs = 0

        for((index,value) in timeInString.withIndex()){
            timeInMs += value.toInt() * 1000 * Math.pow(60.toDouble(),(2-index).toDouble()).toInt()
        }
        return timeInMs
    }


}