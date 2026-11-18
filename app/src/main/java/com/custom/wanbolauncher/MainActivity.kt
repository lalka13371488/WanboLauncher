package com.custom.wanbolauncher

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.StateSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.net.URL
import kotlin.concurrent.thread

data class AppItem(val name: String, val packageName: String, val icon: Drawable)

class MainActivity : AppCompatActivity() {

    private lateinit var bgImageView: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private var wallIndex = 0

    private val photoIds = listOf(
        "1506744038136-46273834b3fb",
        "1469474968028-56623f02e42e",
        "1470071459604-3b5ec3a7fe05",
        "1501785888041-af3ef285b470",
        "1441974231531-c6227db76b6e"
    )

    private val wallpaperRunnable = object : Runnable {
        override fun run() {
            loadNextWallpaper()
            handler.postDelayed(this, 25000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val frameRoot = FrameLayout(this).apply {
            setBackgroundColor(0xFF0B0F17.toInt())
        }

        bgImageView = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val dimOverlay = View(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x77080B12.toInt(), 0xCC080B12.toInt())
            )
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val contentRoot = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 52, 64, 44)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
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
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 88f
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            setShadowLayer(12f, 0f, 4f, 0x80000000.toInt())
        }

        val date = TextClock(this).apply {
            format24Hour = "EEEE, d MMMM"
            format12Hour = "EEEE, d MMMM"
            setTextColor(0xFFE2E8F0.toInt())
            textSize = 22f
            setPadding(6, 4, 0, 0)
            setShadowLayer(8f, 0f, 2f, 0x80000000.toInt())
        }

        clockBox.addView(clock)
        clockBox.addView(date)

        val grid = GridLayout(this).apply {
            columnCount = 2
            rowCount = 2
        }

        grid.addView(createStylishButton("📶", "Wi-Fi", "Сеть и интернет") {
            openSystemScreen(Settings.ACTION_WIFI_SETTINGS)
        })
        grid.addView(createStylishButton("🎧", "Bluetooth", "Аудио и пульты") {
            openSystemScreen(Settings.ACTION_BLUETOOTH_SETTINGS)
        })
        grid.addView(createStylishButton("📐", "Проекция", "Углы и трапеция") {
            openKeystoneSettings()
        })
        grid.addView(createStylishButton("⚙️", "Настройки", "Система проектора") {
            openSystemScreen(Settings.ACTION_SETTINGS)
        })

        topSection.addView(clockBox)
        topSection.addView(grid)

        val appsLabel = TextView(this).apply {
            text = "ПРИЛОЖЕНИЯ"
            letterSpacing = 0.12f
            setTextColor(0xFFCBD5E1.toInt())
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(8, 0, 0, 14)
        }

        val recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 215
            )
            clipToPadding = false
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        contentRoot.addView(topSection)
        contentRoot.addView(appsLabel)
        contentRoot.addView(recyclerView)

        frameRoot.addView(bgImageView)
        frameRoot.addView(dimOverlay)
        frameRoot.addView(contentRoot)
        setContentView(frameRoot)

        setupAppsList(recyclerView)
    }
        override fun onResume() {
        super.onResume()
        handler.post(wallpaperRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(wallpaperRunnable)
    }

    private fun loadNextWallpaper() {
        val id = photoIds[wallIndex % photoIds.size]
        wallIndex++
        val host = "://unsplash.com"
        val fullUrl = "https://$host/photo-$id?w=1280&q=80"
        thread {
            try {
                val stream = URL(fullUrl).openStream()
                val bmp: Bitmap? = BitmapFactory.decodeStream(stream)
                if (bmp != null) {
                    runOnUiThread {
                        bgImageView.alpha = 0.35f
                        bgImageView.setImageBitmap(bmp)
                        bgImageView.animate().alpha(1f).setDuration(900).start()
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun createStylishButton(iconEmoji: String, title: String, subtitle: String, action: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isFocusable = true
            isClickable = true
            background = createGlassCardDrawable()
            setPadding(24, 18, 24, 18)
            layoutParams = GridLayout.LayoutParams().apply {
                width = 275
                height = 110
                setMargins(12, 12, 12, 12)
            }

            val iconView = TextView(context).apply {
                text = iconEmoji
                textSize = 26f
                setPadding(0, 0, 18, 0)
            }

            val textContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
            }

            val titleView = TextView(context).apply {
                text = title
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(0xFFFFFFFF.toInt())
            }

            val subView = TextView(context).apply {
                text = subtitle
                textSize = 13f
                setTextColor(0xFF94A3B8.toInt())
            }

            textContainer.addView(titleView)
            textContainer.addView(subView)

            addView(iconView)
            addView(textContainer)

            setOnFocusChangeListener { v, hasFocus ->
                v.animate().scaleX(if (hasFocus) 1.05f else 1.0f)
                    .scaleY(if (hasFocus) 1.05f else 1.0f)
                    .setDuration(150).start()
            }
            setOnClickListener { action() }
        }
    }

    private fun openKeystoneSettings() {
        try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.chijimlee.trapezoid",
                    "com.chijimlee.trapezoid.activity.FourPointActivity"
                )
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось открыть углы", Toast.LENGTH_SHORT).show()
            openSystemScreen(Settings.ACTION_DISPLAY_SETTINGS)
        }
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

fun createGlassCardDrawable(): StateListDrawable {
    val focused = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        intArrayOf(0xCC1E293B.toInt(), 0xCC0F172A.toInt())
    ).apply {
        setStroke(4, 0xFF38BDF8.toInt())
        cornerRadius = 24f
    }
    val normal = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        intArrayOf(0x99131A26.toInt(), 0x800B0F17.toInt())
    ).apply {
        setStroke(2, 0x40FFFFFF.toInt())
        cornerRadius = 24f
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
            background = createGlassCardDrawable()
            layoutParams = RecyclerView.LayoutParams(240, 195).apply {
                setMargins(0, 8, 24, 8)
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

        container.setOnFocusChangeListener { v, hasFocus ->
            v.animate().scaleX(if (hasFocus) 1.06f else 1.0f)
                .scaleY(if (hasFocus) 1.06f else 1.0f)
                .setDuration(150).start()
        }

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

