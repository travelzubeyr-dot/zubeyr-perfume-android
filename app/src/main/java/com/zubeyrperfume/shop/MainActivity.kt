package com.zubeyrperfume.shop

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zubeyrperfume.shop.ui.CartScreen
import com.zubeyrperfume.shop.ui.CatalogScreen
import com.zubeyrperfume.shop.ui.CheckoutScreen
import com.zubeyrperfume.shop.ui.DoneScreen
import com.zubeyrperfume.shop.ui.Gold
import com.zubeyrperfume.shop.ui.HomeScreen
import com.zubeyrperfume.shop.ui.Ink
import com.zubeyrperfume.shop.ui.MoreScreen
import com.zubeyrperfume.shop.ui.Paper
import com.zubeyrperfume.shop.ui.ProductScreen
import com.zubeyrperfume.shop.ui.TrackScreen
import com.zubeyrperfume.shop.ui.ZubeyrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZubeyrTheme { ShopApp() }
        }
    }
}

private data class Tab(val screen: Screen, val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopApp(vm: ShopViewModel = viewModel()) {
    val context = LocalContext.current
    val shop = vm.catalogue.shop

    val tabs = listOf(
        Tab(Screen.Home, "Home", Icons.Filled.Home),
        Tab(Screen.Shop, "Shop", Icons.Filled.Storefront),
        Tab(Screen.Cart, "Bag", Icons.Filled.ShoppingBag),
        Tab(Screen.Track, "Track", Icons.Filled.LocalShipping),
        Tab(Screen.More, "More", Icons.Filled.Menu)
    )

    BackHandler(enabled = true) {
        if (!vm.back()) (context as? ComponentActivity)?.finish()
    }

    val screen = vm.current
    val title = when (screen) {
        is Screen.Home -> shop.name
        is Screen.Shop -> "All perfumes"
        is Screen.Cart -> "Your bag"
        is Screen.Track -> "Track order"
        is Screen.More -> "Zubeyr Perfume"
        is Screen.Checkout -> "Checkout"
        is Screen.Done -> "Thank you"
        is Screen.Detail -> vm.catalogue.product(screen.productId)?.brand ?: "Perfume"
    }
    val deep = vm.stack.size > 1

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        if (deep) {
                            IconButton(onClick = { vm.back() }) {
                                Icon(Icons.Filled.ArrowBack, "Back", tint = Paper)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Ink,
                        titleContentColor = Paper,
                        navigationIconContentColor = Paper
                    )
                )
                if (vm.loading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Gold,
                        trackColor = Ink
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Paper) {
                tabs.forEach { tab ->
                    val selected = screen::class == tab.screen::class
                    NavigationBarItem(
                        selected = selected,
                        onClick = { vm.openTab(tab.screen) },
                        icon = {
                            if (tab.screen is Screen.Cart && vm.cartCount > 0) {
                                BadgedBox(badge = { Badge(containerColor = Gold, contentColor = Ink) { Text("${vm.cartCount}") } }) {
                                    Icon(tab.icon, tab.label)
                                }
                            } else {
                                Icon(tab.icon, tab.label)
                            }
                        },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Ink,
                            selectedTextColor = Ink,
                            indicatorColor = Gold.copy(alpha = 0.25f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (screen) {
                is Screen.Home -> HomeScreen(vm) { openWhatsApp(context, shop, null) }
                is Screen.Shop -> CatalogScreen(vm)
                is Screen.Cart -> CartScreen(vm)
                is Screen.Checkout -> CheckoutScreen(vm)
                is Screen.Track -> TrackScreen(vm)
                is Screen.Detail -> ProductScreen(vm, screen.productId) { p -> openWhatsApp(context, shop, p) }
                is Screen.Done -> DoneScreen(
                    vm = vm,
                    code = screen.code,
                    total = screen.total,
                    onWhatsApp = { openWhatsApp(context, shop, null) },
                    onCall = { open(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:${shop.phone}"))) }
                )
                is Screen.More -> MoreScreen(
                    vm = vm,
                    onCall = { open(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:${shop.phone}"))) },
                    onWhatsApp = { openWhatsApp(context, shop, null) },
                    onTelegram = { openUrl(context, "https://t.me/${shop.telegram}") },
                    onEmail = { open(context, Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${shop.email}"))) },
                    onWebsite = { openUrl(context, Store.SITE) }
                )
            }
        }
    }
}

/* -------------------------------------------------------------- outbound */

private fun openWhatsApp(context: Context, shop: Shop, product: Product?) {
    val text = if (product == null) {
        "Hello Zubeyr Perfume, I have a question about an order."
    } else {
        "Hello, I am interested in ${product.title} (${product.sizes.firstOrNull()?.ml ?: 0} ml)."
    }
    openUrl(context, "https://wa.me/${shop.whatsapp}?text=" + Uri.encode(text))
}

private fun openUrl(context: Context, url: String) =
    open(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)))

private fun open(context: Context, intent: Intent) {
    try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No app on this phone can open that.", Toast.LENGTH_SHORT).show()
    }
}
