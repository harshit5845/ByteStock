package com.inventory.manager.ui.chatbot

import com.inventory.manager.data.model.AnalyticsSummary
import com.inventory.manager.data.model.Item
import kotlin.math.roundToInt

class ChatbotEngine {

    private var items: List<Item> = emptyList()
    private var summary: AnalyticsSummary? = null

    data class BotResponse(
        val text: String,
        val suggestions: List<String> = emptyList()
    )

    fun setData(newItems: List<Item>, newSummary: AnalyticsSummary?) {
        items = newItems
        summary = newSummary
    }

    fun process(input: String): BotResponse {
        val q = input.lowercase().trim()
        return when {
            // ── Greetings ──────────────────────────────────────────────────────
            q.matches(Regex("(hi|hello|hey|namaste|helo|hii|hiii).*")) ->
                BotResponse(
                    "👋 Hello! I'm your ByteStock Assistant.\n\nI can help you with:\n• 📦 Stock & inventory queries\n• 🧾 GST calculations\n• 📊 Sales insights\n• 💡 Restock suggestions\n• ❓ How-to guides\n\nWhat do you need help with?",
                    listOf("Show low stock", "Top selling products", "GST rates", "How to add item")
                )

            // ── How-to guides ──────────────────────────────────────────────────
            q.contains("how to add") || q.contains("add item") || q.contains("add product") ->
                BotResponse(
                    "➕ **How to Add an Item:**\n\n1. Tap **Inventory** in the bottom nav\n2. Tap the **+ Add Item** button (bottom right)\n3. Fill in:\n   • Item name & category\n   • Purchase & selling price\n   • GST rate & stock quantity\n   • HSN code (for GST billing)\n4. Tap **Save Item**\n\n💡 Tip: Set a low stock threshold so you get alerts!",
                    listOf("How to create bill", "What is HSN code", "GST rates")
                )

            q.contains("how to") && (q.contains("bill") || q.contains("invoice")) ||
            q.contains("create bill") || q.contains("make bill") ->
                BotResponse(
                    "🧾 **How to Create a Bill:**\n\n1. Tap **Billing** in the bottom nav\n2. (Optional) Enter customer name\n3. Search for products by name\n4. Tap a product to add it to the bill\n5. Use **+/-** to adjust quantities\n6. Add a discount % if needed\n7. Tap **Save Bill ✓**\n8. Select payment mode\n\n✅ Stock is auto-deducted when you save!",
                    listOf("How to add item", "Payment modes", "GST on bill")
                )

            q.contains("how to") && q.contains("search") ||
            q.contains("find product") || q.contains("search item") ->
                BotResponse(
                    "🔍 **How to Search Products:**\n\n• In **Inventory**: Use the search bar at the top — searches by name and category\n• Use **category chips** (All, Food, Electronics…) to filter\n• In **Billing**: Type in the search box to quickly find and add products to a bill",
                    listOf("How to add item", "Low stock items", "Category filter")
                )

            q.contains("how to") && q.contains("edit") ||
            q.contains("update item") || q.contains("change price") ->
                BotResponse(
                    "✏️ **How to Edit an Item:**\n\n1. Go to **Inventory**\n2. Find the item\n3. Tap the **✏️ pencil icon** on the item card\n4. Update any details\n5. Tap **Save Item**\n\n💡 To quickly update stock only, tap the **⊞ grid icon** instead!",
                    listOf("How to update stock", "How to delete item")
                )

            q.contains("delete") || q.contains("remove item") ->
                BotResponse(
                    "🗑️ **How to Delete an Item:**\n\n1. Go to **Inventory**\n2. Find the item\n3. Tap the **✕ red button** on the item card\n4. Confirm deletion\n\n⚠️ This cannot be undone!",
                    listOf("How to add item", "How to edit item")
                )

            q.contains("how to") && q.contains("stock") && q.contains("update") ||
            q.contains("update stock") || q.contains("add stock") ->
                BotResponse(
                    "📦 **How to Update Stock:**\n\n1. Go to **Inventory**\n2. Find the item\n3. Tap the **⊞ grid icon**\n4. Choose Add Stock or Remove Stock\n5. Enter quantity → tap **Update**\n\n💡 Stock is also auto-deducted every time you save a bill!",
                    listOf("Low stock items", "Out of stock items")
                )

            q.contains("how to") && q.contains("profile") ||
            q.contains("setup business") || q.contains("business name") ->
                BotResponse(
                    "👤 **How to Set Up Your Business Profile:**\n\n1. Tap **Profile** in the bottom nav\n2. Tap **Edit Profile**\n3. Enter:\n   • Business name\n   • Owner name\n   • GSTIN (15-digit GST number)\n   • Phone, Email, Address\n4. Tap **Save**\n\n💡 Your GSTIN appears on all bills!",
                    listOf("What is GSTIN", "Default GST rate")
                )

            q.contains("how to") && q.contains("analytics") ||
            q.contains("view report") || q.contains("sales report") ->
                BotResponse(
                    "📊 **How to View Sales Analytics:**\n\n1. Tap **Analytics** in the bottom nav\n2. Select a time window: 7 Days / 30 Days / 3 Months / 1 Year\n3. Tap **↻** to refresh\n\nYou'll see:\n• 💰 Total revenue & units sold\n• 📅 Daily/weekly/monthly bar charts\n• 🔥 Top selling products\n• 📉 Slow/dead stock with restock suggestions\n• 🗂️ Revenue by category\n\n💡 Analytics only appear after you've saved some bills!",
                    listOf("Top selling products", "Restock suggestions", "Revenue total")
                )

            // ── Stock queries ──────────────────────────────────────────────────
            q.contains("low stock") || q.contains("running out") || q.contains("less stock") -> {
                val lowItems = items.filter { it.isLowStock }
                if (lowItems.isEmpty())
                    BotResponse("✅ Great news! All products are well-stocked right now.", listOf("Out of stock items", "Inventory value"))
                else
                    BotResponse(
                        "⚠️ **${lowItems.size} item(s) running low:**\n\n" +
                        lowItems.joinToString("\n") { "• ${it.name}: **${it.stock} ${it.unit}** left (min: ${it.lowStockThreshold})" } +
                        "\n\n💡 Tap the ⊞ icon on any item to restock it.",
                        listOf("Restock suggestions", "Out of stock items", "How to update stock")
                    )
            }

            q.contains("out of stock") || q.contains("finished") || q.contains("zero stock") -> {
                val outItems = items.filter { it.isOutOfStock }
                if (outItems.isEmpty())
                    BotResponse("✅ No items are out of stock!", listOf("Low stock items", "Inventory value"))
                else
                    BotResponse(
                        "❌ **${outItems.size} item(s) out of stock:**\n\n" +
                        outItems.joinToString("\n") { "• ${it.name}" } +
                        "\n\n🚨 These won't be added to bills. Restock urgently!",
                        listOf("How to update stock", "Low stock items")
                    )
            }

            q.contains("all stock") || q.contains("stock list") || q.contains("inventory list") -> {
                if (items.isEmpty())
                    BotResponse("📭 No items in inventory yet. Add your first product!", listOf("How to add item"))
                else {
                    val text = items.take(8).joinToString("\n") {
                        val status = when { it.isOutOfStock -> "❌"; it.isLowStock -> "⚠️"; else -> "✅" }
                        "$status ${it.name}: ${it.stock} ${it.unit}"
                    }
                    val more = if (items.size > 8) "\n\n...and ${items.size - 8} more items" else ""
                    BotResponse("📦 **Your Inventory (${items.size} items):**\n\n$text$more", listOf("Low stock items", "Inventory value"))
                }
            }

            q.contains("how many") && q.contains("product") ||
            q.contains("total product") || q.contains("item count") ->
                BotResponse(
                    "📦 You have **${items.size} products** in your inventory.\n\n" +
                    "• ✅ In Stock: ${items.count { !it.isLowStock && !it.isOutOfStock }}\n" +
                    "• ⚠️ Low Stock: ${items.count { it.isLowStock }}\n" +
                    "• ❌ Out of Stock: ${items.count { it.isOutOfStock }}",
                    listOf("Low stock items", "Inventory value")
                )

            // ── Inventory value ────────────────────────────────────────────────
            q.contains("inventory value") || q.contains("stock worth") ||
            q.contains("stock value") || q.contains("total value") -> {
                val value = items.sumOf { it.stock * it.sellingPrice }
                val costValue = items.sumOf { it.stock * it.purchasePrice }
                val profit = value - costValue
                BotResponse(
                    "💰 **Inventory Valuation:**\n\n" +
                    "• Selling value: **₹%.2f**\n".format(value) +
                    "• Cost value: **₹%.2f**\n".format(costValue) +
                    "• Potential profit: **₹%.2f**\n\n".format(profit) +
                    "Based on ${items.size} products, ${items.sumOf { it.stock }} total units.",
                    listOf("Low stock items", "Top selling products")
                )
            }

            // ── Analytics / Sales ──────────────────────────────────────────────
            q.contains("top sell") || q.contains("best sell") || q.contains("most sold") -> {
                val top = summary?.topProducts
                if (top.isNullOrEmpty())
                    BotResponse("📊 No sales data yet. Save some bills first to see top sellers!", listOf("How to create bill"))
                else
                    BotResponse(
                        "🔥 **Top Selling Products:**\n\n" +
                        top.take(5).mapIndexed { i, p ->
                            "${i + 1}. ${p.itemName}\n   ${p.totalUnitsSold} units • ₹%.0f revenue • avg %.1f/day".format(p.totalRevenue, p.avgDailySales)
                        }.joinToString("\n\n"),
                        listOf("Revenue total", "Slow moving stock", "Restock suggestions")
                    )
            }

            q.contains("slow") || q.contains("dead stock") || q.contains("not selling") -> {
                val slow = summary?.slowProducts
                if (slow.isNullOrEmpty())
                    BotResponse("📈 All your products are selling well!", listOf("Top selling products"))
                else
                    BotResponse(
                        "📉 **Slow / Dead Stock:**\n\n" +
                        slow.take(5).joinToString("\n") {
                            "• ${it.itemName} (${it.demandLabel.emoji} ${it.demandLabel.displayName})"
                        } +
                        "\n\n💡 Consider discounting or reducing orders for these items.",
                        listOf("Restock suggestions", "How to edit item")
                    )
            }

            q.contains("restock") || q.contains("what to order") || q.contains("what to buy") -> {
                val topRestock = summary?.topProducts?.filter { it.restockSuggestion > 0 } ?: emptyList()
                val slowRestock = summary?.slowProducts?.filter { it.restockSuggestion > 0 } ?: emptyList()
                val lowStock = items.filter { it.isLowStock || it.isOutOfStock }
                val combined = (topRestock + slowRestock + lowStock.map { it.name }).distinctBy {
                    if (it is String) it else (it as com.inventory.manager.data.model.ProductAnalysis).itemName
                }
                if (combined.isEmpty())
                    BotResponse("✅ Stock levels look healthy for the next 30 days!", listOf("Inventory value", "Low stock items"))
                else {
                    val text = buildString {
                        if (topRestock.isNotEmpty()) {
                            appendLine("📦 **High demand — order more:**")
                            topRestock.take(3).forEach { appendLine("  • ${it.itemName}: +${it.restockSuggestion} units") }
                            appendLine()
                        }
                        if (lowStock.isNotEmpty()) {
                            appendLine("⚠️ **Currently low/out:**")
                            lowStock.take(3).forEach { appendLine("  • ${it.name}: ${it.stock} ${it.unit} left") }
                        }
                    }
                    BotResponse(text.trim(), listOf("How to update stock", "Top selling products"))
                }
            }

            q.contains("revenue") || q.contains("earning") || q.contains("income") ||
            q.contains("sales total") || q.contains("total sales") -> {
                val s = summary
                if (s == null || s.totalRevenue == 0.0)
                    BotResponse("📊 No sales data yet. Create and save bills to track revenue!", listOf("How to create bill"))
                else
                    BotResponse(
                        "💰 **Sales Summary:**\n\n" +
                        "• Revenue: **₹%.2f**\n".format(s.totalRevenue) +
                        "• Units sold: **${s.totalUnitsSold}**\n" +
                        "• Bills created: **${s.totalBillsAnalysed}**\n\n" +
                        "📊 Go to **Analytics** tab for full charts and trends!",
                        listOf("Top selling products", "How to view analytics")
                    )
            }

            // ── GST Help ───────────────────────────────────────────────────────
            q.contains("gst rate") || q.contains("tax rate") || q.contains("gst slab") -> {
                val category = when {
                    q.contains("food") || q.contains("grocery") -> "🍚 **Food & Grocery:** 0% (fresh), 5% (packaged rice/dal), 12% (frozen)"
                    q.contains("medicine") || q.contains("pharma") -> "💊 **Medicine:** 0% (life-saving), 5% (most medicines), 12% (some Ayurvedic)"
                    q.contains("electronic") || q.contains("mobile") -> "📱 **Electronics:** 12% (mobiles), 18% (laptops/cables), 28% (AC)"
                    q.contains("cloth") || q.contains("apparel") -> "👕 **Clothing:** 5% (≤₹1000), 12% (>₹1000)"
                    else -> "📋 **All GST Slabs:**\n• 0% — Fresh vegetables, milk, eggs\n• 5% — Rice, dal, medicines, clothes\n• 12% — Frozen food, mobiles\n• 18% — Electronics, services\n• 28% — Luxury goods, AC"
                }
                BotResponse(category + "\n\nWhen adding items, select the correct rate in the GST Rate dropdown.", listOf("What is HSN code", "CGST vs SGST", "GST formula"))
            }

            q.contains("gst formula") || q.contains("calculate gst") || q.contains("how gst") -> BotResponse(
                "🧮 **GST Calculation Formula:**\n\n" +
                "**Add GST to price:**\n" +
                "Price with GST = Base Price × (1 + GST%/100)\n" +
                "Example: ₹100 + 18% = ₹118\n\n" +
                "**Extract GST from total:**\n" +
                "GST = Total × Rate / (100 + Rate)\n" +
                "Example: ₹118 → GST = 118 × 18/118 = ₹18\n\n" +
                "**CGST = SGST = GST ÷ 2**\n" +
                "Example: 18% GST → 9% CGST + 9% SGST",
                listOf("GST rates", "What is HSN code", "CGST vs SGST")
            )

            q.contains("cgst") || q.contains("sgst") || q.contains("igst") -> BotResponse(
                "📋 **GST Components:**\n\n" +
                "• **CGST** — Central GST → goes to Central Govt\n" +
                "• **SGST** — State GST → goes to State Govt\n" +
                "• **IGST** — Integrated GST → for inter-state sales\n\n" +
                "For intra-state sales (same state):\n" +
                "GST 18% = 9% CGST + 9% SGST\n\n" +
                "ByteStock auto-calculates CGST & SGST on every bill! ✅",
                listOf("GST formula", "GST rates")
            )

            q.contains("hsn") || q.contains("sac code") -> BotResponse(
                "🔢 **HSN Codes** (Harmonised System Nomenclature)\n\n" +
                "HSN codes classify goods for GST purposes.\n\n" +
                "Common codes:\n" +
                "• 1006 → Rice\n" +
                "• 0713 → Dal/Pulses\n" +
                "• 3004 → Medicines\n" +
                "• 6109 → T-Shirts/Garments\n" +
                "• 8544 → Cables/Wires\n" +
                "• 8517 → Mobile phones\n\n" +
                "💡 Enter the HSN code when adding items for proper GST compliance.",
                listOf("GST rates", "How to add item")
            )

            q.contains("gstin") || q.contains("gst number") || q.contains("gst registration") -> BotResponse(
                "📝 **GSTIN (GST Identification Number)**\n\n" +
                "• 15-digit unique number for registered businesses\n" +
                "• Format: 22AAAAA0000A1Z5\n\n" +
                "**Required if annual turnover exceeds:**\n" +
                "• ₹40 lakhs (goods)\n" +
                "• ₹20 lakhs (services)\n" +
                "• ₹10 lakhs (NE states)\n\n" +
                "💡 Add your GSTIN in **Profile → Edit Profile** so it appears on all bills.",
                listOf("How to setup profile", "What is HSN code")
            )

            q.contains("e-way") || q.contains("eway") -> BotResponse(
                "🚛 **E-Way Bill**\n\n" +
                "Required when goods worth **₹50,000+** are transported.\n\n" +
                "• Generate on the GST portal before dispatch\n" +
                "• Valid for limited distance/time\n" +
                "• Not required for intra-city movement\n\n" +
                "ByteStock tracks your inventory — generate E-Way bills via the GST portal separately.",
                listOf("GST rates", "What is GSTIN")
            )

            q.contains("itc") || q.contains("input tax credit") -> BotResponse(
                "💡 **Input Tax Credit (ITC)**\n\n" +
                "ITC lets you deduct GST paid on purchases from GST collected on sales.\n\n" +
                "Example:\n" +
                "• GST paid on purchase: ₹180\n" +
                "• GST collected on sale: ₹270\n" +
                "• Tax to pay = ₹270 - ₹180 = **₹90**\n\n" +
                "✅ Requirements for ITC:\n" +
                "• You must be GST registered\n" +
                "• Supplier must file their returns\n" +
                "• You must have a valid tax invoice",
                listOf("What is GSTIN", "GST rates", "GST formula")
            )

            // ── Payment modes ──────────────────────────────────────────────────
            q.contains("payment") || q.contains("payment mode") -> BotResponse(
                "💳 **Supported Payment Modes:**\n\n" +
                "• 💵 Cash\n• 📱 UPI\n• 💳 Card\n• 🏦 Net Banking\n• 📄 Cheque\n• 🤝 Credit\n\n" +
                "Select the mode when saving a bill. It's shown on the bill receipt.",
                listOf("How to create bill", "How to view bill history")
            )

            // ── App features ───────────────────────────────────────────────────
            q.contains("feature") || q.contains("what can you do") ||
            q.contains("what can this app") -> BotResponse(
                "✨ **ByteStock Features:**\n\n" +
                "🏠 **Home** — Dashboard with live stats\n" +
                "📦 **Inventory** — Add/edit/search products\n" +
                "🧾 **Billing** — GST bills with auto CGST+SGST\n" +
                "📊 **Analytics** — Sales trends & predictions\n" +
                "👤 **Profile** — Business settings & dark mode\n" +
                "🤖 **This Chatbot** — Your 24/7 assistant!\n\n" +
                "All data stored locally — no internet needed! 🇮🇳",
                listOf("How to add item", "How to create bill", "GST rates")
            )

            q.contains("dark mode") || q.contains("night mode") || q.contains("theme") -> BotResponse(
                "🌙 **Dark Mode:**\n\n" +
                "1. Tap **Profile** in the bottom nav\n" +
                "2. Toggle **🌙 Dark Mode** switch\n\n" +
                "The app will switch instantly to dark theme!",
                listOf("How to setup profile")
            )

            q.contains("bill history") || q.contains("past bill") || q.contains("old bill") -> BotResponse(
                "📋 **View Bill History:**\n\n" +
                "1. Tap **Billing** in the bottom nav\n" +
                "2. Tap the **History** tab at the top\n" +
                "3. All saved bills are listed here\n" +
                "4. Tap any bill to see full details including GST breakdown\n\n" +
                "Bills are sorted newest first.",
                listOf("How to create bill", "Revenue total")
            )

            // ── Help ───────────────────────────────────────────────────────────
            q.contains("help") || q.isEmpty() -> BotResponse(
                "🤖 **I can help you with:**\n\n" +
                "📦 **Inventory** — stock levels, add/edit items\n" +
                "🧾 **Billing** — create bills, payment modes\n" +
                "📊 **Analytics** — sales trends, top products\n" +
                "💰 **GST** — rates, CGST/SGST, HSN codes\n" +
                "💡 **Predictions** — restock suggestions\n" +
                "🔧 **How-to** — guides for every feature\n\n" +
                "Just type your question!",
                listOf("Show low stock", "How to create bill", "GST rates", "Restock suggestions")
            )

            // ── Fallback ───────────────────────────────────────────────────────
            else -> BotResponse(
                "🤔 I'm not sure about \"$input\".\n\nTry asking:\n• \"How to add an item\"\n• \"Show low stock items\"\n• \"What is GST rate for food\"\n• \"Top selling products\"\n• \"How to create a bill\"",
                listOf("How to add item", "GST rates", "Low stock items", "Help")
            )
        }
    }
}
