package com.indianservers.paintme

import android.content.Context
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONArray
import org.json.JSONObject

private val ink = Color(0xFF0C0A19)
private val panel = Color(0xFF1D1930)
private val violet = Color(0xFFA84EFF)
private val pink = Color(0xFFFF69C9)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary = violet, background = ink, surface = panel)) { ArtVerse() } }
    }
}

private data class Studio(val title: String, val subtitle: String, val icon: String, val colors: List<Color>, val available: Boolean = false)
private val studios = listOf(
    Studio("Paint", "Your canvas awaits", "✦", listOf(Color(0xFF7635E7), Color(0xFF2B1956)), true),
    Studio("Color", "Find your calm", "◉", listOf(Color(0xFFDB568E), Color(0xFF57204E))),
    Studio("Sketch", "Ideas in motion", "✎", listOf(Color(0xFF6672B2), Color(0xFF292B4C))),
    Studio("Animate", "Bring art to life", "▶", listOf(Color(0xFF168C9D), Color(0xFF12394D))),
    Studio("Learn", "Grow your skills", "◇", listOf(Color(0xFF405CC1), Color(0xFF1F2858))),
    Studio("AI Studio", "Imagine more", "✧", listOf(Color(0xFFDA7855), Color(0xFF632E62)))
)

@Composable
private fun ArtVerse() {
    var editing by remember { mutableStateOf(false) }
    if (editing) Editor(onBack = { editing = false }) else Home(onPaint = { editing = true })
}

@Composable
private fun Home(onPaint: () -> Unit) {
    var message by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF171024), ink, Color(0xFF120D20)))).statusBarsPadding().navigationBarsPadding().padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✦", color = pink, fontSize = 38.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("ArtVerse", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("STUDIO  /  BEYOND IMAGINATION", fontSize = 9.sp, letterSpacing = 1.5.sp, color = Color(0xFFB8ACC8))
            }
            Text("✧", Modifier.background(panel, CircleShape).padding(12.dp), color = Color(0xFFE5C5FF), fontSize = 20.sp)
        }
        Spacer(Modifier.height(38.dp))
        Text("YOUR CREATIVE SPACE", color = Color(0xFFC298F4), fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("What would you like to", color = Color.White, fontSize = 27.sp, lineHeight = 34.sp)
        Text("create today?", color = Color(0xFFE088F2), fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(studios) { studio ->
                Column(Modifier.height(150.dp).fillMaxWidth().background(Brush.linearGradient(studio.colors), RoundedCornerShape(24.dp)).border(1.dp, Color.White.copy(alpha = .20f), RoundedCornerShape(24.dp)).clickable { if (studio.available) onPaint() else message = "${studio.title} Studio is coming in a future update." }.padding(17.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(studio.title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                        Text("↗", color = Color.White.copy(alpha = .7f), fontSize = 18.sp)
                    }
                    Text(studio.subtitle, color = Color.White.copy(alpha = .7f), fontSize = 11.sp)
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) { Text(studio.icon, fontSize = 55.sp, color = Color.White.copy(alpha = .8f), lineHeight = 60.sp) }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 15.dp).background(Color(0xFF211B32), RoundedCornerShape(28.dp)).padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text("⌂  Home", color = Color(0xFFE6B5FF), fontWeight = FontWeight.Bold)
            Text("✧  Create", color = Color(0xFFB9B1C6), modifier = Modifier.clickable { onPaint() })
            Text("▧  Gallery", color = Color(0xFFB9B1C6), modifier = Modifier.clickable { message = "Your last painting opens in Paint Studio." })
        }
    }
    message?.let { AlertDialog(onDismissRequest = { message = null }, title = { Text("ArtVerse Studio") }, text = { Text(it) }, confirmButton = { TextButton(onClick = { message = null }) { Text("Got it") } }) }
}

@Composable
private fun Editor(onBack: () -> Unit) {
    val context = LocalContext.current
    var drawing by remember { mutableStateOf<DrawingView?>(null) }
    var color by remember { mutableIntStateOf(android.graphics.Color.rgb(139, 65, 235)) }
    var size by remember { mutableFloatStateOf(12f) }
    var eraser by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var layerVersion by remember { mutableIntStateOf(0) }
    var showLayers by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri: Uri? ->
        if (uri != null) exportMessage = if (drawing?.exportPng(uri) == true) "PNG exported successfully." else "Could not export PNG. Please try again."
    }
    val colors = listOf(0xFF24202B, 0xFF8B41EB, 0xFFF64BA3, 0xFFFF6658, 0xFFFFB82F, 0xFF21BFA9, 0xFF2688F5).map { it.toInt() }
    Column(Modifier.fillMaxSize().background(ink).statusBarsPadding().navigationBarsPadding().padding(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", Modifier.clickable { drawing?.save(); onBack() }.padding(end = 16.dp), color = Color.White, fontSize = 32.sp)
            Column(Modifier.weight(1f)) { Text("ArtVerse Studio", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold); Text("PAINT STUDIO  •  OFFLINE", color = Color(0xFFB99DCB), fontSize = 9.sp, letterSpacing = 1.sp) }
            TextButton(onClick = { drawing?.undo() }) { Text("Undo") }
            TextButton(onClick = { drawing?.redo() }) { Text("Redo") }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✦  Your canvas", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("Layers", Modifier.clickable { showLayers = true }.padding(8.dp), color = Color(0xFFE0B4FF), fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
        AndroidView(factory = { DrawingView(it).also { view -> drawing = view } }, modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)))
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(20.dp)).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            ToolChip("✎  Brush", !eraser) { eraser = false; drawing?.eraser = false }
            ToolChip("◇  Eraser", eraser) { eraser = true; drawing?.eraser = true }
            TextButton(onClick = { exportLauncher.launch("ArtVerse.png") }) { Text("PNG") }
            TextButton(onClick = { drawing?.save(); saved = true }) { Text("Save") }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            colors.forEach { c -> Box(Modifier.size(if (color == c && !eraser) 38.dp else 32.dp).border(if (color == c && !eraser) 2.dp else 0.dp, Color.White, CircleShape).padding(3.dp).background(Color(c), CircleShape).clickable { color = c; eraser = false; drawing?.apply { brushColor = c; this.eraser = false } }) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Size", color = Color.White, fontSize = 12.sp)
            Slider(value = size, onValueChange = { size = it; drawing?.brushSize = it }, valueRange = 2f..48f, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
            Text(size.toInt().toString(), color = Color.White, fontSize = 12.sp)
        }
    }
    if (showLayers) {
        val view = drawing
        // Reading the version refreshes this list after layer operations.
        val version = layerVersion
        AlertDialog(onDismissRequest = { showLayers = false }, title = { Text("Layers") }, text = {
            Column {
                Text("Tap a layer to paint on it", fontSize = 12.sp, color = Color.LightGray)
                view?.layerLabels()?.reversed()?.forEach { (index, label, visible) ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (visible) "◉" else "○", Modifier.clickable { view.toggleLayer(index); layerVersion = version + 1 }.padding(10.dp), color = Color.White)
                        Text(label + if (index == view.selectedLayer) "  •" else "", Modifier.weight(1f).clickable { view.selectLayer(index); layerVersion = version + 1 }.padding(8.dp), color = Color.White)
                        Text("×", Modifier.clickable { view.deleteLayer(index); layerVersion = version + 1 }.padding(10.dp), color = Color.LightGray)
                    }
                }
                TextButton(onClick = { view?.addLayer(); layerVersion = version + 1 }) { Text("+ Add layer") }
            }
        }, confirmButton = { TextButton(onClick = { showLayers = false }) { Text("Done") } })
    }
    exportMessage?.let { AlertDialog(onDismissRequest = { exportMessage = null }, title = { Text("PNG export") }, text = { Text(it) }, confirmButton = { TextButton(onClick = { exportMessage = null }) { Text("Done") } }) }
    if (saved) AlertDialog(onDismissRequest = { saved = false }, title = { Text("Painting saved") }, text = { Text("Your canvas is stored on this device and will open when you return to Paint Studio.") }, confirmButton = { TextButton(onClick = { saved = false }) { Text("Done") } })
}

@Composable private fun ToolChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(label, Modifier.background(if (selected) Color(0xFF7135CE) else Color.Transparent, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 13.dp, vertical = 10.dp), color = Color.White, fontSize = 12.sp)
}

private data class Sample(val x: Float, val y: Float, val pressure: Float = 1f)
private data class Stroke(val color: Int, val width: Float, val erase: Boolean, val points: MutableList<Sample>)
private data class Layer(val name: String, var visible: Boolean = true, val strokes: MutableList<Stroke> = mutableListOf())
private class DrawingView(context: Context) : View(context) {
    var brushColor = android.graphics.Color.rgb(139, 65, 235)
    var brushSize = 12f
    var eraser = false
    private val layers = mutableListOf(Layer("Layer 1"))
    var selectedLayer = 0
        private set
    private val undone = mutableListOf<Pair<Int, Stroke>>()
    private var current: Stroke? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
    init { setBackgroundColor(android.graphics.Color.WHITE); load() }
    private fun render(canvas: AndroidCanvas, w: Int, h: Int) {
        canvas.drawColor(android.graphics.Color.WHITE)
        layers.filter { it.visible }.forEach { layer ->
            val checkpoint = canvas.saveLayer(0f, 0f, w.toFloat(), h.toFloat(), null)
            layer.strokes.forEach { stroke ->
                paint.color = stroke.color
                paint.xfermode = if (stroke.erase) PorterDuffXfermode(PorterDuff.Mode.CLEAR) else null
                stroke.points.forEachIndexed { index, p ->
                    val previous = stroke.points.getOrElse(index - 1) { p }
                    paint.strokeWidth = (stroke.width * w * ((previous.pressure + p.pressure) / 2f)).coerceAtLeast(0.5f)
                    canvas.drawLine(previous.x * w, previous.y * h, p.x * w + if (index == 0) .01f else 0f, p.y * h, paint)
                }
            }
            paint.xfermode = null
            canvas.restoreToCount(checkpoint)
        }
    }
    override fun onDraw(canvas: AndroidCanvas) { super.onDraw(canvas); render(canvas, width, height) }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (width == 0 || height == 0) return false
        val stylus = event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS || event.getToolType(0) == MotionEvent.TOOL_TYPE_ERASER
        fun sample(x: Float, y: Float, pressure: Float) = Sample((x / width).coerceIn(0f, 1f), (y / height).coerceIn(0f, 1f), if (stylus) pressure.coerceIn(.1f, 1f) else 1f)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!layers[selectedLayer].visible) return false
                current = Stroke(brushColor, brushSize * resources.displayMetrics.density / width, eraser || event.getToolType(0) == MotionEvent.TOOL_TYPE_ERASER, mutableListOf(sample(event.x, event.y, event.pressure)))
                layers[selectedLayer].strokes.add(current!!); undone.clear(); parent.requestDisallowInterceptTouchEvent(true); invalidate(); return true
            }
            MotionEvent.ACTION_MOVE -> { for (i in 0 until event.historySize) current?.points?.add(sample(event.getHistoricalX(i), event.getHistoricalY(i), event.getHistoricalPressure(i))); current?.points?.add(sample(event.x, event.y, event.pressure)); invalidate(); return true }
            MotionEvent.ACTION_UP -> { current?.points?.add(sample(event.x, event.y, event.pressure)); current = null; save(); invalidate(); performClick(); return true }
            MotionEvent.ACTION_CANCEL -> { current = null; save(); return true }
        }
        return true
    }
    override fun performClick(): Boolean { super.performClick(); return true }
    fun undo() { val strokes = layers[selectedLayer].strokes; if (strokes.isNotEmpty()) { undone.add(selectedLayer to strokes.removeAt(strokes.lastIndex)); save(); invalidate() } }
    fun redo() { if (undone.isNotEmpty() && undone.last().first == selectedLayer) { layers[selectedLayer].strokes.add(undone.removeAt(undone.lastIndex).second); save(); invalidate() } }
    fun layerLabels() = layers.mapIndexed { i, l -> Triple(i, l.name, l.visible) }
    fun selectLayer(i: Int) { selectedLayer = i; undone.clear(); save() }
    fun toggleLayer(i: Int) { layers[i].visible = !layers[i].visible; save(); invalidate() }
    fun addLayer() { layers.add(Layer("Layer ${layers.size + 1}")); selectedLayer = layers.lastIndex; undone.clear(); save(); invalidate() }
    fun deleteLayer(i: Int) { if (layers.size > 1) { layers.removeAt(i); selectedLayer = when { selectedLayer > i -> selectedLayer - 1; selectedLayer == i -> i.coerceAtMost(layers.lastIndex); else -> selectedLayer }; undone.clear(); save(); invalidate() } }
    fun exportPng(uri: Uri): Boolean = runCatching {
        require(width > 0 && height > 0)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try { render(AndroidCanvas(bitmap), width, height); context.contentResolver.openOutputStream(uri)?.use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) } ?: error("Cannot open file") } finally { bitmap.recycle() }
        true
    }.getOrDefault(false)
    fun save() {
        val data = JSONArray()
        layers.forEach { layer ->
            val strokes = JSONArray()
            layer.strokes.forEach { s -> val points = JSONArray(); s.points.forEach { points.put(JSONArray().put(it.x.toDouble()).put(it.y.toDouble()).put(it.pressure.toDouble())) }; strokes.put(JSONObject().put("color", s.color).put("width", s.width.toDouble()).put("erase", s.erase).put("points", points)) }
            data.put(JSONObject().put("name", layer.name).put("visible", layer.visible).put("strokes", strokes))
        }
        context.getSharedPreferences("artverse", Context.MODE_PRIVATE).edit().putString("layers", data.toString()).putInt("selectedLayer", selectedLayer).apply()
    }
    private fun parseStrokes(array: JSONArray): MutableList<Stroke> {
        val result = mutableListOf<Stroke>()
        for (i in 0 until array.length()) { val obj = array.getJSONObject(i); val points = obj.getJSONArray("points"); val list = mutableListOf<Sample>(); for (j in 0 until points.length()) { val p = points.getJSONArray(j); list.add(Sample(p.getDouble(0).toFloat(), p.getDouble(1).toFloat(), if (p.length() > 2) p.getDouble(2).toFloat() else 1f)) }; result.add(Stroke(obj.getInt("color"), obj.getDouble("width").toFloat(), obj.optBoolean("erase", false), list)) }
        return result
    }
    private fun load() {
        val prefs = context.getSharedPreferences("artverse", Context.MODE_PRIVATE)
        runCatching {
            val saved = prefs.getString("layers", null)
            if (saved == null) { layers[0].strokes.addAll(parseStrokes(JSONArray(prefs.getString("canvas", "[]")))) }
            else { val data = JSONArray(saved); val loaded = mutableListOf<Layer>(); for (i in 0 until data.length()) { val o = data.getJSONObject(i); loaded.add(Layer(o.getString("name"), o.optBoolean("visible", true), parseStrokes(o.getJSONArray("strokes")))) }; if (loaded.isNotEmpty()) { layers.clear(); layers.addAll(loaded) }; selectedLayer = prefs.getInt("selectedLayer", 0).coerceIn(0, layers.lastIndex) }
        }
    }
}
