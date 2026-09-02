package com.zubeyrperfume.shop.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zubeyrperfume.shop.Product
import com.zubeyrperfume.shop.money

fun hexColor(value: String, fallback: Color): Color = try {
    Color(android.graphics.Color.parseColor(if (value.startsWith("#")) value else "#$value"))
} catch (_: Exception) {
    fallback
}

/** Perfumes without a photograph are drawn as their own bottle, using the
 *  glass and cap colours stored with the product. */
@Composable
fun BottleArt(glass: String, cap: String, modifier: Modifier = Modifier) {
    val g = hexColor(glass, Color(0xFF2B2018))
    val c = hexColor(cap, Gold)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val bw = w * 0.44f
        val bh = h * 0.52f
        val left = (w - bw) / 2f
        val top = h * 0.36f

        drawRoundRect(
            color = c,
            topLeft = Offset(w / 2f - bw * 0.17f, h * 0.14f),
            size = Size(bw * 0.34f, h * 0.11f),
            cornerRadius = CornerRadius(bw * 0.06f, bw * 0.06f)
        )
        drawRect(
            color = c.copy(alpha = 0.55f),
            topLeft = Offset(w / 2f - bw * 0.11f, h * 0.24f),
            size = Size(bw * 0.22f, h * 0.13f)
        )
        drawRoundRect(
            color = g,
            topLeft = Offset(left, top),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(bw * 0.20f, bw * 0.20f)
        )
        drawRoundRect(
            color = c.copy(alpha = 0.9f),
            topLeft = Offset(left + bw * 0.17f, top + bh * 0.34f),
            size = Size(bw * 0.66f, bh * 0.24f),
            cornerRadius = CornerRadius(bw * 0.04f, bw * 0.04f)
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.16f),
            topLeft = Offset(left + bw * 0.10f, top + bh * 0.10f),
            size = Size(bw * 0.14f, bh * 0.46f),
            cornerRadius = CornerRadius(bw * 0.07f, bw * 0.07f)
        )
    }
}

@Composable
fun ProductImage(product: Product, modifier: Modifier = Modifier) {
    Box(modifier.background(Mist), contentAlignment = Alignment.Center) {
        val url = product.images.firstOrNull()
        if (url.isNullOrBlank()) {
            BottleArt(product.glass, product.cap, Modifier.fillMaxSize())
        } else {
            AsyncImage(
                model = url,
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun Stars(rate: Double, count: Int, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Star, null, tint = Gold, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            String.format("%.1f", rate) + if (count > 0) "  ($count)" else "",
            style = MaterialTheme.typography.bodySmall,
            color = Faint
        )
    }
}

@Composable
fun Tag(text: String, background: Color = Ink, foreground: Color = Paper) {
    Surface(color = background, shape = RoundedCornerShape(3.dp)) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = foreground,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Ink else Paper,
        contentColor = if (selected) Paper else Body,
        border = BorderStroke(1.dp, if (selected) Ink else Line),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            maxLines = 1
        )
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Ink)
        if (action != null && onAction != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelLarge,
                color = Gold,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

@Composable
fun GoldButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Paper),
        modifier = modifier.height(50.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun QuietButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, Line),
        modifier = modifier.height(50.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = Ink)
    }
}

@Composable
fun PriceLine(product: Product) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            money(product.fromPrice),
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            fontWeight = FontWeight.Bold
        )
        if (product.oldPrice > product.fromPrice) {
            Spacer(Modifier.width(6.dp))
            Text(
                money(product.oldPrice),
                style = MaterialTheme.typography.bodySmall,
                color = Faint,
                textDecoration = TextDecoration.LineThrough
            )
        }
    }
}

@Composable
fun ProductCard(product: Product, onOpen: () -> Unit, onAdd: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Line),
        color = Paper,
        modifier = Modifier.clickable(onClick = onOpen)
    ) {
        Column {
            Box {
                ProductImage(
                    product,
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.86f)
                )
                Row(
                    Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (product.discount > 0) Tag("-${product.discount}%", Sale)
                    if (product.tags.contains("new")) Tag("New", Gold, Ink)
                    if (product.inStock == 0) Tag("Sold out", Faint)
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(
                    product.brand.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Gold,
                    maxLines = 1
                )
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Stars(product.rate, product.reviewCount)
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriceLine(product)
                    Surface(
                        color = if (product.inStock > 0) Ink else Line,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = product.inStock > 0, onClick = onAdd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Add, "Add to bag", tint = Paper, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
