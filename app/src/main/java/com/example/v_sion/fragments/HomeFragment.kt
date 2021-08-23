package com.example.v_sion.fragments

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.v_sion.R
import com.example.v_sion.models.ResultModel
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


// Parameters
private var hour_in_mil = (1000 * 60 * 60).toLong()
private var end_time = System.currentTimeMillis()
//private var start_time = end_time - hour_in_mil
private val start_time: Calendar = Calendar.getInstance()

private var strMsg : String = ""


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //get the today date at 0:00:00.000 (beginning of the day)
        start_time.add(Calendar.DATE, 0)
        start_time.set(Calendar.HOUR_OF_DAY, 0)
        start_time.set(Calendar.MINUTE, 0)
        start_time.set(Calendar.SECOND, 0)
        start_time.set(Calendar.MILLISECOND, 0)

        //do these if user has granted permission
        if (checkUsageStatsPermission(activity?.applicationContext)) {
            info("Permission Granted.")

            getUsageStatistics(start_time.timeInMillis, end_time)
/*
            totalTime = getUsageStats()
            showUsageStats()
            showTimeTracking()
            //load target time saved, if any.
            loadTargetTime()
            targetAchieved = compareTimeSpent(targetTimeCount.text.toString(),totalTimeCount.text.toString())
            scheduleGetUsageStats()
*/
        } else {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        view.usageDataTxt.text = strMsg

        // Inflate the layout for this fragment
        return view
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
    private fun getUsageStatistics(start_time: Long, end_time: Long) {
        var currentEvent: UsageEvents.Event
        //  List<UsageEvents.Event> allEvents = new ArrayList<>();
        val map: HashMap<String, ResultModel> = HashMap()
        val sameEvents: HashMap<String, MutableList<UsageEvents.Event>> = HashMap()
        // access to usage data
        val mUsageStatsManager =
            requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        if (mUsageStatsManager != null) {
            // query events data from starting time to end time
            val usageEvents = mUsageStatsManager.queryEvents(start_time, end_time)
            info("Usage Events: " + usageEvents)

            // Put these data into the map
            while (usageEvents.hasNextEvent()) {
                currentEvent = UsageEvents.Event()
                info("Current Usage Events: " + currentEvent)
                usageEvents.getNextEvent(currentEvent)
                if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED
                ) {
                    //  allEvents.add(currentEvent);
                    val key = currentEvent.packageName
                    if (map[key] == null) {
                        //ResultModel: (packagename), 0, 0
                        map[key] = ResultModel(key)
                        //map.put(key, ResultModel(key))
                        sameEvents[key] = ArrayList<UsageEvents.Event>()
                    }
                    sameEvents[key]!!.add(currentEvent)
                }
            }
            info("map: "+ map)
            info("sameEvents map: "+ sameEvents)


            // Traverse through each app data which is grouped together and count launch, calculate duration
            for ((_, value) in sameEvents.entries) {
                val totalEvents = value.size
                // if we have >1 events...
                if (totalEvents > 1) {
                    for (i in 0 until totalEvents - 1) {
                        val E0 = value[i]
                        val E1 = value[i + 1]
                        if (E1.eventType == 1 || E0.eventType == 1) {
                            //map[E1.packageName].launchCount += 1
                            var appLaunchCount : Long = map.get(E0.packageName)!!.launchCount
                            appLaunchCount += 1
                            map[E1.packageName]!!.launchCount = appLaunchCount
                            //map[E1.packageName].put(ResultModel(E1.packageName, ));
                        }
                        if (E0.eventType == 1 && E1.eventType == 2) {
                            val diff : Long = E1.timeStamp - E0.timeStamp
                            var appTime : Long = map.get(E0.packageName)!!.timeInForeground
                            appTime += diff
                            //map[E0.packageName]?.timeInForeground ?:  += diff
                            //map.put(E0.packageName,());
                            map[E0.packageName]!!.timeInForeground = appTime
                        }
                        //map[E1.packageName] = (ResultModel(E1.packageName, appTime, appLaunchCount));

                    }
                }

                // If First eventtype is ACTIVITY_PAUSED then added the difference of start_time and Event occuring time because the application is already running.
                if (value[0].eventType == 2) {
                    val diff = value[0].timeStamp - start_time
                    map[value[0].packageName]!!.timeInForeground += diff
                }

                // If Last eventtype is ACTIVITY_RESUMED then added the difference of end_time and Event occuring time because the application is still running .
                if (value[totalEvents - 1].eventType == 1) {
                    val diff = end_time - value[totalEvents - 1].timeStamp
                    map[value[totalEvents - 1].packageName]!!.timeInForeground += diff
                }
            }


            var smallInfoList: ArrayList<ResultModel> = ArrayList(map.values)

            // Concatenating data to show in a text view. You may do according to your requirement
            for (appUsageInfo in smallInfoList) {
                // Do according to your requirement
                strMsg += (appUsageInfo.packageName.toString() + " : " + convertTime(appUsageInfo.timeInForeground) + "\n\n")
            }
            info("strMsg: " + strMsg)
            //usageDataTxt.text = "aaa"
        } else {
            Toast.makeText(context, "Sorry...", Toast.LENGTH_SHORT).show()
        }
    }

    // Convert Time in ms (Long) to "1h 2m 3s" (String)
    private fun convertTime(count: Long): String{
        val hours = ((count / (1000*60*60)) % 24).toInt()
        val minutes = ((count / (1000*60)) % 60).toInt()
        val seconds = ((count/ 1000) % 60).toInt()
        return "" + hours + "h " + minutes + "m " + seconds + "s"
    }
}