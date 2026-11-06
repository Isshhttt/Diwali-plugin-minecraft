# 🪔 Diwali Festival Minecraft Plugin (Paper 1.21.x)

A feature-rich, high-performance Diwali festival plugin built natively for Paper/Purpur 1.21.x using Java 21 and modern Item Displays (`item_display`). Comes bundled with its own standalone 3D custom resource pack.

---

## ✨ Features Included

### 🌟 Jhalar Wire Lights
* **Two-Point Hanging System:** Right-click Block A (anchor point) and right-click Block B (up to 20 blocks away) to stretch dynamic drooping catenary wires adorned with glowing 3D fairy light bulbs.
* **4 Color Variants:** Warm Golden, Multicolor RGB, Pure White, and Saffron/Orange.
* **Real Illumination:** Casts actual Minecraft block light (Level 15) onto the surrounding area.
* **Tool-Specific Removal:** Safely snipped with **Shears** to collect the item back.

### 🪔 Diyas & Lanterns
* **Earthen Clay Diya (Mitti Ka Diya):** Placeable on solid surfaces or floating realistically on water.
* **Akash Kandil:** Vibrant hanging octagonal paper lantern with decorative tassels.
* **Sky Lantern:** Releases into the night sky with a warm glow, gently ascending into the clouds with flame and smoke trails.
* **Fuel & Ignition:**
  * Requires **Pure Desi Ghee** to fuel (burns for 2 hours per fuel application).
  * Ignited using the custom **Diwali Lighter**.
  * Extinguishes after 2 hours; easily refueled with Ghee.
  * Pickaxe or bare hands breaks and collects the Diya.

### 🎆 Firecrackers (Patakhe) & Safety
* **Chakri (Ground Spinner):** Whirls and dances across the pavement for 11 seconds, spraying golden spiral spark loops and whistling hiss.
* **Anar (Flower Pot):** Multi-stage spark fountain erupting 6.5 blocks into the sky (Golden foundation transitioning into Emerald Green & Ruby Red sparks).
* **Phuljhadi (Handheld Sparkler):** Hold near open flame/diya for 15-20s (interactive action bar progress). Ignites for 50s of crackling sizzle trails while moving.
* **Ladi (String Garland):** Rapid machine-gun pops and thick white smoke screen (Available in 100-Wala & 1000-Wala sizes) with a grand finale double bang.
* **Sutli Bomb (Atom Bomb):** 4-second smoking fuse, ground shockwave, fixed **7 Hearts (14 HP)** true armor-piercing damage, and inflicts the custom **Burnt** effect (ash/ember swirl, ringing sound, slowness, brief darkness).
* **Skyrocket:** High-speed whistling rocket ascending 30 blocks with a sharp celebratory bang.
* **Skyshot:** Heavy mortar launching comets into colossal multi-colored starburst canopies (Golden Willow, Emerald Rain, Ruby Crimson, Multi-color Rainbow). Available in Single Shell and 12-Shot Aerial Cake varieties.
* **Server Safety:** 100% zero block/ground destruction.

### 🌸 Decorations, Sweets & Gifting
* **Marigold Flower Toran:** Yellow & orange floral door garland.
* **Diwali Sweets (Mithai):**
  * *Kaju Katli:* Haste II & Speed I (3 minutes).
  * *Motichoor Ladoo:* Absorption II & Saturation (3 minutes).
  * *Gulab Jamun:* Regeneration I & Glowing Festive Aura (3 minutes).
* **Interactive Diwali Gift Box:**
  * Place down and right-click to open a 9-slot packaging GUI.
  * Pick recipient from the active online player heads list.
  * Seal the gift, break it to take, and place it in front of your friend.
  * Designated recipient left-clicks to unwrap with celebratory confetti, fireworks, and sound bursts!
* **Tools & Fuel:**
  * *Diwali Lighter:* 100 uses durability.
  * *Pure Desi Ghee:* Smelt milk bucket in furnace or buy from shop.

---

## 🛠 Commands & Permissions

| Command | Permission | Description |
|---|---|---|
| `/diwali shop` | `diwali.shop` | Opens the festival bazaar to buy items with Emeralds |
| `/diwali give <player> <item> [amount]` | `diwali.admin` | Gives festival items directly |
| `/diwali reload` | `diwali.admin` | Reloads configs and messages |

---

## 📦 Installation & Resource Pack

1. Place `build/libs/Diwali-1.0.0.jar` into your server's `plugins/` folder.
2. Start or restart your server.
3. The plugin will automatically extract `plugins/Diwali/DiwaliPack.zip` on first launch.
4. Host `DiwaliPack.zip` (via `server.properties` `resource-pack=` or your server pack host), or put it in your client's `.minecraft/resourcepacks` folder.
