package com.rogg.webserver_kt

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.collections.ArrayList


class ViewholderDevice(private var activity: MainActivity,private val dataSet: ArrayList<Device>)  :
    RecyclerView.Adapter<ViewholderDevice.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView
        val desc: TextView
        val add: ImageView
        val container: RelativeLayout
        var added=false

        init {
            // Define click listener for the ViewHolder's View
            name = view.findViewById(R.id.name)
            desc = view.findViewById(R.id.desc)
            add = view.findViewById(R.id.add)
            container = view.findViewById(R.id.container)
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_device, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.name.text = dataSet[position].name
        viewHolder.desc.text = "id: "+dataSet[position].id+"   ip:"+dataSet[position].ip
        viewHolder.add.setOnClickListener {
            viewHolder.added=!viewHolder.added
            if(viewHolder.added){
                activity.devices.add(dataSet[position])
                viewHolder.container.setBackgroundColor(Color.YELLOW)
                viewHolder.add.setImageResource(R.drawable.baseline_remove_24)
            }
            else{
                activity.devices.removeAt(getPosition(dataSet[position]))
                viewHolder.container.setBackgroundColor(Color.TRANSPARENT)
                viewHolder.add.setImageResource(R.drawable.baseline_add_24)
            }
            Log.i("Client Http","devices : "+activity.devices.size)
        }
    }
    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun getPosition(device: Device):Int{
        for(i in dataSet.indices){
            if(dataSet[i].id==device.id)
                return i
        }
        return -1
    }

}
