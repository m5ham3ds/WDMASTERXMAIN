package com.wdmaster.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.wdmaster.app.R
import com.wdmaster.app.data.model.Router

class RouterSpinnerAdapter(
    private val context: android.content.Context,
    private val routers: List<Router>
) : ArrayAdapter<Router>(context, R.layout.item_spinner_router, routers) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_spinner_router, parent, false)

        val router = routers[position]
        
        val tvName = view.findViewById<TextView>(R.id.tvSpinnerRouterName)
        val tvPrefix = view.findViewById<TextView>(R.id.tvSpinnerRouterPrefix)
        
        tvName.text = router.name
        tvPrefix.text = router.macPrefix
        tvPrefix.visibility = if (router.macPrefix.isNotEmpty()) View.VISIBLE else View.GONE
        
        if (!router.isActive) {
            tvName.alpha = 0.5f
            tvPrefix.alpha = 0.5f
        }
        
        return view
    }

    fun updateRouters(newRouters: List<Router>) {
        // Create new adapter instance or use notifyDataSetChanged with updated list
    }
}