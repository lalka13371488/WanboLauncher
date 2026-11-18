package com.custom.wanbolauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class AppItem(val name: String, val packageName: String, val icon: Drawable)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupQuickButtons()
        setupAppsList()
    }

    private fun setupQuickButtons() {
        findViewById<Button>(R.id.btnWifi).setOnClickListener {
            openSystemScreen(Settings.ACTION_WIFI_SETTINGS)
        }
        findViewById<Button>(R.id.btnBluetooth).setOnClickListener {
            openSystemScreen(Settings.ACTION_BLUETOOTH_SETTINGS)
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            openSystemScreen(Settings.ACTION_SETTINGS)
        }
        findViewById<Button>(R.id.btnProjection).setOnClickListener {
            val wanboIntents = listOf(
                "com.zeasn.projector.setting",
                "com.dangbei.projector.setting",
                "com.android.tv.settings"
            )
            var opened = false
            for (pkg in wanboIntents) {
                val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    startActivity(launchIntent)
                    opened = true
                    break
                }
            }
            if (!opened) {
                openSystemScreen(Settings.ACTION_DISPLAY_SETTINGS)
            }
        }
    }

    private fun openSystemScreen(action: String) {
        try {
            startActivity(Intent(action))
        } catch (e: Exception) {
            Toast.makeText(this, "Раздел недоступен", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAppsList() {
        val recyclerView = findViewById<RecyclerView>(R.id.appsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val pkgAppsList: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)
        val apps = pkgAppsList
            .filter { it.activityInfo.packageName != packageName }
            .map {
                AppItem(
                    name = it.loadLabel(packageManager).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = it.loadIcon(packageManager)
                )
            }
            .sortedBy { it.name.lowercase() }

        recyclerView.adapter = AppsAdapter(apps) { app ->
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) startActivity(launchIntent)
        }
    }

    override fun onBackPressed() {
        // Блокируем выход из лаунчера по кнопке Назад
    }
}

class AppsAdapter(
    private val apps: List<AppItem>,
    private val onClick: (AppItem) -> Unit
) : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    class ViewHolder(val container: LinearLayout, val iconView: ImageView, val titleView: TextView) :
        RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isFocusable = true
            isClickable = true
            background = ContextCompat.getDrawable(context, R.drawable.card_selector)
            layoutParams = RecyclerView.LayoutParams(220, 155).apply {
                setMargins(0, 0, 20, 0)
            }
            setPadding(16, 16, 16, 16)
        }

        val iconView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(72, 72)
        }

        val titleView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 12 }
            gravity = Gravity.CENTER
            maxLines = 1
            textSize = 15f
            setTextColor(0xFFF8FAFC.toInt())
        }

        container.addView(iconView)
        container.addView(titleView)
        return ViewHolder(container, iconView, titleView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.iconView.setImageDrawable(app.icon)
        holder.titleView.text = app.name
        holder.container.setOnClickListener { onClick(app) }
    }

    override fun getItemCount() = apps.size
}
