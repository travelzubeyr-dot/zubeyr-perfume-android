package com.zubeyrperfume.shop

import org.json.JSONArray
import org.json.JSONObject

/* ------------------------------------------------------------------ helpers */

fun JSONArray?.strings(): List<String> {
    if (this == null) return emptyList()
    val out = ArrayList<String>(length())
    for (i in 0 until length()) {
        val v = optString(i, "")
        if (v.isNotBlank()) out.add(v)
    }
    return out
}

fun JSONArray?.objects(): List<JSONObject> {
    if (this == null) return emptyList()
    val out = ArrayList<JSONObject>(length())
    for (i in 0 until length()) optJSONObject(i)?.let { out.add(it) }
    return out
}

fun money(n: Int): String {
    val s = StringBuilder()
    val digits = kotlin.math.abs(n).toString()
    for ((i, c) in digits.withIndex()) {
        if (i > 0 && (digits.length - i) % 3 == 0) s.append(',')
        s.append(c)
    }
    if (n < 0) s.insert(0, '-')
    return "$s ETB"
}

/* ------------------------------------------------------------------- models */

data class Bottle(val ml: Int, val price: Int, val old: Int, val stock: Int)

data class Notes(val top: List<String>, val mid: List<String>, val base: List<String>) {
    val isEmpty: Boolean get() = top.isEmpty() && mid.isEmpty() && base.isEmpty()
}

data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val gender: String,
    val family: String,
    val desc: String,
    val cats: List<String>,
    val sizes: List<Bottle>,
    val notes: Notes,
    val rate: Double,
    val reviewCount: Int,
    val tags: List<String>,
    val seasons: List<String>,
    val occasions: List<String>,
    val smellsLike: List<String>,
    val longevity: Int,
    val projection: Int,
    val glass: String,
    val cap: String,
    val images: List<String>
) {
    val title: String get() = if (brand.isBlank()) name else "$brand $name"
    val fromPrice: Int get() = sizes.minByOrNull { it.price }?.price ?: 0
    val oldPrice: Int get() = sizes.minByOrNull { it.price }?.old ?: 0
    val inStock: Int get() = sizes.sumOf { it.stock }
    val discount: Int
        get() {
            val o = oldPrice
            val p = fromPrice
            return if (o > p && p > 0) ((o - p) * 100.0 / o).toInt() else 0
        }

    fun size(ml: Int): Bottle? = sizes.firstOrNull { it.ml == ml }

    fun matches(q: String): Boolean {
        val t = q.trim().lowercase()
        if (t.isEmpty()) return true
        return listOf(name, brand, family, gender, desc)
            .plus(cats).plus(smellsLike)
            .plus(notes.top).plus(notes.mid).plus(notes.base)
            .any { it.lowercase().contains(t) }
    }

    companion object {
        fun from(o: JSONObject): Product {
            val sizes = ArrayList<Bottle>()
            val sa = o.optJSONArray("sizes")
            if (sa != null) {
                for (i in 0 until sa.length()) {
                    val z = sa.optJSONArray(i) ?: continue
                    sizes.add(
                        Bottle(
                            z.optInt(0, 0),
                            z.optInt(1, 0),
                            z.optInt(2, 0),
                            z.optInt(3, 0)
                        )
                    )
                }
            }
            val n = o.optJSONObject("notes")
            val images = ArrayList<String>()
            o.optString("img", "").takeIf { it.isNotBlank() }?.let { images.add(it) }
            for (u in o.optJSONArray("imgs").strings()) if (!images.contains(u)) images.add(u)

            return Product(
                id = o.optString("id", ""),
                name = o.optString("name", "Perfume"),
                brand = o.optString("brand", ""),
                gender = o.optString("gender", ""),
                family = o.optString("family", ""),
                desc = o.optString("desc", ""),
                cats = o.optJSONArray("cat").strings(),
                sizes = if (sizes.isEmpty()) listOf(Bottle(0, 0, 0, 0)) else sizes,
                notes = Notes(
                    n?.optJSONArray("t").strings(),
                    n?.optJSONArray("m").strings(),
                    n?.optJSONArray("b").strings()
                ),
                rate = o.optDouble("rate", 0.0).let { if (it.isNaN()) 0.0 else it },
                reviewCount = o.optInt("rc", 0),
                tags = o.optJSONArray("tags").strings(),
                seasons = o.optJSONArray("seasons").strings(),
                occasions = o.optJSONArray("occ").strings(),
                smellsLike = o.optJSONArray("like").strings(),
                longevity = o.optInt("lon", 0),
                projection = o.optInt("proj", 0),
                glass = o.optString("glass", "#2B2018"),
                cap = o.optString("cap", "#C8A24A"),
                images = images
            )
        }
    }
}

data class Category(val id: String, val name: String, val sub: String, val color: String) {
    companion object {
        fun from(o: JSONObject) = Category(
            o.optString("id", ""),
            o.optString("name", ""),
            o.optString("sub", ""),
            o.optString("c", "#C8A24A")
        )
    }
}

data class ShipOption(
    val id: String,
    val name: String,
    val note: String,
    val eta: String,
    val badge: String,
    val fee: Int,
    val freeOver: Boolean
) {
    companion object {
        fun from(o: JSONObject) = ShipOption(
            o.optString("id", ""),
            o.optString("name", ""),
            o.optString("note", ""),
            o.optString("eta", ""),
            o.optString("badge", ""),
            o.optInt("fee", 0),
            o.optInt("freeOver", 0) == 1 || o.optBoolean("freeOver", false)
        )

        val defaults = listOf(
            ShipOption("standard", "Standard delivery", "Addis Ababa next day · elsewhere 2–4 days", "1–4 days", "Most chosen", 80, true),
            ShipOption("express", "Express delivery", "Same day in Addis Ababa if ordered before 4:00 PM", "Today", "Fastest", 150, false),
            ShipOption("pickup", "Collect from the shop", "Bole Road, Addis Ababa · open every day", "Ready in 2 hours", "Free", 0, false)
        )
    }
}

data class PayMethod(
    val id: String,
    val name: String,
    val amharic: String,
    val note: String,
    val needsRef: Boolean
) {
    companion object {
        val all = listOf(
            PayMethod("chapa", "Chapa", "ቻፓ", "Card and mobile wallet checkout page", false),
            PayMethod("telebirr", "Telebirr", "ቴሌብር", "Send to our merchant number, then paste the reference", true),
            PayMethod("cbebirr", "CBE Birr", "ሲቢኢ ብር", "Mobile wallet transfer", true),
            PayMethod("bank", "Bank transfer", "ባንክ ዝውውር", "CBE, Awash or Dashen — then paste the transaction number", true),
            PayMethod("cod", "Cash on delivery", "እቃው ሲደርስ ይክፈሉ", "Pay the rider when it reaches you", false),
            PayMethod("santim", "SantimPay", "ሳንቲም ፔይ", "Ethiopian wallet gateway", false),
            PayMethod("card", "Visa / Mastercard", "ካርድ", "International cards via our gateway", false)
        )
    }
}

data class Shop(
    val name: String = "Zubeyr Perfume",
    val slogan: String = "Luxury Fragrance for Everyone",
    val slogan2: String = "Original Perfumes. Delivered Across Ethiopia.",
    val phone: String = "+251914713122",
    val phoneShown: String = "+251 91 471 3122",
    val whatsapp: String = "251914713122",
    val telegram: String = "ZubeyrPerfume",
    val email: String = "sales@zubeyrperfume.com",
    val address: String = "Bole Road, Addis Ababa, Ethiopia",
    val freeOver: Int = 2500,
    val telebirr: String = "",
    val cbeBirr: String = "",
    val bankName: String = "",
    val bankAcct: String = "",
    val bankHolder: String = ""
) {
    companion object {
        fun from(o: JSONObject?): Shop {
            if (o == null) return Shop()
            val d = Shop()
            return Shop(
                name = o.optString("name", d.name),
                slogan = o.optString("slogan", d.slogan),
                slogan2 = o.optString("slogan2", d.slogan2),
                phone = o.optString("phone", d.phone),
                phoneShown = o.optString("phoneShown", d.phoneShown),
                whatsapp = o.optString("whatsapp", d.whatsapp),
                telegram = o.optString("telegram", d.telegram),
                email = o.optString("email", d.email),
                address = o.optString("address", d.address),
                freeOver = o.optInt("freeOverAddis", d.freeOver),
                telebirr = o.optString("telebirr", ""),
                cbeBirr = o.optString("cbeBirr", ""),
                bankName = o.optString("bankName", ""),
                bankAcct = o.optString("bankAcct", ""),
                bankHolder = o.optString("bankHolder", "")
            )
        }
    }

    fun payHint(method: PayMethod): String = when (method.id) {
        "telebirr" -> if (telebirr.isBlank()) method.note else "Send to $telebirr, then paste the reference number."
        "cbebirr" -> if (cbeBirr.isBlank()) method.note else "Send to $cbeBirr, then paste the reference number."
        "bank" -> if (bankAcct.isBlank()) method.note else "$bankName · $bankAcct · $bankHolder"
        else -> method.note
    }
}

data class Catalogue(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val brands: List<String> = emptyList(),
    val shipping: List<ShipOption> = ShipOption.defaults,
    val shop: Shop = Shop()
) {
    fun product(id: String): Product? = products.firstOrNull { it.id == id }

    companion object {
        fun parse(root: JSONObject): Catalogue {
            val products = root.optJSONArray("products").objects().map { Product.from(it) }
            val cats = root.optJSONArray("categories").objects().map { Category.from(it) }
            val ship = root.optJSONArray("shipping").objects().map { ShipOption.from(it) }
            val brands = root.optJSONArray("brands").strings().ifEmpty {
                products.map { it.brand }.filter { it.isNotBlank() }.distinct()
            }
            return Catalogue(
                products = products.filter { it.id.isNotBlank() },
                categories = cats,
                brands = brands,
                shipping = ship.ifEmpty { ShipOption.defaults },
                shop = Shop.from(root.optJSONObject("settings"))
            )
        }
    }
}

/* --------------------------------------------------------------------- cart */

data class CartLine(val productId: String, val ml: Int, val qty: Int)

data class CartRow(val product: Product, val bottle: Bottle, val qty: Int) {
    val line: Int get() = bottle.price * qty
}

/* -------------------------------------------------------------- order state */

data class Customer(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val note: String = "",
    val sms: Boolean = true,
    val subscribe: Boolean = false
)

data class PlacedOrder(val code: String, val total: Int)

data class TrackedOrder(
    val code: String,
    val status: String,
    val placedAt: String,
    val delivery: String,
    val total: Int,
    val events: List<Pair<String, String>>
)
