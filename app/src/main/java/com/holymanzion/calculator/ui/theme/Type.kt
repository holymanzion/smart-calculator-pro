package com.holymanzion.calculator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val CalculatorTypography = Typography()

/** Expression line: monospaced digits keep the text from shifting as you type. */
val ExpressionTextStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 34.sp,
    lineHeight = 42.sp,
)

/** Result line, shown smaller than the expression until `=` is pressed. */
val ResultTextStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 26.sp,
    lineHeight = 32.sp,
)

val KeyTextStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 26.sp,
)

val SmallKeyTextStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 17.sp,
)
