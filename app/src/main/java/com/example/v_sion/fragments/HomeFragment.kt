package com.example.v_sion.fragments

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
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
import com.example.v_sion.models.ResultModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


// Parameters
private var hour_in_mil = (1000 * 60 * 60).toLong()
private var end_time = System.currentTimeMillis()
//private var start_time = end_time - hour_in_mil
private val start_time: Calendar = Calendar.getInstance()

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



/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        menu.findItem(R.id.menu_search)?.setVisible(true)

        val searchItem = menu.findItem(R.id.menu_search)

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
/*
            //load target time saved, if any.
            loadTargetTime()
            targetAchieved = compareTimeSpent(targetTimeCount.text.toString(),totalTimeCount.text.toString())
            scheduleGetUsageStats()
*/
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
            itemsswipetorefresh.isRefreshing = false
            /*
            targetAchieved = compareTimeSpent(targetTimeCount.text.toString(),totalTimeCount.text.toString())
            itemsswipetorefresh.isRefreshing = false
            scheduleGetUsageStats()
            */

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

    //update current time and total time spent on phone.
    private fun showTimeTracking(){
        startTimeCount.text = convertTime2(start_time.timeInMillis)
        currentTimeCount.text = convertTime2(System.currentTimeMillis())
        totalTimeCount.text = totalTime
    }


}