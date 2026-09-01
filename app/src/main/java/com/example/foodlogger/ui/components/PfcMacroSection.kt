package com.example.foodlogger.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodlogger.ui.theme.CarbColor
import com.example.foodlogger.ui.theme.EmeraldGreenPrimary
import com.example.foodlogger.ui.theme.FatColor
import com.example.foodlogger.ui.theme.ProteinColor

enum class PfcStatus(val label: String, val color: Color) {
    APPROPRIATE("適正", EmeraldGreenPrimary),
    HIGH("多め", Color(0xFFE65100)),
    LOW("少なめ", Color(0xFF1976D2))
}

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

    // Japanese Ministry of Health ideal PFC ratios
    val pStatus = when {
        pPct < 13 -> PfcStatus.LOW
        pPct > 20 -> PfcStatus.HIGH
        else -> PfcStatus.APPROPRIATE
    }
    val fStatus = when {
        fPct < 20 -> PfcStatus.LOW
        fPct > 30 -> PfcStatus.HIGH
        else -> PfcStatus.APPROPRIATE
    }
    val cStatus = when {
        cPct < 50 -> PfcStatus.LOW
        cPct > 65 -> PfcStatus.HIGH
        else -> PfcStatus.APPROPRIATE
    }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(proteinG, fatG, carbsG) {
        animatedProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PFCバランス（三大栄養素）",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "理想との比較",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Donut Chart & Overview Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Donut Chart with Calorie Center
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        val strokeWidth = 12.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2

                        val sweepP = if (totalMacroKcal > 0) (pKcal / totalMacroKcal * 360f * animatedProgress.value).toFloat() else 0f
                        val sweepF = if (totalMacroKcal > 0) (fKcal / totalMacroKcal * 360f * animatedProgress.value).toFloat() else 0f
                        val sweepC = if (totalMacroKcal > 0) (cKcal / totalMacroKcal * 360f * animatedProgress.value).toFloat() else 0f

                        drawCircle(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            radius = radius,
                            style = Stroke(width = strokeWidth)
                        )

                        var startAngle = -90f
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
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "kcal",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // PFC Comparison Rows
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PfcComparisonRow(
                        label = "タンパク質 (P)",
                        amountG = proteinG,
                        currentPct = pPct,
                        idealRange = "13〜20%",
                        status = pStatus,
                        color = ProteinColor
                    )
                    PfcComparisonRow(
                        label = "脂質 (F)",
                        amountG = fatG,
                        currentPct = fPct,
                        idealRange = "20〜30%",
                        status = fStatus,
                        color = FatColor
                    )
                    PfcComparisonRow(
                        label = "炭水化物 (C)",
                        amountG = carbsG,
                        currentPct = cPct,
                        idealRange = "50〜65%",
                        status = cStatus,
                        color = CarbColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PfcComparisonRow(
    label: String,
    amountG: Double,
    currentPct: Int,
    idealRange: String,
    status: PfcStatus,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = color,
                    modifier = Modifier.size(8.dp)
                ) {}
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = status.color.copy(alpha = 0.15f),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                    color = status.color,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${String.format("%.1f", amountG)}g (${currentPct}%)",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "理想: $idealRange",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
