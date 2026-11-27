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
import androidx.activity.compose.BackHandler
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
    Studio("Color", "Find your calm", "◉", listOf(Color(0xFFDB568E), Color(0xFF57204E)), true),
    Studio("Sketch", "Ideas in motion", "✎", listOf(Color(0xFF6672B2), Color(0xFF292B4C)), true)
)

@Composable
private fun ArtVerse() {
    var studio by remember { mutableStateOf<String?>(null) }
    when (studio) {
        "Paint", "Sketch" -> Editor(studio!!, onBack = { studio = null })
        "Color" -> ColoringEditor(onBack = { studio = null })
        else -> Home(onOpen = { studio = it })
    }
}

@Composable
private fun Home(onOpen: (String) -> Unit) {
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
                Column(Modifier.height(150.dp).fillMaxWidth().background(Brush.linearGradient(studio.colors), RoundedCornerShape(24.dp)).border(1.dp, Color.White.copy(alpha = .20f), RoundedCornerShape(24.dp)).clickable { onOpen(studio.title) }.padding(17.dp)) {
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
            Text("✧  Create", color = Color(0xFFB9B1C6), modifier = Modifier.clickable { onOpen("Paint") })
            Text("▧  Gallery", color = Color(0xFFB9B1C6), modifier = Modifier.clickable { message = "Your last painting opens in Paint Studio." })
        }
    }
    message?.let { AlertDialog(onDismissRequest = { message = null }, title = { Text("ArtVerse Studio") }, text = { Text(it) }, confirmButton = { TextButton(onClick = { message = null }) { Text("Got it") } }) }
}

@Composable
private fun Editor(mode: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var drawing by remember { mutableStateOf<DrawingView?>(null) }
    var color by remember { mutableIntStateOf(if (mode == "Sketch") android.graphics.Color.DKGRAY else android.graphics.Color.rgb(139, 65, 235)) }
    var preset by remember { mutableStateOf(if (mode == "Sketch") "Pencil" else "Round") }
    var shape by remember { mutableStateOf("Freehand") }
    var size by remember { mutableFloatStateOf(12f) }
    var eraser by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var layerVersion by remember { mutableIntStateOf(0) }
    var showLayers by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }
    var layerToDelete by remember { mutableIntStateOf(-1) }
    var renameLayer by remember { mutableStateOf(false) }
    var layerName by remember { mutableStateOf("") }
    var opacity by remember { mutableFloatStateOf(1f) }
    BackHandler { drawing?.save(); onBack() }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri: Uri? ->
        if (uri != null) exportMessage = if (drawing?.exportPng(uri) == true) "PNG exported successfully." else "Could not export PNG. Please try again."
    }
    val colors = (if (mode == "Sketch") listOf(0xFF181820, 0xFF45454C, 0xFF777780, 0xFFAA9B8B, 0xFF704C39, 0xFF46333B, 0xFF26354A) else listOf(0xFF24202B, 0xFF8B41EB, 0xFFF64BA3, 0xFFFF6658, 0xFFFFB82F, 0xFF21BFA9, 0xFF2688F5)).map { it.toInt() }
    Column(Modifier.fillMaxSize().background(ink).statusBarsPadding().navigationBarsPadding().padding(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", Modifier.clickable { drawing?.save(); onBack() }.padding(end = 16.dp), color = Color.White, fontSize = 32.sp)
            Column(Modifier.weight(1f)) { Text("ArtVerse Studio", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold); Text("${mode.uppercase()} STUDIO  •  OFFLINE", color = Color(0xFFB99DCB), fontSize = 9.sp, letterSpacing = 1.sp) }
            TextButton(onClick = { drawing?.undo() }) { Text("Undo") }
            TextButton(onClick = { drawing?.redo() }) { Text("Redo") }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✦  Your canvas", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("Layers", Modifier.clickable { showLayers = true }.padding(8.dp), color = Color(0xFFE0B4FF), fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
        AndroidView(factory = { DrawingView(it, mode).also { view -> view.brushColor = color; drawing = view } }, modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)))
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(20.dp)).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            ToolChip(if (mode == "Sketch") "✎  Pencil" else "✎  Brush", !eraser) { eraser = false; drawing?.eraser = false }
            ToolChip("◇  Eraser", eraser) { eraser = true; shape = "Freehand"; drawing?.apply { this.eraser = true; this.shape = "Freehand" } }
            TextButton(onClick = { exportLauncher.launch("ArtVerse.png") }) { Text("PNG") }
            TextButton(onClick = { drawing?.save(); saved = true }) { Text("Save") }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("Freehand", "Line", "Rectangle", "Ellipse").forEach { tool ->
                Text(tool, Modifier.weight(1f).background(if (shape == tool) Color(0xFF573087) else panel, RoundedCornerShape(10.dp)).clickable { shape = tool; drawing?.shape = tool; eraser = false; drawing?.eraser = false }.padding(vertical = 8.dp), textAlign = TextAlign.Center, color = Color.White, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (if (mode == "Sketch") listOf("Pencil", "Charcoal", "Ink") else listOf("Round", "Soft", "Marker")).forEach { name ->
                Text(name, Modifier.weight(1f).background(if (preset == name) Color(0xFF573087) else panel, RoundedCornerShape(12.dp)).clickable { preset = name; drawing?.preset = name; eraser = false; drawing?.eraser = false }.padding(vertical = 8.dp), textAlign = TextAlign.Center, color = Color.White, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            colors.forEach { c -> Box(Modifier.size(if (color == c && !eraser) 38.dp else 32.dp).border(if (color == c && !eraser) 2.dp else 0.dp, Color.White, CircleShape).padding(3.dp).background(Color(c), CircleShape).clickable { color = c; eraser = false; drawing?.apply { brushColor = c; this.eraser = false } }) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { showColorPicker = true }) { Text("Custom color") }
            Text("Opacity", color = Color.White, fontSize = 12.sp)
            Slider(value = opacity, onValueChange = { opacity = it; drawing?.brushOpacity = it }, valueRange = .05f..1f, modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
            Text("${(opacity * 100).toInt()}%", color = Color.White, fontSize = 11.sp)
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
                        Text("↑", Modifier.clickable { view.moveLayer(index, 1); layerVersion++ }.padding(6.dp), color = Color.LightGray)
                        Text("↓", Modifier.clickable { view.moveLayer(index, -1); layerVersion++ }.padding(6.dp), color = Color.LightGray)
                        Text("×", Modifier.clickable { if (view.layerLabels().size > 1) layerToDelete = index }.padding(8.dp), color = Color.LightGray)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { view?.addLayer(); layerVersion = version + 1 }) { Text("+ Add layer") }
                    TextButton(onClick = { view?.duplicateSelectedLayer(); layerVersion++ }) { Text("Duplicate") }
                    TextButton(onClick = { layerName = view?.selectedLayerName() ?: ""; renameLayer = true }) { Text("Rename") }
                }
                Text("Blend: ${view?.selectedLayerBlend() ?: "Normal"}", Modifier.clickable { view?.cycleLayerBlend(); layerVersion++ }.padding(vertical = 8.dp), color = Color(0xFFE0B4FF))
                Text("Selected layer opacity: ${((view?.selectedLayerOpacity() ?: 1f) * 100).toInt()}%", fontSize = 12.sp)
                Slider(value = view?.selectedLayerOpacity() ?: 1f, onValueChange = { view?.setLayerOpacity(it); layerVersion++ }, valueRange = 0f..1f)
            }
        }, confirmButton = { TextButton(onClick = { showLayers = false }) { Text("Done") } })
    }
    if (renameLayer) AlertDialog(onDismissRequest = { renameLayer = false }, title = { Text("Rename layer") }, text = { OutlinedTextField(value = layerName, onValueChange = { layerName = it.take(40) }, singleLine = true, label = { Text("Name") }) }, confirmButton = { TextButton(onClick = { drawing?.renameSelectedLayer(layerName); layerVersion++; renameLayer = false }, enabled = layerName.isNotBlank()) { Text("Save") } }, dismissButton = { TextButton(onClick = { renameLayer = false }) { Text("Cancel") } })
    if (layerToDelete >= 0) AlertDialog(onDismissRequest = { layerToDelete = -1 }, title = { Text("Delete layer?") }, text = { Text("All strokes on this layer will be removed.") }, confirmButton = { TextButton(onClick = { drawing?.deleteLayer(layerToDelete); layerVersion++; layerToDelete = -1 }) { Text("Delete") } }, dismissButton = { TextButton(onClick = { layerToDelete = -1 }) { Text("Cancel") } })
    if (showColorPicker) ColorPickerDialog(color, onDismiss = { showColorPicker = false }) { selected -> color = selected; eraser = false; drawing?.apply { brushColor = selected; eraser = false }; showColorPicker = false }
    exportMessage?.let { AlertDialog(onDismissRequest = { exportMessage = null }, title = { Text("PNG export") }, text = { Text(it) }, confirmButton = { TextButton(onClick = { exportMessage = null }) { Text("Done") } }) }
    if (saved) AlertDialog(onDismissRequest = { saved = false }, title = { Text("Painting saved") }, text = { Text("Your canvas is stored on this device and will open when you return to this studio.") }, confirmButton = { TextButton(onClick = { saved = false }) { Text("Done") } })
}

@Composable private fun ToolChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(label, Modifier.background(if (selected) Color(0xFF7135CE) else Color.Transparent, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 13.dp, vertical = 10.dp), color = Color.White, fontSize = 12.sp)
}

private data class Sample(val x: Float, val y: Float, val pressure: Float = 1f)
private data class Stroke(val color: Int, val width: Float, val erase: Boolean, val points: MutableList<Sample>, val opacity: Int = 255, val shape: String = "Freehand")
private data class Layer(var name: String, var visible: Boolean = true, val strokes: MutableList<Stroke> = mutableListOf(), var opacity: Float = 1f, var blend: String = "Normal")
private class DrawingView(context: Context, private val mode: String) : View(context) {
    var brushColor = android.graphics.Color.rgb(139, 65, 235)
    var brushSize = 12f
    var brushOpacity = 1f
    var eraser = false
    var shape = "Freehand"
    var preset = if (mode == "Sketch") "Pencil" else "Round"
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
            val layerPaint = Paint().apply {
                alpha = (layer.opacity * 255).toInt()
                xfermode = when (layer.blend) {
                    "Multiply" -> PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                    "Screen" -> PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                    "Overlay" -> PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
                    else -> null
                }
            }
            val checkpoint = canvas.saveLayer(0f, 0f, w.toFloat(), h.toFloat(), layerPaint)
            layer.strokes.forEach { stroke ->
                paint.color = stroke.color
                paint.alpha = stroke.opacity
                paint.xfermode = if (stroke.erase) PorterDuffXfermode(PorterDuff.Mode.CLEAR) else null
                if (stroke.shape != "Freehand" && stroke.points.size > 1) {
                    val first = stroke.points.first(); val last = stroke.points.last()
                    val left = minOf(first.x, last.x) * w; val top = minOf(first.y, last.y) * h
                    val right = maxOf(first.x, last.x) * w; val bottom = maxOf(first.y, last.y) * h
                    paint.strokeWidth = (stroke.width * w).coerceAtLeast(0.5f)
                    when (stroke.shape) {
                        "Line" -> canvas.drawLine(first.x * w, first.y * h, last.x * w, last.y * h, paint)
                        "Rectangle" -> canvas.drawRect(left, top, right, bottom, paint)
                        "Ellipse" -> canvas.drawOval(left, top, right, bottom, paint)
                    }
                } else stroke.points.forEachIndexed { index, p ->
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
                current = Stroke(brushColor, brushSize * resources.displayMetrics.density / width, eraser || event.getToolType(0) == MotionEvent.TOOL_TYPE_ERASER, mutableListOf(sample(event.x, event.y, event.pressure)), (brushOpacity * when (preset) { "Pencil" -> 150; "Charcoal" -> 95; "Soft" -> 80; "Marker" -> 170; else -> 255 }).toInt(), if (eraser) "Freehand" else shape)
                layers[selectedLayer].strokes.add(current!!); undone.clear(); parent.requestDisallowInterceptTouchEvent(true); invalidate(); return true
            }
            MotionEvent.ACTION_MOVE -> {
                val stroke = current
                if (stroke != null) {
                    if (stroke.shape == "Freehand") for (i in 0 until event.historySize) stroke.points.add(sample(event.getHistoricalX(i), event.getHistoricalY(i), event.getHistoricalPressure(i)))
                    else if (stroke.points.size > 1) stroke.points.removeAt(stroke.points.lastIndex)
                    stroke.points.add(sample(event.x, event.y, event.pressure))
                }
                invalidate(); return true
            }
            MotionEvent.ACTION_UP -> { current?.points?.add(sample(event.x, event.y, event.pressure)); current = null; save(); invalidate(); performClick(); return true }
            MotionEvent.ACTION_CANCEL -> { current = null; save(); return true }
        }
        return true
    }
    override fun performClick(): Boolean { super.performClick(); return true }
    fun undo() { val strokes = layers[selectedLayer].strokes; if (strokes.isNotEmpty()) { undone.add(selectedLayer to strokes.removeAt(strokes.lastIndex)); save(); invalidate() } }
    fun redo() { if (undone.isNotEmpty() && undone.last().first == selectedLayer) { layers[selectedLayer].strokes.add(undone.removeAt(undone.lastIndex).second); save(); invalidate() } }
    fun layerLabels() = layers.mapIndexed { i, l -> Triple(i, l.name, l.visible) }
    fun selectedLayerName() = layers[selectedLayer].name
    fun selectedLayerOpacity() = layers[selectedLayer].opacity
    fun selectedLayerBlend() = layers[selectedLayer].blend
    fun cycleLayerBlend() {
        val modes = listOf("Normal", "Multiply", "Screen", "Overlay")
        val layer = layers[selectedLayer]
        layer.blend = modes[(modes.indexOf(layer.blend).coerceAtLeast(0) + 1) % modes.size]
        save(); invalidate()
    }
    fun renameSelectedLayer(name: String) { layers[selectedLayer].name = name.trim(); save() }
    fun setLayerOpacity(value: Float) { layers[selectedLayer].opacity = value.coerceIn(0f, 1f); save(); invalidate() }
    fun selectLayer(i: Int) { selectedLayer = i; undone.clear(); save() }
    fun toggleLayer(i: Int) { layers[i].visible = !layers[i].visible; save(); invalidate() }
    fun addLayer() { layers.add(Layer("Layer ${layers.size + 1}")); selectedLayer = layers.lastIndex; undone.clear(); save(); invalidate() }
    fun duplicateSelectedLayer() {
        val original = layers[selectedLayer]
        val copy = original.copy(name = original.name + " copy", strokes = original.strokes.map { it.copy(points = it.points.toMutableList()) }.toMutableList())
        layers.add(selectedLayer + 1, copy); selectedLayer++; undone.clear(); save(); invalidate()
    }
    fun moveLayer(i: Int, direction: Int) {
        val destination = i + direction
        if (destination !in layers.indices) return
        java.util.Collections.swap(layers, i, destination)
        selectedLayer = when (selectedLayer) { i -> destination; destination -> i; else -> selectedLayer }
        undone.clear(); save(); invalidate()
    }
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
            layer.strokes.forEach { s -> val points = JSONArray(); s.points.forEach { points.put(JSONArray().put(it.x.toDouble()).put(it.y.toDouble()).put(it.pressure.toDouble())) }; strokes.put(JSONObject().put("color", s.color).put("width", s.width.toDouble()).put("erase", s.erase).put("opacity", s.opacity).put("shape", s.shape).put("points", points)) }
            data.put(JSONObject().put("name", layer.name).put("visible", layer.visible).put("opacity", layer.opacity.toDouble()).put("blend", layer.blend).put("strokes", strokes))
        }
        context.getSharedPreferences("artverse", Context.MODE_PRIVATE).edit().putString(if (mode == "Paint") "layers" else "sketch_layers", data.toString()).putInt(if (mode == "Paint") "selectedLayer" else "sketch_selectedLayer", selectedLayer).apply()
    }
    private fun parseStrokes(array: JSONArray): MutableList<Stroke> {
        val result = mutableListOf<Stroke>()
        for (i in 0 until array.length()) { val obj = array.getJSONObject(i); val points = obj.getJSONArray("points"); val list = mutableListOf<Sample>(); for (j in 0 until points.length()) { val p = points.getJSONArray(j); list.add(Sample(p.getDouble(0).toFloat(), p.getDouble(1).toFloat(), if (p.length() > 2) p.getDouble(2).toFloat() else 1f)) }; result.add(Stroke(obj.getInt("color"), obj.getDouble("width").toFloat(), obj.optBoolean("erase", false), list, obj.optInt("opacity", 255), obj.optString("shape", "Freehand"))) }
        return result
    }
    private fun load() {
        val prefs = context.getSharedPreferences("artverse", Context.MODE_PRIVATE)
        runCatching {
            val saved = prefs.getString(if (mode == "Paint") "layers" else "sketch_layers", null)
            if (saved == null) { if (mode == "Paint") layers[0].strokes.addAll(parseStrokes(JSONArray(prefs.getString("canvas", "[]")))) }
            else { val data = JSONArray(saved); val loaded = mutableListOf<Layer>(); for (i in 0 until data.length()) { val o = data.getJSONObject(i); loaded.add(Layer(o.getString("name"), o.optBoolean("visible", true), parseStrokes(o.getJSONArray("strokes")), o.optDouble("opacity", 1.0).toFloat().coerceIn(0f, 1f), o.optString("blend", "Normal"))) }; if (loaded.isNotEmpty()) { layers.clear(); layers.addAll(loaded) }; selectedLayer = prefs.getInt(if (mode == "Paint") "selectedLayer" else "sketch_selectedLayer", 0).coerceIn(0, layers.lastIndex) }
        }
    }
}

@Composable
private fun ColoringEditor(onBack: () -> Unit) {
    var view by remember { mutableStateOf<ColoringView?>(null) }
    var chosen by remember { mutableIntStateOf(0xFFF64BA3.toInt()) }
    var message by remember { mutableStateOf<String?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var template by remember { mutableStateOf("Mandala") }
    var recolor by remember { mutableStateOf(false) }
    var numbered by remember { mutableStateOf(false) }
    BackHandler { onBack() }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri: Uri? ->
        if (uri != null) message = if (view?.exportPng(uri) == true) "PNG exported successfully." else "Could not export PNG. Please try again."
    }
    val palette = listOf(0xFFF64BA3, 0xFF8B41EB, 0xFF2688F5, 0xFF21BFA9, 0xFF64C636, 0xFFFFB82F, 0xFFFF6658, 0xFF24202B).map { it.toInt() }
    Column(Modifier.fillMaxSize().background(ink).statusBarsPadding().navigationBarsPadding().padding(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", Modifier.clickable { onBack() }.padding(end = 16.dp), color = Color.White, fontSize = 32.sp)
            Column(Modifier.weight(1f)) { Text("Coloring Studio", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold); Text("${template.uppercase()}  •  OFFLINE", color = Color(0xFFB99DCB), fontSize = 9.sp, letterSpacing = 1.sp) }
            TextButton(onClick = { view?.undo() }) { Text("Undo") }
            TextButton(onClick = { view?.redo() }) { Text("Redo") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Mandala", "Mosaic").forEach { page ->
                Text(page, Modifier.weight(1f).background(if (template == page) Color(0xFF7135CE) else panel, RoundedCornerShape(12.dp)).clickable { template = page; view?.selectTemplate(page) }.padding(10.dp), color = Color.White, textAlign = TextAlign.Center)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (recolor) "Tap a region to replace matching colors" else "Tap a region to fill", color = Color(0xFFBDB2C8), fontSize = 12.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = { recolor = !recolor; view?.recolor = recolor }) { Text(if (recolor) "Recolor on" else "Recolor") }
        }
        if (template == "Mosaic") {
            TextButton(onClick = { numbered = !numbered; view?.numbered = numbered; view?.invalidate() }) { Text(if (numbered) "Number guide on" else "Show number guide") }
            if (numbered) Text("Use palette colors 1–4 to follow the guide", color = Color.LightGray, fontSize = 11.sp)
        }
        Text("$progress% colored", color = Color(0xFFBDB2C8), fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
        AndroidView(factory = { ColoringView(it).also { v -> view = v; v.color = chosen; v.onProgress = { progress = it }; progress = v.progress() } }, modifier = Modifier.weight(1f).fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            palette.forEachIndexed { index, c ->
                Box(Modifier.size(if (chosen == c) 38.dp else 32.dp).border(if (chosen == c) 2.dp else 0.dp, Color.White, CircleShape).padding(3.dp).background(Color(c), CircleShape).clickable { chosen = c; view?.color = c }, contentAlignment = Alignment.Center) {
                    if (numbered && template == "Mosaic" && index < 4) Text("${index + 1}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(20.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = { confirmReset = true }) { Text("Reset") }
            TextButton(onClick = { showColorPicker = true }) { Text("Custom color") }
            TextButton(onClick = { exporter.launch("ArtVerse-coloring.png") }) { Text("Export PNG") }
        }
    }
    if (confirmReset) AlertDialog(onDismissRequest = { confirmReset = false }, title = { Text("Reset coloring?") }, text = { Text("This clears the colors from this page. You can undo the reset.") }, confirmButton = { TextButton(onClick = { view?.reset(); confirmReset = false }) { Text("Reset") } }, dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } })
    if (showColorPicker) ColorPickerDialog(chosen, onDismiss = { showColorPicker = false }) { chosen = it; view?.color = it; showColorPicker = false }
    message?.let { AlertDialog(onDismissRequest = { message = null }, title = { Text("PNG export") }, text = { Text(it) }, confirmButton = { TextButton(onClick = { message = null }) { Text("Done") } }) }
}

private class ColoringView(context: Context) : View(context) {
    var color: Int = 0xFFF64BA3.toInt()
    var recolor = false
    var numbered = false
    var onProgress: (Int) -> Unit = {}
    fun progress() = colors.count { it != android.graphics.Color.WHITE } * 100 / colors.size
    private var template = "Mandala"
    private var colors = IntArray(17) { android.graphics.Color.WHITE }
    private val undo = mutableListOf<IntArray>()
    private val redo = mutableListOf<IntArray>()
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = android.graphics.Color.rgb(48, 31, 58); strokeWidth = 2.5f }
    init { loadPage() }
    private fun pageKey() = if (template == "Mandala") "coloring" else "coloring_mosaic"
    private fun loadPage() {
        colors = IntArray(if (template == "Mandala") 17 else 16) { android.graphics.Color.WHITE }
        val saved = context.getSharedPreferences("artverse", Context.MODE_PRIVATE).getString(pageKey(), null)
        runCatching { if (saved != null) { val json = JSONArray(saved); if (json.length() == colors.size) for (i in colors.indices) colors[i] = json.getInt(i) } }
    }
    fun selectTemplate(page: String) { if (page == template) return; template = page; loadPage(); undo.clear(); redo.clear(); onProgress(progress()); invalidate() }
    private fun drawArt(canvas: AndroidCanvas, w: Int, h: Int, showGuide: Boolean = true) {
        canvas.drawColor(android.graphics.Color.rgb(252, 247, 251))
        val scale = minOf(w, h) / 420f
        canvas.save(); canvas.translate(w / 2f, h / 2f); canvas.scale(scale, scale)
        if (template == "Mosaic") {
            for (i in colors.indices) {
                val x = -180f + (i % 4) * 90f; val y = -180f + (i / 4) * 90f
                val rect = android.graphics.RectF(x, y, x + 90f, y + 90f)
                fill.color = colors[i]; canvas.drawRect(rect, fill); canvas.drawRect(rect, outline)
                if (showGuide && numbered && colors[i] == android.graphics.Color.WHITE) {
                    val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.DKGRAY; textSize = 24f; textAlign = Paint.Align.CENTER }
                    canvas.drawText("${(i / 4 + i % 4) % 4 + 1}", x + 45f, y + 53f, label)
                }
            }
            canvas.restore(); return
        }
        for (ring in 0..1) {
            for (i in 0..7) {
                canvas.save(); canvas.rotate(i * 45f + if (ring == 0) 22.5f else 0f)
                val rect = if (ring == 0) android.graphics.RectF(-37f, -198f, 37f, -38f) else android.graphics.RectF(-32f, -139f, 32f, -18f)
                fill.color = colors[ring * 8 + i]; canvas.drawOval(rect, fill); canvas.drawOval(rect, outline); canvas.restore()
            }
        }
        fill.color = colors[16]; canvas.drawCircle(0f, 0f, 35f, fill); canvas.drawCircle(0f, 0f, 35f, outline)
        canvas.restore()
    }
    override fun onDraw(canvas: AndroidCanvas) { super.onDraw(canvas); drawArt(canvas, width, height) }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked != MotionEvent.ACTION_UP) return true
        val scale = minOf(width, height) / 420f
        val x = (event.x - width / 2f) / scale; val y = (event.y - height / 2f) / scale
        var hit = if (template == "Mosaic") {
            if (x >= -180f && x < 180f && y >= -180f && y < 180f) ((y + 180f) / 90f).toInt() * 4 + ((x + 180f) / 90f).toInt() else -1
        } else if (x*x + y*y <= 35f*35f) 16 else -1
        if (template == "Mandala" && hit < 0) for (ring in 1 downTo 0) {
            for (i in 7 downTo 0) {
                val angle = Math.toRadians((i * 45.0 + if (ring == 0) 22.5 else 0.0))
                val rx = x * kotlin.math.cos(angle) + y * kotlin.math.sin(angle)
                val ry = -x * kotlin.math.sin(angle) + y * kotlin.math.cos(angle)
                val cy = if (ring == 0) -118f else -78.5f; val a = if (ring == 0) 37f else 32f; val b = if (ring == 0) 80f else 60.5f
                if ((rx*rx)/(a*a) + ((ry-cy)*(ry-cy))/(b*b) <= 1.0) { hit = ring * 8 + i; break }
            }
            if (hit >= 0) break
        }
        if (hit >= 0 && colors[hit] != color) { undo.add(colors.copyOf()); val previous = colors[hit]; if (recolor) { for (i in colors.indices) if (colors[i] == previous) colors[i] = color } else colors[hit] = color; redo.clear(); save(); invalidate() }
        performClick(); return true
    }
    override fun performClick(): Boolean { super.performClick(); return true }
    fun undo() { if (undo.isNotEmpty()) { redo.add(colors.copyOf()); colors = undo.removeAt(undo.lastIndex); save(); invalidate() } }
    fun redo() { if (redo.isNotEmpty()) { undo.add(colors.copyOf()); colors = redo.removeAt(redo.lastIndex); save(); invalidate() } }
    fun reset() { if (colors.any { it != android.graphics.Color.WHITE }) { undo.add(colors.copyOf()); colors.fill(android.graphics.Color.WHITE); redo.clear(); save(); invalidate() } }
    private fun save() { val a = JSONArray(); colors.forEach { a.put(it) }; context.getSharedPreferences("artverse", Context.MODE_PRIVATE).edit().putString(pageKey(), a.toString()).apply(); onProgress(progress()) }
    fun exportPng(uri: Uri): Boolean = runCatching { require(width > 0 && height > 0); val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); try { drawArt(AndroidCanvas(bitmap), width, height, false); context.contentResolver.openOutputStream(uri)?.use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) } ?: error("Cannot open file") } finally { bitmap.recycle() }; true }.getOrDefault(false)
}


@Composable
private fun ColorPickerDialog(initial: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val hsv = remember(initial) { FloatArray(3).also { android.graphics.Color.colorToHSV(initial, it) } }
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }
    val result = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Custom color") }, text = {
        Column {
            Box(Modifier.fillMaxWidth().height(52.dp).background(Color(result), RoundedCornerShape(10.dp)))
            Text("Hue ${hue.toInt()}°")
            Slider(value = hue, onValueChange = { hue = it }, valueRange = 0f..360f)
            Text("Saturation ${(saturation * 100).toInt()}%")
            Slider(value = saturation, onValueChange = { saturation = it })
            Text("Brightness ${(value * 100).toInt()}%")
            Slider(value = value, onValueChange = { value = it })
            Text(String.format("#%06X", result and 0xFFFFFF), color = Color.LightGray)
        }
    }, confirmButton = { TextButton(onClick = { onSelect(result) }) { Text("Use color") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
