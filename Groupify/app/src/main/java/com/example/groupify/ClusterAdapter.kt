//package com.example.groupify
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//
//class ClusterAdapter(
//    private val clusters: List<ClusterData>
//) : RecyclerView.Adapter<ClusterAdapter.ClusterViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClusterViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cluster, parent, false)
//        return ClusterViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ClusterViewHolder, position: Int) {
//        val clusterData = clusters[position]
//        holder.bind(clusterData)
//    }
//
//    override fun getItemCount() = clusters.size
//
//    inner class ClusterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        private val folderNameTextView: TextView = view.findViewById(R.id.folderNameTextView)
//        private val appsRecyclerView: RecyclerView = view.findViewById(R.id.appsRecyclerView)
//
//        fun bind(clusterData: ClusterData) {
//            folderNameTextView.text = clusterData.category_name
//            appsRecyclerView.layoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
//            appsRecyclerView.adapter = AppAdapter(clusterData.apps) // apps 리스트 전달
//        }
//    }
//}
