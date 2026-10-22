package com.thelightphone.lux

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightProgressBar
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.designVerticalPxToSp
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable
import kotlin.math.min

@InitialScreen
class LuxScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, LuxViewModel>(sealedActivity) {

    override val viewModelClass: Class<LuxViewModel>
        get() = LuxViewModel::class.java

    override fun createViewModel() = LuxViewModel(lightContext.ambientLight)

    @Composable
    override fun Content() {
        val state by viewModel.state.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()
        LightTheme(colors = themeColors) {
            LuxContent(state = state, onMeasure = viewModel::measure)
        }
    }
}

@Composable
internal fun LuxContent(state: LuxUiState, onMeasure: () -> Unit) {
    val c = LightThemeTokens.colors
    Column(Modifier.fillMaxSize().background(c.background)) {
        LightTopBar(center = LightTopBarCenter.Text("Lux"))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 1f.gridUnitsAsDp())
                .padding(bottom = 1f.gridUnitsAsDp()),
            verticalArrangement = Arrangement.spacedBy(0.6f.gridUnitsAsDp()),
        ) {
            Header()
            Ruler()
            RadarPanel(state, Modifier.weight(1f).fillMaxWidth())
            Telemetry(state)
            MeasureButton(state, onMeasure)
        }
    }
}

@Composable
private fun Header() {
    val c = LightThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .background(c.content)
                .padding(horizontal = 0.7f.gridUnitsAsDp(), vertical = 0.3f.gridUnitsAsDp())
        ) {
            LightText("LUX-X", LightTextVariant.Subheading, monospace = true, color = c.background)
        }
        Box(Modifier.padding(horizontal = 0.6f.gridUnitsAsDp()).width(3.dp).fillMaxHeight().background(c.content))
        Column {
            LightText("AMBIENT LIGHT METER", LightTextVariant.Fine, monospace = true)
            LightText("LOG SCALE / 0.1 - 100K LX", LightTextVariant.Superfine, monospace = true, lighten = true)
        }
    }
}

@Composable
private fun Ruler() {
    val c = LightThemeTokens.colors
    Canvas(Modifier.fillMaxWidth().height(0.6f.gridUnitsAsDp())) {
        val step = 6.dp.toPx()
        drawLine(c.content, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
        var x = 0f
        var i = 0
        while (x <= size.width) {
            val h = if (i % 5 == 0) size.height else size.height * 0.55f
            drawLine(c.content, Offset(x, size.height), Offset(x, size.height - h), 1.dp.toPx())
            x += step
            i++
        }
    }
}

@Composable
private fun Tab(label: String) {
    val c = LightThemeTokens.colors
    Box(
        Modifier
            .background(c.content)
            .padding(horizontal = 0.5f.gridUnitsAsDp(), vertical = 0.15f.gridUnitsAsDp())
    ) {
        LightText(label, LightTextVariant.Superfine, monospace = true, color = c.background)
    }
}

@Composable
private fun RadarPanel(state: LuxUiState, modifier: Modifier) {
    val c = LightThemeTokens.colors
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 10f.designVerticalPxToSp(),
        color = c.contentSecondary,
    )
    val valueStyle = labelStyle.copy(color = c.content)

    Column(modifier) {
        Tab("FIG. 01 LOG RADAR")
        Box(Modifier.weight(1f).fillMaxWidth().border(1.dp, c.content)) {
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val maxR = min(size.width, size.height) / 2f - 16.dp.toPx()
                val hair = 1.dp.toPx()
                val dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()))

                // corner brackets
                val b = 10.dp.toPx()
                val m = 6.dp.toPx()
                for ((sx, sy) in listOf(0 to 0, 1 to 0, 0 to 1, 1 to 1)) {
                    val x = if (sx == 0) m else size.width - m
                    val y = if (sy == 0) m else size.height - m
                    val dx = if (sx == 0) b else -b
                    val dy = if (sy == 0) b else -b
                    drawLine(c.content, Offset(x, y), Offset(x + dx, y), hair)
                    drawLine(c.content, Offset(x, y), Offset(x, y + dy), hair)
                }

                // axes + dashed diagonals
                drawLine(c.contentSecondary, Offset(cx - maxR, cy), Offset(cx + maxR, cy), hair)
                drawLine(c.contentSecondary, Offset(cx, cy - maxR), Offset(cx, cy + maxR), hair)
                val d = maxR * 0.7071f
                drawLine(c.contentSecondary, Offset(cx - d, cy - d), Offset(cx + d, cy + d), hair, pathEffect = dash)
                drawLine(c.contentSecondary, Offset(cx - d, cy + d), Offset(cx + d, cy - d), hair, pathEffect = dash)

                // decade rings
                for (i in 1..RING_COUNT) {
                    val r = maxR * i / RING_COUNT
                    drawCircle(
                        color = if (i == RING_COUNT) c.content else c.contentSecondary,
                        radius = r,
                        center = Offset(cx, cy),
                        style = if (i == RING_COUNT) Stroke(hair) else Stroke(hair, pathEffect = dash),
                    )
                    drawText(measurer, RING_LABELS[i - 1], Offset(cx + 3.dp.toPx(), cy - r - 12.dp.toPx()), labelStyle)
                }

                // reading
                state.lux?.let { lux ->
                    val r = maxR * luxToRadius(lux)
                    drawCircle(c.content, r, Offset(cx, cy), alpha = 0.14f)
                    drawCircle(c.content, r, Offset(cx, cy), style = Stroke(2.dp.toPx()))
                    drawCircle(c.content, 4.dp.toPx(), Offset(cx, cy - r))
                    drawLine(c.content, Offset(cx, cy - r), Offset(cx + maxR * 0.55f, cy - r), hair)
                    drawText(
                        measurer,
                        formatLux(lux) + " LX",
                        Offset(cx + maxR * 0.55f + 4.dp.toPx(), cy - r - 6.dp.toPx()),
                        valueStyle,
                    )
                }
            }
        }
    }
}

@Composable
private fun Cell(
    label: String,
    value: String,
    modifier: Modifier,
    inverted: Boolean = false,
) {
    val c = LightThemeTokens.colors
    val fg = if (inverted) c.background else c.content
    val sub = if (inverted) c.background else c.contentSecondary
    Column(
        modifier
            .then(if (inverted) Modifier.background(c.content) else Modifier.border(1.dp, c.content))
            .padding(0.5f.gridUnitsAsDp()),
        verticalArrangement = Arrangement.spacedBy(0.2f.gridUnitsAsDp()),
    ) {
        LightText(label, LightTextVariant.Superfine, monospace = true, color = sub)
        Box(Modifier.fillMaxWidth().height(1.dp).background(sub))
        LightText(value, LightTextVariant.Subheading, monospace = true, color = fg, maxLines = 1)
    }
}

@Composable
private fun Telemetry(state: LuxUiState) {
    val gap = 0.5f.gridUnitsAsDp()
    val lux = state.lux
    val reading = lux?.let { formatLux(it) + " LX" } ?: "-- LX"
    val band = lux?.let { bandFor(it).label } ?: "--"
    val spread = if (state.min != null && state.max != null)
        formatLux(state.min) + "-" + formatLux(state.max) else "--"
    val status = when (state.phase) {
        Phase.Ready -> "READY"
        Phase.Measuring -> "SAMPLING"
        Phase.Locked -> "LOCKED"
        Phase.NoSensor -> "NO SENSOR"
        Phase.NoReading -> "NO SIGNAL"
    }
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(gap)) {
            Cell("READING", reading, Modifier.weight(1f).fillMaxHeight())
            Cell("BAND", band, Modifier.weight(1f).fillMaxHeight())
        }
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(gap)) {
            Cell("SPREAD", spread, Modifier.weight(1f).fillMaxHeight())
            Cell("STATUS", status, Modifier.weight(1f).fillMaxHeight(), inverted = true)
        }
    }
}

@Composable
private fun MeasureButton(state: LuxUiState, onMeasure: () -> Unit) {
    val c = LightThemeTokens.colors
    val enabled = state.phase == Phase.Ready || state.phase == Phase.Locked || state.phase == Phase.NoReading
    val label = when (state.phase) {
        Phase.Measuring -> "MEASURING"
        Phase.NoSensor -> "UNAVAILABLE"
        Phase.Locked, Phase.NoReading -> "MEASURE AGAIN"
        Phase.Ready -> "MEASURE"
    }
    Column(
        Modifier
            .fillMaxWidth()
            .border(1.dp, c.content)
            .lightClickable(enabled = enabled, onClick = onMeasure)
            .padding(vertical = 0.7f.gridUnitsAsDp()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LightText(label, LightTextVariant.Button, align = TextAlign.Center, lighten = !enabled)
        if (state.phase == Phase.Measuring) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 0.7f.gridUnitsAsDp(), vertical = 0.3f.gridUnitsAsDp())) {
                LightProgressBar(c, state.progress)
            }
        }
    }
}
