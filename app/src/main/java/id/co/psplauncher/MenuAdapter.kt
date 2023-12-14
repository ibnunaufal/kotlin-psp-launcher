package id.co.psplauncher

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import id.co.psplauncher.main.MainActivity

class MenuAdapter(val listMenu: ArrayList<Menu>, ctx: Context): RecyclerView.Adapter<MenuAdapter.ListViewHolder>() {
    private var context: Context = ctx
    inner class ListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var name: TextView = itemView.findViewById(R.id.name)
        var icon: ImageView = itemView.findViewById(R.id.icon)
        var entire: LinearLayout = itemView.findViewById(R.id.entire)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuAdapter.ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        view.isFocusable = true
        return ListViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: MenuAdapter.ListViewHolder, position: Int) {
        val menu = listMenu[position]
        holder.name.text = menu.name
        holder.icon.setImageDrawable(menu.icon)

        holder.itemView.setOnLongClickListener {
            var context: Context = holder.itemView.context
            context as MainActivity
            context.openAppDetail(menu.label, menu.name)
            true
        }
        holder.itemView.setOnClickListener {
            val context: Context = holder.itemView.context
            val i: Intent? = context.packageManager.getLaunchIntentForPackage(menu.label)
            context.startActivity(i)
        }
        holder.itemView.isFocusable = true
        holder.itemView.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
//                holder.entire.background = holder.itemView.context.getDrawable(R.drawable.active_new)

            }else{
                holder.entire.setBackgroundColor(Color.parseColor("#00FFFFFF"))
            }
        }

    }

    override fun getItemCount(): Int {
        return listMenu.size
    }

    fun openAppDetail(app: String){
        val builder = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        builder.setTitle("Opsi")

        builder.setNegativeButton("Batal"){
                _, _ ->
        }

        val temp: MutableList<String> = mutableListOf()

        if (app.contains("solusinegeri")){
            temp.add("Hapus Aplikasi")
        }
        temp.add("Buka Detail Aplikasi")

        val devices = temp.toTypedArray()
        builder.setItems(
            devices
        ) { _, which ->
            if (temp[which] == "Hapus Aplikasi") {
                confirmDelete(app)
            } else {
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$app")))
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    fun confirmDelete(packageName: String){
        val builder = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        var name = ""
        listMenu.forEach {
            if (packageName == it.label){
                name = it.name
            }
        }
        builder.setTitle("Anda yakin akan melakukan menghapus $name?")
        builder.setPositiveButton("Ya"){
                _,_ -> doRemove(packageName)
        }
        builder.setNegativeButton("Batal"){
                _, _ ->
        }

        val dialog = builder.create()
        dialog.show()
    }
    fun doRemove(packageName: String){
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$packageName")
        context.startActivity(intent)
    }
}