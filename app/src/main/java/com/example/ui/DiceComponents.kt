package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DieType
import com.example.model.SingleDie
import com.example.ui.theme.CleanAccentPurple
import com.example.ui.theme.CleanCardBorder
import com.example.ui.theme.CleanPipColor
import com.example.ui.theme.CleanPillBg
import com.example.ui.theme.CleanTextPrimary
import com.example.ui.theme.CleanTextSecondary
import com.example.ui.theme.CleanWhite
import kotlin.random.Random

@Composable
fun DiceArena(
    dice: List<SingleDie>,
    dieType: DieType,
    isRolling: Boolean,
    onDieClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val count = dice.size

    when {
        count <= 2 -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dice.forEach { die ->
                    DieCard(
                        die = die,
                        dieType = dieType,
                        isRolling = isRolling,
                        size = if (count == 1) 140.dp else 128.dp,
                        pipSize = if (count == 1) 16.dp else 14.dp,
                        onClick = { onDieClick(die.id) }
                    )
                }
            }
        }
        count <= 4 -> {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    dice.take(2).forEach { die ->
                        DieCard(
                            die = die,
                            dieType = dieType,
                            isRolling = isRolling,
                            size = 110.dp,
                            pipSize = 12.dp,
                            onClick = { onDieClick(die.id) }
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    dice.drop(2).take(2).forEach { die ->
                        DieCard(
                            die = die,
                            dieType = dieType,
                            isRolling = isRolling,
                            size = 110.dp,
                            pipSize = 12.dp,
                            onClick = { onDieClick(die.id) }
                        )
                    }
                }
            }
        }
        else -> {
            // 5 or 6 dice
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    dice.take(3).forEach { die ->
                        DieCard(
                            die = die,
                            dieType = dieType,
                            isRolling = isRolling,
                            size = 92.dp,
                            pipSize = 10.dp,
                            onClick = { onDieClick(die.id) }
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    dice.drop(3).forEach { die ->
                        DieCard(
                            die = die,
                            dieType = dieType,
                            isRolling = isRolling,
                            size = 92.dp,
                            pipSize = 10.dp,
                            onClick = { onDieClick(die.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DieCard(
    die: SingleDie,
    dieType: DieType,
    isRolling: Boolean,
    size: Dp,
    pipSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(isRolling) {
        if (isRolling && !die.isHeld) {
            val randomTargetRotation = (if (Random.nextBoolean()) 360f else -360f) + Random.nextInt(-45, 45)
            rotationAnim.animateTo(
                targetValue = randomTargetRotation,
                animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
            )
            rotationAnim.snapTo(0f)
            scaleAnim.animateTo(
                targetValue = 1.08f,
                animationSpec = tween(120)
            )
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(200, easing = LinearOutSlowInEasing)
            )
        } else {
            rotationAnim.snapTo(0f)
            scaleAnim.snapTo(1f)
        }
    }

    val cardShape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .size(size)
            .scale(scaleAnim.value)
            .rotate(rotationAnim.value)
            .shadow(
                elevation = if (die.isHeld) 6.dp else 4.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(cardShape)
            .background(if (die.isHeld) CleanPillBg.copy(alpha = 0.35f) else CleanWhite)
            .border(
                BorderStroke(
                    width = if (die.isHeld) 2.dp else 1.dp,
                    color = if (die.isHeld) CleanAccentPurple else CleanCardBorder
                ),
                shape = cardShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("die_card_${die.id}"),
        contentAlignment = Alignment.Center
    ) {
        // Die content
        if (dieType.isCoin) {
            CoinDisplay(value = die.value, size = size)
        } else if (dieType == DieType.D6) {
            D6PipGrid(value = die.value, size = size, pipSize = pipSize)
        } else {
            PolyhedralDisplay(dieType = dieType, value = die.value, size = size)
        }

        // Held indicator pill badge
        if (die.isHeld) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(20.dp)
                    .background(CleanAccentPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Held Die",
                    tint = CleanWhite,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun D6PipGrid(
    value: Int,
    size: Dp,
    pipSize: Dp,
    modifier: Modifier = Modifier
) {
    // 3x3 grid matrix representation for values 1 to 6
    val grid = when (value) {
        1 -> listOf(
            listOf(false, false, false),
            listOf(false, true,  false),
            listOf(false, false, false)
        )
        2 -> listOf(
            listOf(false, false, true),
            listOf(false, false, false),
            listOf(true,  false, false)
        )
        3 -> listOf(
            listOf(false, false, true),
            listOf(false, true,  false),
            listOf(true,  false, false)
        )
        4 -> listOf(
            listOf(true,  false, true),
            listOf(false, false, false),
            listOf(true,  false, true)
        )
        5 -> listOf(
            listOf(true,  false, true),
            listOf(false, true,  false),
            listOf(true,  false, true)
        )
        6 -> listOf(
            listOf(true,  false, true),
            listOf(true,  false, true),
            listOf(true,  false, true)
        )
        else -> listOf(
            listOf(false, false, false),
            listOf(false, true,  false),
            listOf(false, false, false)
        )
    }

    val containerInnerSize = size * 0.58f
    val spacing = (containerInnerSize - (pipSize * 3)) / 2

    Column(
        modifier = modifier.size(containerInnerSize),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (col in 0..2) {
                    val active = grid[row][col]
                    if (active) {
                        Box(
                            modifier = Modifier
                                .size(pipSize)
                                .clip(CircleShape)
                                .background(CleanPipColor)
                        )
                    } else {
                        Box(modifier = Modifier.size(pipSize))
                    }
                }
            }
        }
    }
}

@Composable
fun PolyhedralDisplay(
    dieType: DieType,
    value: Int,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value.toString(),
            fontSize = if (size > 100.dp) 38.sp else 28.sp,
            fontWeight = FontWeight.Bold,
            color = CleanTextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = dieType.label.lowercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = CleanAccentPurple.copy(alpha = 0.8f),
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun CoinDisplay(
    value: Int,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val isHeads = value == 1
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.5f)
                .clip(CircleShape)
                .background(CleanPillBg)
                .border(2.dp, CleanAccentPurple.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isHeads) "H" else "T",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CleanAccentPurple
            )
        }
        Text(
            text = if (isHeads) "HEADS" else "TAILS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CleanTextPrimary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
