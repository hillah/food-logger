package com.example.foodlogger.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodlogger.ui.theme.CarbColor
import com.example.foodlogger.ui.theme.FatColor
import com.example.foodlogger.ui.theme.ProteinColor

@Composable
fun PfcMacroSection(
    calories: Double,
    proteinG: Double,
    fatG: Double,
    carbsG: Double,
    modifier: Modifier = Modifier
) {
    val pKcal = proteinG * 4.0
    val fKcal = fatG * 9.0
    val cKcal = carbsG * 4.0
    val totalMacroKcal = pKcal + fKcal + cKcal

    val pPct = if (totalMacroKcal > 0) (pKcal / totalMacroKcal * 100).toInt() else 0
    val fPct = if (totalMacroKcal > 0) (fKcal / totalMacroKcal * 100).toInt() else 0
    val cPct = if (totalMacroKcal > 0) (cKcal / totalMacroKcal * 100).toInt() else 0

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(proteinG, fatG, carbsG) {
        animatedProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Donut Chart with Calorie Center
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = this.center

                    val sweepP = if (totalMacroKcal > 0) (pKcal / totalMacroKcal * 360f * animatedProgress.value).toFloat() else 0f
                    val sweepF = if (totalMacroKcal > 0) (fKcal / totalMacroKcal * 360f * animatedProgress.value).toFloat() else 0f
                    val sweepC = if (totalMacroKcal > 0) (cKcal / totalMacroKcal * 360f * animatedProgress.value).toFloat() else 0f

                    // Draw Background Ring
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )

                    var startAngle = -90f
                    // Protein
                    if (sweepP > 0) {
                        drawArc(
                            color = ProteinColor,
                            startAngle = startAngle,
                            sweepAngle = sweepP,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweepP
                    }
                    // Fat
                    if (sweepF > 0) {
                        drawArc(
                            color = FatColor,
                            startAngle = startAngle,
                            sweepAngle = sweepF,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweepF
                    }
                    // Carbs
                    if (sweepC > 0) {
                        drawArc(
                            color = CarbColor,
                            startAngle = startAngle,
                            sweepAngle = sweepC,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${calories.toInt()}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Macro details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroRow(
                    label = "タンパク質 (P)",
                    amountG = proteinG,
                    pct = pPct,
                    color = ProteinColor
                )
                MacroRow(
                    label = "脂質 (F)",
                    amountG = fatG,
                    pct = fPct,
                    color = FatColor
                )
                MacroRow(
                    label = "炭水化物 (C)",
                    amountG = carbsG,
                    pct = cPct,
                    color = CarbColor
                )
            }
        }
    }
}

@Composable
private fun MacroRow(
    label: String,
    amountG: Double,
    pct: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = color,
                modifier = Modifier.size(10.dp)
            ) {}
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "${String.format("%.1f", amountG)}g (${pct}%)",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
