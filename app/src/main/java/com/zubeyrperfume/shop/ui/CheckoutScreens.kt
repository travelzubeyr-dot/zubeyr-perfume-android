package com.zubeyrperfume.shop.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zubeyrperfume.shop.CartRow
import com.zubeyrperfume.shop.PayMethod
import com.zubeyrperfume.shop.Screen
import com.zubeyrperfume.shop.ShopViewModel
import com.zubeyrperfume.shop.money

/* ==================================================================== CART */

@Composable
fun CartScreen(vm: ShopViewModel) {
    val rows = vm.rows()

    if (rows.isEmpty()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(Gutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))
            Text("Your bag is empty", style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(Modifier.height(8.dp))
            Text(
                "Pick a perfume and it will wait for you here.",
                style = MaterialTheme.typography.bodyMedium,
                color = Faint
            )
            Spacer(Modifier.height(18.dp))
            GoldButton("Browse perfumes") { vm.clearFilters(); vm.go(Screen.Shop) }
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Gutter)
        ) {
            Spacer(Modifier.height(10.dp))
            rows.forEach { row -> CartRowView(row, vm) }
            Spacer(Modifier.height(10.dp))
            Summary(vm)
            Spacer(Modifier.height(20.dp))
        }

        Surface(shadowElevation = 8.dp, color = Paper) {
            Column(Modifier.padding(Gutter)) {
                GoldButton("Checkout · ${money(vm.total())}", Modifier.fillMaxWidth()) {
                    vm.checkoutError = null
                    vm.go(Screen.Checkout)
                }
            }
        }
    }
}

@Composable
private fun CartRowView(row: CartRow, vm: ShopViewModel) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        ProductImage(
            row.product,
            Modifier
                .size(84.dp)
                .clickable { vm.openProduct(row.product.id) }
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(row.product.brand.uppercase(), style = MaterialTheme.typography.labelMedium, color = Gold)
            Text(
                row.product.name,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text("${row.bottle.ml} ml", style = MaterialTheme.typography.bodySmall, color = Faint)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                QtyStepper(row.qty, row.bottle.stock) { vm.setQty(row, it) }
                Spacer(Modifier.width(10.dp))
                Text(money(row.line), style = MaterialTheme.typography.titleMedium, color = Ink)
            }
            Text(
                "Remove",
                style = MaterialTheme.typography.bodySmall,
                color = Faint,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clickable { vm.remove(row) }
            )
        }
    }
    HorizontalDivider(color = Line)
}

@Composable
private fun Summary(vm: ShopViewModel) {
    val free = vm.catalogue.shop.freeOver
    val d = vm.delivery()
    Column {
        SummaryLine("Subtotal", money(vm.subtotal()))
        SummaryLine(d?.name ?: "Delivery", if (vm.deliveryFee() == 0) "Free" else money(vm.deliveryFee()))
        if (free > 0 && vm.subtotal() < free && d?.freeOver == true) {
            Text(
                "Add ${money(free - vm.subtotal())} more for free delivery in Addis Ababa.",
                style = MaterialTheme.typography.bodySmall,
                color = Gold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = Line)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(
                money(vm.total()),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Faint)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Body)
    }
}

/* ================================================================ CHECKOUT */

@Composable
fun CheckoutScreen(vm: ShopViewModel) {
    val shop = vm.catalogue.shop
    val c = vm.customer

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Gutter)
        ) {
            Spacer(Modifier.height(10.dp))
            Text("Where should it go?", style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(Modifier.height(10.dp))

            Field("Full name", c.name) { vm.customer = c.copy(name = it) }
            Field("Phone number", c.phone, KeyboardType.Phone) { vm.customer = c.copy(phone = it) }
            Field("Email (optional, for the receipt)", c.email, KeyboardType.Email) { vm.customer = c.copy(email = it) }
            Field("Address or landmark", c.address, lines = 2) { vm.customer = c.copy(address = it) }
            Field("Anything the rider should know (optional)", c.note, lines = 2) { vm.customer = c.copy(note = it) }

            TickBox("Text me the tracking code", c.sms) { vm.customer = c.copy(sms = it) }
            TickBox("Email me new arrivals and price drops", c.subscribe) { vm.customer = c.copy(subscribe = it) }

            Spacer(Modifier.height(18.dp))
            Text("Delivery", style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(Modifier.height(6.dp))
            vm.catalogue.shipping.forEach { option ->
                val free = option.freeOver && shop.freeOver > 0 && vm.subtotal() >= shop.freeOver
                Choice(
                    selected = vm.deliveryId == option.id,
                    title = option.name,
                    note = listOfNotNull(option.note.takeIf { it.isNotBlank() }, option.eta.takeIf { it.isNotBlank() })
                        .joinToString(" · "),
                    trailing = if (free || option.fee == 0) "Free" else money(option.fee),
                    badge = option.badge
                ) { vm.deliveryId = option.id }
            }

            Spacer(Modifier.height(18.dp))
            Text("Payment", style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(Modifier.height(6.dp))
            PayMethod.all.forEach { p ->
                Choice(
                    selected = vm.paymentId == p.id,
                    title = "${p.name}  ${p.amharic}",
                    note = shop.payHint(p),
                    trailing = "",
                    badge = ""
                ) { vm.paymentId = p.id }
            }

            if (vm.payment().needsRef) {
                Spacer(Modifier.height(6.dp))
                Field("Transaction or reference number", vm.reference) { vm.reference = it }
            }

            Spacer(Modifier.height(18.dp))
            Summary(vm)

            vm.checkoutError?.let {
                Spacer(Modifier.height(12.dp))
                Surface(color = Sale.copy(alpha = 0.08f), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Sale,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        Surface(shadowElevation = 8.dp, color = Paper) {
            Column(Modifier.padding(Gutter)) {
                GoldButton(
                    text = if (vm.placing) "Sending your order…" else "Place order · ${money(vm.total())}",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !vm.placing
                ) { vm.placeOrder() }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Prices and stock are confirmed by the shop before the order is accepted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Faint
                )
            }
        }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    keyboard: KeyboardType = KeyboardType.Text,
    lines: Int = 1,
    onValue: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        singleLine = lines == 1,
        minLines = lines,
        shape = RoundedCornerShape(6.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}

@Composable
private fun TickBox(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChange, colors = CheckboxDefaults.colors(checkedColor = Ink))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Body)
    }
}

@Composable
private fun Choice(
    selected: Boolean,
    title: String,
    note: String,
    trailing: String,
    badge: String,
    onSelect: () -> Unit
) {
    Surface(
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Ink else Line),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(onClick = onSelect)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = Ink)
            )
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = Ink)
                    if (badge.isNotBlank()) {
                        Spacer(Modifier.width(6.dp))
                        Tag(badge, Gold, Ink)
                    }
                }
                if (note.isNotBlank()) {
                    Text(note, style = MaterialTheme.typography.bodySmall, color = Faint)
                }
            }
            if (trailing.isNotBlank()) {
                Text(trailing, style = MaterialTheme.typography.titleMedium, color = Ink)
            }
        }
    }
}

/* ============================================================= ORDER PLACED */

@Composable
fun DoneScreen(vm: ShopViewModel, code: String, total: Int, onWhatsApp: () -> Unit, onCall: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Gutter)
    ) {
        Spacer(Modifier.height(30.dp))
        Text("Order received", style = MaterialTheme.typography.displaySmall, color = Ink)
        Spacer(Modifier.height(10.dp))
        Text(
            "We will call you on ${vm.customer.phone.ifBlank { "your number" }} to confirm before the rider sets off.",
            style = MaterialTheme.typography.bodyLarge,
            color = Body
        )

        Spacer(Modifier.height(20.dp))
        Surface(color = Ink, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("Tracking code", style = MaterialTheme.typography.bodySmall, color = Gold)
                Text(code, style = MaterialTheme.typography.headlineMedium, color = Paper)
                Spacer(Modifier.height(8.dp))
                Text("Total ${money(total)}", style = MaterialTheme.typography.titleMedium, color = Paper)
            }
        }

        Spacer(Modifier.height(20.dp))
        GoldButton("Track this order", Modifier.fillMaxWidth()) {
            vm.go(Screen.Track)
            vm.track(code)
        }
        Spacer(Modifier.height(10.dp))
        QuietButton("Send us a message on WhatsApp", Modifier.fillMaxWidth(), onWhatsApp)
        Spacer(Modifier.height(10.dp))
        QuietButton("Call the shop", Modifier.fillMaxWidth(), onCall)
        Spacer(Modifier.height(10.dp))
        QuietButton("Keep shopping", Modifier.fillMaxWidth()) { vm.go(Screen.Home) }
    }
}

/* ================================================================ TRACKING */

@Composable
fun TrackScreen(vm: ShopViewModel) {
    var code by remember { mutableStateOf(vm.savedOrderCodes().firstOrNull().orEmpty()) }
    val saved = vm.savedOrderCodes()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Gutter)
    ) {
        Text("Track an order", style = MaterialTheme.typography.headlineSmall, color = Ink)
        Spacer(Modifier.height(6.dp))
        Text(
            "Enter the code from your receipt, for example ZP-260902-14.",
            style = MaterialTheme.typography.bodyMedium,
            color = Faint
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase() },
            label = { Text("Tracking code") },
            singleLine = true,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (saved.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                saved.take(3).forEach { s ->
                    Chip(s, code == s) { code = s; vm.track(s) }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        GoldButton(
            text = if (vm.tracking) "Looking…" else "Find my order",
            modifier = Modifier.fillMaxWidth(),
            enabled = !vm.tracking
        ) { vm.track(code) }

        vm.trackError?.let {
            Spacer(Modifier.height(14.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = Sale)
        }

        vm.tracked?.let { o ->
            Spacer(Modifier.height(20.dp))
            Surface(border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(o.code, style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text(
                        "Placed ${o.placedAt} · ${o.delivery}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Faint
                    )
                    Spacer(Modifier.height(12.dp))
                    val flow = listOf(
                        "new" to "Order received",
                        "confirmed" to "Confirmed by phone",
                        "shipped" to "Out for delivery",
                        "delivered" to "Delivered"
                    )
                    val reached = flow.indexOfFirst { it.first == o.status }
                    if (o.status == "cancelled") {
                        Text("This order was cancelled.", style = MaterialTheme.typography.bodyLarge, color = Sale)
                    } else {
                        flow.forEachIndexed { i, step ->
                            Row(
                                Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .background(if (i <= reached) Gold else Line, RoundedCornerShape(5.dp))
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    step.second,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (i <= reached) Ink else Faint
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Line)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.bodyMedium, color = Faint)
                        Text(money(o.total), style = MaterialTheme.typography.titleMedium, color = Ink)
                    }
                }
            }
        }
    }
}

/* ==================================================================== MORE */

@Composable
fun MoreScreen(
    vm: ShopViewModel,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onTelegram: () -> Unit,
    onEmail: () -> Unit,
    onWebsite: () -> Unit
) {
    val shop = vm.catalogue.shop
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Ink)
                .padding(Gutter)
        ) {
            Text(shop.name, style = MaterialTheme.typography.headlineMedium, color = Gold)
            Text(shop.slogan, style = MaterialTheme.typography.bodyMedium, color = Paper)
        }

        Column(Modifier.padding(Gutter)) {
            RowLink("Call ${shop.phoneShown}", onCall)
            RowLink("WhatsApp us", onWhatsApp)
            RowLink("Telegram @${shop.telegram}", onTelegram)
            RowLink(shop.email, onEmail)
            RowLink("Open zubeyrperfume.com", onWebsite)
            RowLink("Track an order") { vm.go(Screen.Track) }
            RowLink(if (vm.loading) "Refreshing catalogue…" else "Refresh prices and stock") { vm.refresh() }

            Spacer(Modifier.height(18.dp))
            Text("Where to find us", style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(shop.address, style = MaterialTheme.typography.bodyMedium, color = Body)

            Spacer(Modifier.height(18.dp))
            Text("How you can pay", style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(
                PayMethod.all.joinToString(" · ") { it.name },
                style = MaterialTheme.typography.bodyMedium,
                color = Body
            )

            Spacer(Modifier.height(18.dp))
            Text(
                if (vm.live) "Catalogue is live from the shop." else "Catalogue saved on this phone. Connect to update it.",
                style = MaterialTheme.typography.bodySmall,
                color = Faint
            )
            Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = Faint)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RowLink(label: String, onClick: () -> Unit) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp)
        )
        HorizontalDivider(color = Line)
    }
}
