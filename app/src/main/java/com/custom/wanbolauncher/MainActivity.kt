package com.custom.wanbolauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.StateSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class AppItem(val name: String, val packageName: String, val icon: Drawable)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0B0F17.toInt())
            setPadding(64, 56, 64, 48)
        }

        val topSection = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        val clockBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            )
        }

        val clock = TextClock(this).apply {
            format24Hour = "HH:mm"
            format12Hour = "HH:mm"
            setTextColor(0xFFF8FAFC.toInt())
            textSize = 84f
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        }

        val date = TextClock(this).apply {
            format24Hour = "EEEE, d MMMM"
            format12Hour = "EEEE, d MMMM"
            setTextColor(0xFF94A3B8.toInt())
            textSize = 22f
            setPadding(4, 8, 0, 0)
        }

        clockBox.addView(clock)
        clockBox.addView(date)

        val grid = GridLayout(this).apply {
            columnCount = 2
            rowCount = 2
        }

        grid.addView(createQuickButton("Wi-Fi") { openSystemScreen(Settings.ACTION_WIFI_SETTINGS) })
        grid.addView(createQuickButton("Bluetooth") { openSystemScreen(Settings.ACTION_BLUETOOTH_SETTINGS) })
        grid.addView(createQuickButton("Проекция") { openProjectionSettings() })
        grid.addView(createQuickButton("Настройки") { openSystemScreen(Settings.ACTION_SETTINGS) })

        topSection.addView(clockBox)
        topSection.addView(grid)

        val appsLabel = TextView(this).apply {
            text = "Приложения"
            setTextColor(0xFF64748B.toInt())
            textSize = 16f
            setPadding(0, 0, 0, 16)
        }

        val recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 210
            )
            clipToPadding = false
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        root.addView(topSection)
        root.addView(appsLabel)
        root.addView(recyclerView)
        setContentView(root)

        setupAppsList(recyclerView)
    }

    private fun createQuickButton(label: String, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 18f
            setTextColor(0xFFF8FAFC.toInt())
            isFocusable = true
            background = createCardDrawable()
            layoutParams = GridLayout.LayoutParams().apply {
                width = 230
                height = 100
                setMargins(12, 12, 12, 12)
            }
            setOnClickListener { action() }
        }
    }

    private fun openProjectionSettings() {
        val wanboIntents = listOf(
            "com.zeasn.projector.setting",
            "com.dangbei.projector.setting",
            "com.android.tv.settings"
        )
        for (pkg in wanboIntents) {
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                startActivity(launchIntent)
                return
            }
        }
        openSystemScreen(Settings.ACTION_DISPLAY_SETTINGS)
    }

    private fun openSystemScreen(action: String) {
        try {
            startActivity(Intent(action))
        } catch (e: Exception) {
            Toast.makeText(this, "Раздел недоступен", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAppsList(recyclerView: RecyclerView) {
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
    }
}

fun createCardDrawable(): StateListDrawable {
    val focused = GradientDrawable().apply {
        setColor(0xFF1E293B.toInt())
        setStroke(4, 0xFF38BDF8.toInt())
        cornerRadius = 20f
    }
    val normal = GradientDrawable().apply {
        setColor(0xFF131A26.toInt())
        setStroke(2, 0xFF1E293B.toInt())
        cornerRadius = 20f
    }
    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), focused)
        addState(StateSet.WILD_CARD, normal)
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
            background = createCardDrawable()
            layoutParams = RecyclerView.LayoutParams(240, 190).apply {
                setMargins(0, 0, 24, 0)
            }
            setPadding(16, 16, 16, 16)
        }

        val iconView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(84, 84)
        }

        val titleView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 14 }
            gravity = Gravity.CENTER
            maxLines = 1
            textSize = 16f
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
