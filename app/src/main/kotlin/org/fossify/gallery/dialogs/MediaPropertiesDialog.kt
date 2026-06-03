package org.fossify.gallery.dialogs

import android.app.Activity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.exifinterface.media.ExifInterface
import org.fossify.commons.dialogs.PropertiesDialog
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.isImageFast
import org.fossify.commons.extensions.isRawFast
import org.fossify.gallery.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MediaPropertiesDialog(
    activity: Activity,
    path: String,
    countHiddenItems: Boolean = false
) {
    init {
        val dialog = PropertiesDialog(activity, path, countHiddenItems)
        addGpsLocation(activity, dialog, path)
    }

    private fun addGpsLocation(activity: Activity, dialog: PropertiesDialog, path: String) {
        if (!path.isImageFast() && !path.isRawFast()) {
            return
        }

        val gpsLocation = getGpsLocation(path)
        if (gpsLocation == null) {
            return
        }

        runCatching {
            val addProperty = dialog.javaClass.superclass.getDeclaredMethod(
                "addProperty",
                Int::class.javaPrimitiveType,
                String::class.java,
                Int::class.javaPrimitiveType
            )
            addProperty.isAccessible = true
            getPropertyView(dialog)?.addView(createMapView(activity, gpsLocation))
        }
    }

    private fun getPropertyView(dialog: PropertiesDialog): ViewGroup? {
        val field = dialog.javaClass.superclass.getDeclaredField("mPropertyView")
        field.isAccessible = true
        return field.get(dialog) as? ViewGroup
    }

    private fun createMapView(activity: Activity, gpsLocation: GpsLocation): LinearLayout {
        val density = activity.resources.displayMetrics.density
        val point = GeoPoint(gpsLocation.latitude, gpsLocation.longitude)

        Configuration.getInstance().userAgentValue = activity.packageName

        val title = TextView(activity).apply {
            text = activity.getString(R.string.show_on_map)
            setTextColor(activity.getProperTextColor())
            textSize = 14f
        }

        val mapView = MapView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (180 * density).toInt()
            ).apply {
                topMargin = (8 * density).toInt()
            }
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(16.0)
            controller.setCenter(point)
            overlays.add(Marker(this).apply {
                position = point
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setTitle(activity.getString(R.string.show_on_map))
            })
            onResume()
        }

        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((16 * density).toInt(), (10 * density).toInt(), (16 * density).toInt(), (10 * density).toInt())
            addView(title)
            addView(mapView)
        }
    }

    private fun getGpsLocation(path: String): GpsLocation? {
        val exif = try {
            ExifInterface(path)
        } catch (_: Exception) {
            return null
        }

        val latLon = FloatArray(2)
        if (!exif.getLatLong(latLon)) {
            return null
        }

        val location = StringBuilder("${latLon[0]}, ${latLon[1]}")
        val altitude = exif.getAltitude(0.0)
        if (altitude != 0.0) {
            location.append(", ${altitude}m")
        }

        return GpsLocation(latLon[0].toDouble(), latLon[1].toDouble(), location.toString())
    }

    private data class GpsLocation(
        val latitude: Double,
        val longitude: Double,
        val coordinates: String
    )
}
