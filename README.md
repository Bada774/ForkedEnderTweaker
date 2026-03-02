<style>
#note {
    font-style: italic;
}

#source-variable_code {
    color: coral;
    font-style: italic;
}

#own-variable_code {
    color: mediumseagreen;
    font-weight: bold;
}

#additional-variable_code {
    color: darkgoldenrod;
    font-style: italic;
    font-weight: bold;
}
</style>

<h1 align="center">FET - Forked Ender Tweaker</h1>

<h3 align="center">FET is a fork of the original <a href="https://www.curseforge.com/minecraft/mc-mods/endertweaker">EnderTweaker</a> by Shadows_of_Fire</h3>

<h4 align="center">
An interface between EnderIO and CraftTweaker.<br><br>
It supports adding and removing recipes for EnderIO machines using ZenScript, enabling <b>hot-reloading</b> (via <a href="https://www.curseforge.com/minecraft/mc-mods/zenrecipereload">ZenRecipeReload</a>) for modpack customization.
</h4>

<br>

## ✨ Key Improvements in this Fork

- **The latest version of EnderIO is supported:**
    All supported mechanisms work and have been tested with the latest version of 1.12.2 EnderIO (v.5.3.72).
    _Some mechanisms may also work with older versions of EnderIO (5.3.67 - 5.3.71), but the correct operation is not guaranteed._

- **Hot-Reloading:** Added the ability to use `/ct reload` and `/jeiReload` for hot-reloading your ZenScripts to apply changes without relaunching the game entirely.  
    _(Note: Requires [ZenRecipeReload](https://www.curseforge.com/minecraft/mc-mods/zenrecipereload))._

- **Improved Logging:** Clearer error messages in `crafttweaker.log` and the in-game chat to help you debug your scripts.

<br>

---

<p align="center">
    <b>Supported Machines:</b><br>
    <a href="#alloy">Alloy Smelter</a> &bull;
    <a href="#combustion">Combustion Generator</a> &bull;
    <a href="#enchanter">Enchanter</a> &bull;
    <a href="#sagmill">SAG Mill</a> &bull;
    <a href="#slice">Slice'n'Splice</a> &bull;
    <a href="#soulbinder">Soul Binder</a> &bull;
    <a href="#vat">The Vat</a> &bull;
    <a href="#tank">Tank</a>
</p>

<h2 align="center">📖 Documentation</h2>

<details>
<summary><b>Reloading Feature</b></summary>

#### Hot-Reloading

To enable script reloading, you must include the `#reloadable` preprocessor at the very first line of your ZenScript file.

```zenscript
#reloadable

// ...Your imports here...

// ...Your script here...
```

Once you have made any changes to your script (added or removed recipes), run the `/ct reload` command to reload your scripts and the `/jeiReload` command to refresh JEI recipes.

You can find more useful info about reloading in general on the [ZenUtils wiki](https://github.com/friendlyhj/ZenUtils/wiki/ScriptReloading).

---

</details>

<details>
<summary><b id="alloy">Alloy Smelter</b> (mods.enderio.AlloySmelter)</summary>

The Alloy Smelter is a complex mechanism implementing its own melting system that can smelt up to three items at once and combine them into alloys.

#### Add Recipe

##### `AlloySmelter.addRecipe(IItemStack output, IItemStack[] inputs, @Optional int energyCost, @Optional xp)`

```zenscript
AlloySmelter.addRecipe(
    <minecraft:diamond>,
    [<minecraft:dirt>, <minecraft:sand>],
    2000,
    0.5
);
```

**Parameters:**

- `output` ([IItemStack](https://docs.blamejared.com/1.12/en/Vanilla/Items/IItemStack)) - The result of the crafting operation.
- `inputs` ([IIngredient[]](https://docs.blamejared.com/1.12/en/Vanilla/Variable_Types/IIngredient)) - The items required to smelt the alloy.
- `energyCost` (int, optional) - µI required (Default: 5000).
- `xp` (float, optional) - Experience granted (Default: 0.0).

#### Remove Recipe

_(Note: You cannot remove standard smelting recipes (e.g., `Flour > Bread`) using FET's AlloySmelter class, because those use the vanilla Minecraft Furnace smelting system. For such recipes, use [CraftTweaker's Furnace Handler](https://docs.blamejared.com/1.12/en/Vanilla/Recipes/Furnace/Recipes_Furnace/))._

**Performance Tip:** Because the Alloy Smelter lacks native recipe removal tools, FET rebuilds the entire recipe list during removal. This is an "expensive" operation. If you need to remove **multiple recipes, better to use the multiple removal formats instead** to do it in one pass.

##### 1. Remove by Output

##### `AlloySmelter.removeRecipe(...IItemStack output)`

```
// Remove a single recipe
AlloySmelter.removeRecipe(<enderio:item_alloy_ingot:0>); // Electrical Steel

// Remove multiple recipes (VarArgs format)
AlloySmelter.removeRecipe(
    <enderio:item_alloy_ingot:0>, // Electrical Steel
    <enderio:item_alloy_ingot:1>, // Energetic Alloy
    <enderio:item_alloy_ingot:2>  // Vibrant Alloy
);

// Remove multiple recipes (Array format)
AlloySmelter.removeRecipe([
    <enderio:item_alloy_ingot:0>, // Electrical Steel
    <enderio:item_alloy_ingot:1>  // Energetic Alloy
]);
```

**Parameters:**

- `output` ([IItemStack](https://docs.blamejared.com/1.12/en/Vanilla/Items/IItemStack) or array/varargs) - The result of the recipe(s) you want to remove.

##### 2. Remove by Inputs

_(Note: The `removeByInputs` method requires an array/2d array and does not support VarArgs)._

##### `AlloySmelter.removeByInputs(IItemStack[] inputs)`

##### `AlloySmelter.removeByInputs(IItemStack[][] inputs)`

```
// Remove recipe that use these exact inputs
AlloySmelter.removeByInputs(
    [<minecraft:iron_ingot>,
    <enderio:item_material:23>,
    <enderio:item_material:5>]
); //Electrical Steel Ingot

// Remove multiple recipe configurations at once (Array of Arrays)
AlloySmelter.removeByInputs([
    [<minecraft:soul_sand>, <minecraft:gold_ingot>], //Soularium Ingot
    [<minecraft:iron_ingot>, <minecraft:redstone>] //Conductive Ingot
]);
```

**Parameters:**

- `inputs` ([IIngredient[]](https://docs.blamejared.com/1.12/en/Vanilla/Variable_Types/IIngredient) or IIngredient[][]) - The input items of the recipe(s) you want to remove.

---

</details>

<details>
<summary><b id="combustion">Combustion Generator</b> (mods.enderio.CombustionGen)</summary>

Generates µI (Micro Infinity Energy) by burning liquid fuel and using a liquid coolant for cooling.
_(Note: This machine acts as a mix-and-match system. Once you register a Fuel, it can be used with **ANY** registered Coolant and vice versa._

The GUI displays values in **t/mB** (Ticks per Millibucket). This represents **Duration**).

The logic is based on **Heat Balance**. Burning fuel generates Heat. The Coolant absorbs that Heat.

#### Basic understanding of Combustion Generator Mechanics:

_(Note: Variables marked as <code id="own-variable_code">variable</code> are variables you set in your ZenScript script.  
Variables marked as <code id="source-variable_code">variable</code> are derived from EnderIO's source code)._

<h5 id="combustion_multipliers">1. Capacitor & Machine Modifiers</h5>

Machine and capacitor tier multipliers can be configured however you want via EnderIO XML recipe configs. All of them have a base value and a scaler type (which declares what the math will do with the base value). For the Combustion Generator, we generally have the following final multipliers:

- **Capacitor Quality Multiplier** (<code id="source-variable_code">capQuality</code>):

    The basic config declares for Normal and Enhanced Combustion Gens:

    `<capacitor key="enderio:block_combustion_generator/gen" base="1" scaler="CHEMICAL" />`

    `<capacitor key="enderio:block_enhanced_combustion_generator/gen" base="1" scaler="CHEMICAL" />`

    **Formula**: 1.0 + (<code>CapacitorLevel</code> - 1) \* 0.25

    - Basic Capacitor (Lvl 1): **1.00**
    - Double-Layer Capacitor (Lvl 2): **1.25**
    - Octadic Capacitor (Lvl 3): **1.50**

    _(Note: For custom capacitors or those from add-ons, you need to use the same scaler and their respective level values)._

- **Machine Quality** (<code id="source-variable_code">machineQuality</code>):

    Basic config declares:

    `<capacitor key="enderio:block_combustion_generator/gen" base="1" scaler="CHEMICAL" />`

    `<capacitor key="enderio:block_enhanced_combustion_generator/gen" base="200" scaler="CHEMICAL" />`
    </i>

    - Combustion Generator: **1.00**
    - Enhanced Combustion Generator: `200 * 0.01` = **2.0**

<h5 id="energy_per_tick">2. Energy Generation (µI/t)</h5>

**Formula:** <code id="source-variable_code">energyPerTick</code> = Math.round(<code id="own-variable_code">powerPerCycle</code> \* <code id="source-variable_code">capQuality</code> \* <code id="source-variable_code">machineQuality</code>)

<h5>3. Fuel Duration (t/mB)</h5>

Determines how many ticks 1 mB of fuel lasts. Higher capacitor tiers burn fuel faster to maintain higher power output.

<ul id="ticks_per_fuel">
<li><code id="own-variable_code">totalBurnTime</code> = Total ticks per 1000 mB (set in script).</li>
<li><b>Formula:</b> <code id="source-variable_code">ticksPerFuel</code> = <code>Math.max((int)(<code id="own-variable_code">totalBurnTime</code> / <code id="source-variable_code">capQuality</code> / 1000.0F), 1)</code></li>
</ul>

<h5 id="ticks_per_coolant">4. Coolant Duration (t/mB)</h5>

The machine produces heat based on energy generation.

- **A. Cooling Capacity (per mB):**

    How much heat 1 mB of fluid can remove.

    <code><code id="source-variable_code">cooling</code> = (373.25 - <code id="additional-variable_code">Fluid_Temp_K</code>) \* <code id="own-variable_code">degreesCoolingPerMB</code></code>

    <code id="additional-variable_code">Fluid_Temp_K</code> = the liquid's temperature, which can be found by running the `/ct liquids`command. All registered liquids and their properties will be written to the `crafttweaker.log` file.

- **B. Heat Generated (per Tick):**

    Heat per µI Constant: **0.000115**.

    <code><code id="source-variable_code">toCool</code> = <code id="source-variable_code">energyPerTick</code> \* <code id="source-variable_code">machineQuality</code> \* 0.000115</code>

- **C. Final Duration (t/mB):**

    <code><code id="source-variable_code">ticksPerCoolant</code> = Math.max((int)Math.round(<code id="source-variable_code">cooling</code> / <code id="source-variable_code">toCool</code>), 1)</code>

#### Example Calculation (What GUI will show)

<pre><code>// Scenario: You're adding fuel and coolant. For example lets took Hot Coolant and Coolant from IC2 mod:
// Fuel: "Hot Coolant" (Temperature = 1200K), powerPerCycleRF = 100 µI/t, totalBurnTime = 20000.
// Coolant: "Coolant" (Temperature = 300K), degreesCoolingPerMB = 1.0.


//Normal Machine (MQ=1.0) + Basic Capacitor (CQ=1.0)

1. <a href="#energy_per_tick">Energy</a> = 1000 \* 1.0 \* 1.0 = 1000.0 µI/t.
2. <a href="#ticks_per_fuel">Ticks per Fuel</a> = 20000 / 1.0 / 1000 = 20.
3. <a href="#ticks_per_coolant">Ticks per Coolant</a> = ((373.25 - 300K) \* 1.0) / (1000.0µI/t \* 1.0 \* 0.000115) = 73.25 / 0.115 = 637 t/mB.

// Case B: Enhanced Machine (MQ=2.0) + Octadic Capacitor (CQ=1.5)

1. <a href="#energy_per_tick">Energy</a> = 1000 \* 1.5 \* 2.0 = 3000 µI/t.
2. <a href="#ticks_per_fuel">Ticks per Fuel</a> = 300 \* 2.0 \* 0.000115 = 13.
3. <a href="#ticks_per_coolant">Ticks per Coolant</a> = ((373.25 - 300K) \* 1.0) / (1000.0µI/t \* 2.0 \* 0.000115) = 73.25 / 0.69 = 106 t/mB.</code></pre>

#### Add Fuel

##### `CombustionGen.addFuel(ILiquidStack fluid, int powerPerCycle, int totalBurnTime)`

```zenscript
import mods.enderio.CombustionGen;

CombustionGen.addFuel(
    <liquid:hooch>,
    100,
    20000
    );
```

**Parameters:**

- `fluid` ([ILiquidStack](https://docs.blamejared.com/1.12/en/Vanilla/Liquids/ILiquidStack)) - The liquid to burn.
- `powerPerCycle` (int) - Base µI/t (at Basic Capacitor, Normal Machine).
- `totalBurnTime` (int) - Total ticks that 1000 mB (1 Bucket) will burn (at Basic Capacitor).

#### Add Coolant

##### `CombustionGen.addCoolant(ILiquidStack fluid, int powerPerCycle, float degreesCoolingPerMB)`

```zenscript
import mods.enderio.CombustionGen;

CombustionGen.addCoolant(<liquid:xpjuice>, 1.0);
```

**Parameters:**

- `fluid` ([ILiquidStack](https://docs.blamejared.com/1.12/en/Vanilla/Liquids/ILiquidStack)) - The liquid to burn.
- `degreesCoolingPerMB` (int) - Multiplier.

#### Remove Fluids

##### `CombustionGen.removeFuel(ILiquidStack fluid)`

##### `CombustionGen.removeCoolant(ILiquidStack fluid)`

```zenscript
import mods.enderio.CombustionGen;

CombustionGen.removeFuel(<liquid:fire_water>);
CombustionGen.removeCoolant(<liquid:water>);
```

**Parameters:**

- `fluid` ([ILiquidStack](https://docs.blamejared.com/1.12/en/Vanilla/Liquids/ILiquidStack)) - The liquid to remove.

---

</details>

<details>
<summary><b id="enchanter">Enchanter</b> (mods.enderio.Enchanter)</summary>

The Enchanter allows creating Enchanted Books by combining a specific input item with XP levels. It converts physical items and player experience into stored enchantments.

#### Basic understanding of Enchanter Mechanics:

- **The Lapis is Implicit:**
    You **do not** need to specify a Lapis as part of your `input`. The machine is hardcoded to always require one in its secondary slot.

Recipes in the Enchanter are defined per Enchantment. The cost of creating a book is dynamic and scales linearly with the Level of the enchantment you select. It is based on the default vanilla XP enchantability and a few scalers.

_(Note: Variables marked as <code id="own-variable_code">variable</code> are variables you set in your ZenScript script.  
Variables marked as <code id="source-variable_code">variable</code> are derived from EnderIO's source code)._

- **Item Cost:**

    A simple calculation: it multiplies the required amount of items per level by the target level.

    <code><code>itemsNeeded</code> = <code>targetLevel</code> \* <code id="own-variable_code">amountPerLevel</code></code>
    Example: If <code id="own-variable_code">amountPerLevel</code> is 2, crafting a Level V (5) book requires 10 items.

    _(Note: Be careful with the amount of items. If it exceeds the maximum stack size of the required item (e.g., 64 for most resources), higher levels of the enchantment will become uncraftable, even if the maximum possible level for this enchantment is not reached. However, this can also be used as a deliberate limitation.)_

- **XP Cost:**

    Depends on the enchantment's native rarity, the <code id="own-variable_code">costMultiplier</code>, and the config's <code id="source-variable_code">levelCostFactor</code> and <code id="source-variable_code">baseLevelCost</code> (Below is pseudocode for easier understanding):

    <pre>min = Math.max(1, <code id="additional-variable_code">minEnchantability</code>);<br>min = min * <code id="own-variable_code">costMultiplier</code>;<br>cost = (int)Math.round(min * <code id="source-variable_code">levelCostFactor</code>);<br>cost = cost + (int)<code id="source-variable_code">baseLevelCost</code>;<br>return cost;</pre>

    _(Note 1: <code id="additional-variable_code">minEnchantability</code> can be found by procceding CraftTweaker's <a href="https://docs.blamejared.com/1.12/en/Vanilla/Enchantments/IEnchantmentDefinition/#getenchantability"><code>getMinEnchantability(int level)</code></a> function)._

    _(Note 2: A  <code id="own-variable_code">costMultiplier</code> multiplier of 1.0 uses standard EnderIO math. Higher values make the recipe more expensive in levels.  
    By default <code id="source-variable_code">levelCostFactor</code> = 0.75 and <code id="source-variable_code">baseLevelCost</code> = 2.0)._

#### Example Calculation

```
// Scenario: Making Sharpness V book from Wool.
// Recipe: Input=Red Wool, AmountPerLevel=2, CostMultiplier=1.0.

1. Items Needed = 5 \* 2 = 10 Wool.
2. XP Cost:
    min = Sharpness V Ench. (45) \* 1.0 = 45.0
    cost = 45.0 \* 0.75 = 34
    cost = 34 + 2.0 = 36
```

#### Add Recipe

##### `Enchanter.addRecipe(IEnchantmentDefinition enchantment, IIngredient input, int amountPerLevel, double costMultiplier)`

```zenscript
import mods.enderio.Enchanter;

Enchanter.addRecipe(
    <enchantment:minecraft:sharpness>,
    <minecraft:wool:14>,
    2,
    1.0);
```

**Parameters:**

- `enchantment` ([IEnchantmentDefinition](https://docs.blamejared.com/1.12/en/Vanilla/Enchantments/IEnchantmentDefinition)) - The enchantment definition (e.g. `<enchantment:minecraft:sharpness>`).
- `input` ([IIngredient](https://docs.blamejared.com/1.12/en/Vanilla/Variable_Types/IIngredient)) - The item used as a catalyst.
- `amountPerLevel` (int) - How many items are required for Level 1.
- `costMultiplier` (double) - Multiplier for the XP cost calculation.

#### Remove Recipe

##### `Enchanter.removeRecipe(IEnchantmentDefinition enchantment)`

```
import mods.enderio.Enchanter;

// Remove the recipe for Protection
Enchanter.removeRecipe(<enchantment:minecraft:protection>);
```

**Parameters:**

- `enchantment` ([IEnchantmentDefinition](https://docs.blamejared.com/1.12/en/Vanilla/Enchantments/IEnchantmentDefinition)) - The enchantment to remove from the machine.

---

</details>

<details>
<summary><b id="sagmill">SAG Mill</b> (mods.enderio.SagMill)</summary>

Crushes items to produce dusts and byproducts. This machine is heavily affected by **Grinding Balls**.

- **Bonus Type Behavior:**

    How Grinding Balls affect this specific recipe (defined by <code id="own-variable_code">bonusType</code>):
    - `NONE` - Grinding balls have no effect.
    - `MULTIPLY_OUTPUT` - Ball multiplies the amount of the main output.
    - `CHANCE_ONLY` - Ball only boosts the chance of secondary/tertiary output.

#### Add Recipe

##### 1. Using Arrays (Standard)

`SagMill.addRecipe(IItemStack[] outputs, float[] chances, IIngredient input, String bonusType, @Optional int energyCost, @Optional float[] xp);`

```zenscript
import mods.enderio.SagMill;

SagMill.addRecipe(
    [<minecraft:flint>, <minecraft:gravel>],
    [1.0, 0.2],
    <minecraft:cobblestone>,
    "CHANCE_ONLY",
    2000,
    [0.0, 0.1]
);
```

**Parameters:**

- `outputs` ([IItemStack[]](https://docs.blamejared.com/1.12/en/Vanilla/Items/IItemStack)) - Array of result items.
- `chances` (float[]) - Array of probabilities (0.0 to 1.0). Must match `outputs` length.
- `input` ([IIngredient](https://docs.blamejared.com/1.12/en/Vanilla/Items/IIngredient)) - The item to be crushed.
- `bonusType` (String) - See `"Bonus Type Behavior"` above.
- `energyCost` (int, optional) - µI required. Default: 5000.
- `xp` (float[], optional) - XP per item output. Default: 0.

##### 2. Using WeightedStacks

`SagMill.addRecipe(WeightedItemStack[] outputs, IIngredient input, String bonusType, @Optional int energyCost, @Optional float[] xp);`

```zenscript
import mods.enderio.SagMill;

SagMill.addRecipe(
    [<minecraft:diamond> % 100, <minecraft:coal> % 50],
    <minecraft:diamond_ore>,
    "MULTIPLY_OUTPUT"
);
```

**Parameters:**

- `outputs` ([WeightedItemStack[]](https://docs.blamejared.com/1.12/en/Vanilla/Items/WeightedItemStack)) - Item + Chance pairs (e.g. `<item> % 50` for 50%).
- `input`, `bonusType`, `energy`, `xp` - Same as above.

#### Remove Recipe

`SagMill.removeRecipe(IItemStack input);`

```zenscript
import mods.enderio.SagMill;

SagMill.removeRecipe(<minecraft:cobblestone>);
```

**Parameters:**

- `input` ([IItemStack]((https://docs.blamejared.com/1.12/en/Vanilla/Items/IItemStack)) - The grinding item to remove.

---

</details>

<details>
<summary><b id="slice">Slice'n'Splice</b> (mods.enderio.SliceNSplice)</summary>

The Slice'n'Splice is a machine that combines various items into advanced components like Zombie Electrodes or Z-Logic Controllers. It uniquely requires an Axe and Shears in its tool slots to operate (you don't need to specify them).

**Important Note:** This machine is **slot-strict**. The order of the ingredients in your array directly corresponds to the 6 input slots in the machine. If you need to leave a specific slot empty, you must use `null` in that position of the array. If you want to remain empty trailing slots, you can simply provide a shorter array (e.g., an array of 4 items will fill the first 4 slots and leave the last 2 empty).

#### Add Recipe

##### SliceNSplice.addRecipe(IItemStack output, IIngredient[] inputs, @Optional int energyCost, @Optional float xp)

```zenscript
import mods.enderio.SliceNSplice;

SliceNSplice.addRecipe(
    <minecraft:diamond>, 
    [
        <minecraft:stick>, null, <minecraft:stick>, // Slots 0, 1, 2
        null, <minecraft:iron_ingot>, null          // Slots 3, 4, 5
    ], 
    5000,
    0.5
);
```

**Parameters:**

- `output` ([IItemStack](https://docs.blamejared.com/1.12/en/Vanilla/Items/IItemStack)) - The result of the crafting operation.
- `inputs` ([IIngredient[]](https://docs.blamejared.com/1.12/en/Vanilla/Variable_Types/IIngredient)) - An array of up to 6 ingredients representing the machine's slots in order. Use null for empty slots.
- `energyCost` (int, optional) - µI required (Default: 5000).
- `xp` (float, optional) - Experience granted (Default: 0.0).

#### Remove Recipe

##### SliceNSplice.removeRecipe(IItemStack output)

```zenscript
import mods.enderio.SliceNSplice;

SliceNSplice.removeRecipe(
    <enderio:item_material:41> // Z-Logic Controller
);
```

**Parameters:**

- `output` ([IItemStack](https://docs.blamejared.com/1.12/en/Vanilla/Items/IItemStack)) - The result of the recipe you want to remove.

---

</details>

<details>
<summary><b id="soulbinder">Soul Binder</b> (mods.enderio.SoulBinder)</summary>

The Soul Binder is used to bind the soul of a captured mob (from a filled Soul Vial) to a base item, creating a new magical or mechanical component. It consumes both µI (Energy) and player Experience (Levels).

#### Basic understanding of Soul Binder Mechanics:

- **The Soul Vial is Implicit:**
    You **do not** need to specify a filled Soul Vial as part of your `input`. The machine is hardcoded to always require one in its secondary slot.

- **Entity Array:**
    Instead of defining the exact vial item, you provide an array of `Entity IDs` (e.g., `["minecraft:zombie", "minecraft:skeleton"]`). The recipe will accept a Soul Vial filled with **ANY** of the entities listed in this array.

- **Empty Vial Output:**
    The machine automatically returns an empty Soul Vial upon completing the craft. You do not need to script this byproduct.

#### Add Recipe

##### SoulBinder.addRecipe(IItemStack output, IIngredient input, String[] entities, int xp, @Optional int energyCost)

```zenscript
import mods.enderio.SoulBinder;

// Example: Binds either a Zombie or Skeleton soul to a Diamond to create an Emerald.
// Costs 5 XP levels and 100,000 µI.
SoulBinder.addRecipe(
    <minecraft:emerald>,
    <minecraft:diamond>,
    ["minecraft:zombie", "minecraft:skeleton"],
    5,
    100000
);
```

**Parameters:**

- `output` ([IItemStack](https://docs.blamejared.com/1.12/en/Vanilla/Items/IItemStack)) - The result of the binding operation.
- `input` ([IIngredient[]](https://docs.blamejared.com/1.12/en/Vanilla/Variable_Types/IIngredient)) - The base item that the soul will be bound to.
- `entities` (String[]) - An array of valid entity Resource Locations (e.g., `"minecraft:creeper"`).
- `xp` (int) - The cost in experience **levels**.
- `energyCost` (int, optional) - µI required (Default: 5000).

#### Remove Recipe

##### SoulBinder.removeRecipe(IItemStack output)

```zenscript
import mods.enderio.SoulBinder;

SoulBinder.removeRecipe(
    <enderio:item_material:17> // Enticing Crystal
);
```

**Parameters:**

- `output` ([IItemStack](https://docs.blamejared.com/1.12/en/Vanilla/Items/IItemStack)) - The result of the recipe you want to remove.

---

</details>

<details>
<summary><b id="vat">The Vat</b> (mods.enderio.Vat)</summary>

The Vat is a fluid brewing machine. It ferments a base input fluid along with two solid ingredients to create a new output fluid (e.g., `Water + Sugar + Apple = Hootch`).

#### Basic understanding of Soul Binder Mechanics:

- **Input Slots:**
    The Vat has two distinct item input slots. A recipe must define an ingredient for both slots.

- **Slot Multipliers:**
    Each solid input item and input liquid has a specific multiplier. The final amount of both fluids is calculated by the following formulas:

    <code><code id="alternative-variable_code">Ingredient Multiplier</code> = <code id="own-variable_code">slot1Mults</code> * <code id="own-variable_code">slot2Mults</code></code>

    <code><code id="source-variable_code">Input Fluid Volume</code> = <code id="source-variable_code">Ingredient Multiplier</code> *  1000</code>

    <code><code id="source-variable_code">Output Fluid Volume</code> = <code id="alternative-variable_code">Ingredient Multiplier</code> * <code id="own-variable_code">inMult</code> *  1000</code>

<pre><code>// Scenario: You're adding a recipe for brewing lava from water. As items you're using Coal + Flint and Steel:

// Input liquid: "Water"; Input Fluid Multiplier = 0.5.
// Left slot items: "Coal", "Charcoal"; Multipliers: [2.0, 1.5];
// Right slot items: "Flint and Steel"; Multipliers: [2.0].


// Coal (2.0) + Flint and Steel (2.0)

1. Ingredient Multiplier = 2.0 \* 2.0 = 4.0
2. Input Fluid Volume = 4.0 \* 1000 = 4000 mB.
3. Output Fluid Volume = 4.0 \* 0.5 \* 1000  = 2000 mB.

// Charcoal (1.5) + Flint and Steel (2.0)

1. Ingredient Multiplier = 1.5 \* 2.0 = 3.0
2. Input Fluid Volume = 3.0 \* 1000 = 3000 mB.
3. Output Fluid Volume = 3.0 \* 0.5 \* 1000  = 1500 mB.</code></pre>

_(Note: Be careful with multipliers - if the final volume of output liquid exceeds the max capacity of the tank, you will not be able to create this recipe.)._

#### Add Recipe

##### Vat.addRecipe(ILiquidStack output, float inMult, ILiquidStack input, IIngredient[] slot1Solids, float[] slot1Mults, IIngredient[] slot2Solids, float[] slot2Mults, @Optional int energyCost)

```zenscript
import mods.enderio.Vat;

Vat.addRecipe(
    <liquid:lava>,
    0.5,
    <liquid:water>, 
    [<minecraft:coal>, <minecraft:coal:1>], [2.0, 1.5],
    [<minecraft:flint_and_steel>], [2.0],
    40000
);
```

**Parameters:**

- `output` ([ILiquidStack](https://docs.blamejared.com/1.12/en/Vanilla/Liquids/ILiquidStack)) - The resulting fluid.
- `inMult` (float) - The multiplier for input fluid.
- `input` ([ILiquidStack](https://docs.blamejared.com/1.12/en/Vanilla/Liquids/ILiquidStack)) - The input fluid.
- `slot1Solids` ([IIngredient[]](https://docs.blamejared.com/1.12/en/Vanilla/Variable_Types/IIngredient)) - The items for the first slot.
- `slot1Mults` (float[]) - Multipliers for first slot items.
- `slot2Solids` ([IIngredient[]](https://docs.blamejared.com/1.12/en/Vanilla/Variable_Types/IIngredient)) - The items for the second slot.
- `slot2Mults` (float[]) - Multipliers for second slot items.
- `energyCost` (int, optional) - µI required (Default: 5000).

#### Remove Recipe

##### SoulBinder.removeRecipe(IItemStack output)

```zenscript
import mods.enderio.Vat;

// Remove the recipe that outputs Hootch
Vat.removeRecipe(<liquid:hootch>);
```

**Parameters:**

- `output` ([ILiquidStack](https://docs.blamejared.com/1.12/en/Vanilla/Liquids/ILiquidStack)) - The result of the recipe you want to remove.

---

</details>

<details>
<summary><b id="tank">The Tank</b> (mods.enderio.Tank)</summary>

The Fluid Tank is not only for storage but also functions as a simple processing machine. It can either consume fluids from the tank to modify an item (Filling the item) or extract fluids from an item to fill the tank (Emptying the item).

#### Basic understanding of Tank Mechanics:

- **The <code id="own-variable_code">fill</code> boolean:**
    This parameter dictates the direction of the fluid transfer:
    - `true` **(Filling Mode)**: The fluid is consumed from the tank's internal storage to process or fill the input item.
    - `false` **(Emptying Mode)**: The fluid is extracted from the input item and added to the tank's internal storage.

- **Optional Output (Item Consumption):**
    The `output` item parameter is optional. If you do not provide an output (or pass `null`), the input item will be completely consumed/destroyed during the process.

- **Native Forge Containers Restriction:**
    You **cannot** add or remove recipes that use native Forge fluid containers (such as Vanilla Buckets, IC2 Capsules, or Portable Tanks). The EnderIO Tank handles these items automatically via hardcoded Forge mechanics.

#### Add Recipe

##### Tank.addRecipe(boolean fill, IIngredient input, ILiquidStack fluid, @Optional IItemStack output)

```zenscript
import mods.enderio.Tank;

// Example 1 (Filling Mode): Consumes 250mB of Lava from the tank to turn a Sponge into a Wet Sponge.
Tank.addRecipe(true, <minecraft:sponge>, <liquid:lava> * 250, <minecraft:sponge:1>);

// Example 2 (Emptying Mode, No Output): Melts a Diamond to add 100mB of Lava to the tank. The Diamond is completely consumed.
Tank.addRecipe(false, <minecraft:diamond>, <liquid:lava> * 100);
```

**Parameters:**

- `fill` (boolean) - `true` to consume fluid from the tank (Filling Mode), `false` to generate fluid into the tank (Emptying Mode).
- `input` ([IIngredient](https://docs.blamejared.com/1.12/en/Vanilla/Variable_Types/IIngredient)) - The item inserted into the tank's slot.
- `fluid` ([ILiquidStack](https://docs.blamejared.com/1.12/en/Vanilla/Liquids/ILiquidStack")) - The fluid required (if fill is true) or produced (if fill is false).
- `output` ([IItemStack](https://docs.blamejared.com/1.12/en/Vanilla/Items/IItemStack), optional) - The resulting item after the process. Leave empty to consume the item.

#### Remove Recipe

_Note: Recipe removal is based strictly on the **input item** and the tank's mode, not the output. If you omit the fluid parameter, all recipes for that input item in the specified mode will be removed._

##### Tank.removeRecipe(boolean fill, IItemStack input, @Optional ILiquidStack fluid)

```
import mods.enderio.Tank;

// Example 1: Remove the specific EnderIO recipe that empties a Wet Sponge to get Water.
Tank.removeRecipe(false, <minecraft:sponge:1>, <liquid:water>);

// Example 2: Remove ALL Emptying recipes associated with the Experience Bottle (ignoring which fluid it produces).
Tank.removeRecipe(false, <minecraft:experience_bottle>);
```

**Parameters:**

- `fill` (boolean) - `true` to consume fluid from the tank (Filling Mode), `false` to generate fluid into the tank (Emptying Mode).
- `input` ([IIngredient](https://docs.blamejared.com/1.12/en/Vanilla/Variable_Types/IIngredient)) - the **input item** of the recipe you want to remove.
- `fluid` ([ILiquidStack](https://docs.blamejared.com/1.12/en/Vanilla/Liquids/ILiquidStack"), optional) - The specific fluid to match. If omitted, removes all recipes for the given input.

---

</details>