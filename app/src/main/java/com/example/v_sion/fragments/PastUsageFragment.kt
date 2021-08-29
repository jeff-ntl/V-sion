package com.example.v_sion.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.v_sion.R
import com.example.v_sion.adapters.HistoryAdapter
import com.example.v_sion.adapters.HistoryListener
import com.example.v_sion.main.MainApp
import com.example.v_sion.models.HistoryModel
import com.example.v_sion.utils.SwipeToDeleteCallback
import kotlinx.android.synthetic.main.delete_dialog.view.*
import kotlinx.android.synthetic.main.fragment_past_usage.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PastUsageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PastUsageFragment : Fragment(), HistoryListener, AnkoLogger {

    lateinit var app: MainApp

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var histories = mutableListOf<HistoryModel>()

    //user id
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = requireActivity().application as MainApp

        //get current user id when MainActivity is created
        uid = app.auth.currentUser!!.uid

        getUsageHistory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_past_usage, container, false)
    }

    //for recyclerview...
    private fun showUsageHistory(){
        viewManager = LinearLayoutManager(activity)
        viewAdapter = HistoryAdapter(histories,this )

        recyclerView = history_recycler_view.apply {
            // use a linear layout manager
            layoutManager = viewManager
            // specify an viewAdapter
            adapter = viewAdapter
        }
        swipeToDelete()
    }

    //get usage history from firestore
    private fun getUsageHistory(){
        histories.clear()
        app.results.readUsage (uid){
            info(it.size.toString())
            histories = it.toMutableList()
            info("histories size is : " + histories.count().toString())
            info("histories is : " + histories)

            showUsageHistory()
        }
    }

    private fun swipeToDelete(){
        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //Inflate the dialog with custom view
                val mDialogView = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null)
                //AlertDialogBuilder
                val mBuilder = AlertDialog.Builder(context)
                    .setView(mDialogView)
                    .setTitle("Are You Sure You Want to Delete?")
                //show dialog
                val  mAlertDialog = mBuilder.show()

                //handle confirm button clicked
                mDialogView.deleteConfirmBtn.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()

                    val adapter = recyclerView.adapter as HistoryAdapter
                    adapter.removeAt(viewHolder.adapterPosition)
                    triggerDelete(viewHolder.itemView.tag as HistoryModel)
                    info("Successfully deleted.")
                }
                //handle cancel button clicked
                mDialogView.deleteCancelBtn.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()
                }
                //use notifyDataSetChanged() to relayout the views
                viewAdapter.notifyDataSetChanged()
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(recyclerView)
    }

    override fun triggerDelete(history: HistoryModel) {
        app.results.deleteUsage(uid,history.date)
        app.results.readUsage(uid) {
            info(it.size.toString())
            if(it.size == 0){
                TODO("navigate to another fragment telling the user no record is stored in database.")
                //Navigation.findNavController(view).navigate(R.id.action_fragment1_to_fragment2);
            }else{
                getUsageHistory()
            }
        }
    }
}