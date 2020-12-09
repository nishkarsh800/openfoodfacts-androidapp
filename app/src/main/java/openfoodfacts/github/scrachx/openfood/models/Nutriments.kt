package openfoodfacts.github.scrachx.openfood.models

import android.util.Log
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonInclude
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.utils.DEFAULT_MODIFIER
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.convertFromGram
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.convertToGrams
import openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber
import openfoodfacts.github.scrachx.openfood.utils.getModifierNonDefault
import org.apache.commons.lang.StringUtils
import org.jetbrains.annotations.Contract
import java.io.Serializable
import java.util.*

/**
 * JSON representation of the product nutriments entry
 *
 * @see [JSON Structure](http://en.wiki.openfoodfacts.org/API.JSON_interface)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class Nutriments : Serializable {
    companion object {
        private const val serialVersionUID = 1L
        const val DEFAULT_UNIT = "g"
        const val ENERGY_KCAL = "energy-kcal"
        const val ENERGY_KJ = "energy-kj"
        const val ENERGY_FROM_FAT = "energy-from-fat"
        const val FAT = "fat"
        const val SATURATED_FAT = "saturated-fat"
        const val BUTYRIC_ACID = "butyric-acid"
        const val CAPROIC_ACID = "caproic-acid"
        const val CAPRYLIC_ACID = "caprylic-acid"
        const val CAPRIC_ACID = "capric-acid"
        const val LAURIC_ACID = "lauric-acid"
        const val MYRISTIC_ACID = "myristic-acid"
        const val PALMITIC_ACID = "palmitic-acid"
        const val STEARIC_ACID = "stearic-acid"
        const val ARACHIDIC_ACID = "arachidic-acid"
        const val BEHENIC_ACID = "behenic-acid"
        const val LIGNOCERIC_ACID = "lignoceric-acid"
        const val CEROTIC_ACID = "cerotic-acid"
        const val MONTANIC_ACID = "montanic-acid"
        const val MELISSIC_ACID = "melissic-acid"
        const val MONOUNSATURATED_FAT = "monounsaturated-fat"
        const val POLYUNSATURATED_FAT = "polyunsaturated-fat"
        const val OMEGA_3_FAT = "omega-3-fat"
        const val ALPHA_LINOLENIC_ACID = "alpha-linolenic-acid"
        const val EICOSAPENTAENOIC_ACID = "eicosapentaenoic-acid"
        const val DOCOSAHEXAENOIC_ACID = "docosahexaenoic-acid"
        const val OMEGA_6_FAT = "omega-6-fat"
        const val LINOLEIC_ACID = "linoleic-acid"
        const val ARACHIDONIC_ACID = "arachidonic-acid"
        const val GAMMA_LINOLENIC_ACID = "gamma-linolenic-acid"
        const val DIHOMO_GAMMA_LINOLENIC_ACID = "dihomo-gamma-linolenic-acid"
        const val OMEGA_9_FAT = "omega-9-fat"
        const val OLEIC_ACID = "oleic-acid"
        const val ELAIDIC_ACID = "elaidic-acid"
        const val GONDOIC_ACID = "gondoic-acid"
        const val MEAD_ACID = "mead-acid"
        const val ERUCIC_ACID = "erucic-acid"
        const val NERVONIC_ACID = "nervonic-acid"
        const val TRANS_FAT = "trans-fat"
        const val CHOLESTEROL = "cholesterol"
        const val CARBOHYDRATES = "carbohydrates"
        const val SUGARS = "sugars"
        const val SUCROSE = "sucrose"
        const val GLUCOSE = "glucose"
        const val FRUCTOSE = "fructose"
        const val LACTOSE = "lactose"
        const val MALTOSE = "maltose"
        const val MALTODEXTRINS = "maltodextrins"
        const val STARCH = "starch"
        const val POLYOLS = "polyols"
        const val FIBER = "fiber"
        const val PROTEINS = "proteins"
        const val CASEIN = "casein"
        const val SERUM_PROTEINS = "serum-proteins"
        const val NUCLEOTIDES = "nucleotides"
        const val SALT = "salt"
        const val SODIUM = "sodium"
        const val ALCOHOL = "alcohol"
        const val VITAMIN_A = "vitamin-a"
        const val BETA_CAROTENE = "beta-carotene"
        const val VITAMIN_D = "vitamin-d"
        const val VITAMIN_E = "vitamin-e"
        const val VITAMIN_K = "vitamin-k"
        const val VITAMIN_C = "vitamin-c"
        const val VITAMIN_B1 = "vitamin-b1"
        const val VITAMIN_B2 = "vitamin-b2"
        const val VITAMIN_PP = "vitamin-pp"
        const val VITAMIN_B6 = "vitamin-b6"
        const val VITAMIN_B9 = "vitamin-b9"
        const val WATER_HARDNESS = "water-hardness"
        const val GLYCEMIC_INDEX = "glycemic-index"
        const val NUTRITION_SCORE_UK = "nutrition-score-uk"
        const val NUTRITION_SCORE_FR = "nutrition-score-fr"
        const val CARBON_FOOTPRINT = "carbon-footprint"
        const val CHLOROPHYL = "chlorophyl"
        const val COCOA = "cocoa"
        const val COLLAGEN_MEAT_PROTEIN_RATIO = "collagen-meat-protein-ratio"
        const val FRUITS_VEGETABLES_NUTS = "fruits-vegetables-nuts"
        const val PH = "ph"
        const val TAURINE = "taurine"
        const val CAFFEINE = "caffeine"
        const val IODINE = "iodine"
        const val MOLYBDENUM = "molybdenum"
        const val CHROMIUM = "chromium"
        const val SELENIUM = "selenium"
        const val FLUORIDE = "fluoride"
        const val MANGANESE = "manganese"
        const val COPPER = "copper"
        const val ZINC = "zinc"
        const val VITAMIN_B12 = "vitamin-b12"
        const val BIOTIN = "biotin"
        const val PANTOTHENIC_ACID = "pantothenic-acid"
        const val SILICA = "silica"
        const val BICARBONATE = "bicarbonate"
        const val POTASSIUM = "potassium"
        const val CHLORIDE = "chloride"
        const val CALCIUM = "calcium"
        const val PHOSPHORUS = "phosphorus"
        const val IRON = "iron"
        const val MAGNESIUM = "magnesium"

        @JvmField
        val MINERALS_MAP: MutableMap<String, Int> = hashMapOf(
                SILICA to R.string.silica,
                BICARBONATE to R.string.bicarbonate,
                POTASSIUM to R.string.potassium,
                CHLORIDE to R.string.chloride,
                CALCIUM to R.string.calcium,
                CALCIUM to R.string.calcium,
                PHOSPHORUS to R.string.phosphorus,
                IRON to R.string.iron,
                MAGNESIUM to R.string.magnesium,
                ZINC to R.string.zinc,
                COPPER to R.string.copper,
                MANGANESE to R.string.manganese,
                FLUORIDE to R.string.fluoride,
                SELENIUM to R.string.selenium,
                CHROMIUM to R.string.chromium,
                MOLYBDENUM to R.string.molybdenum,
                IODINE to R.string.iodine,
                CAFFEINE to R.string.caffeine,
                TAURINE to R.string.taurine,
                PH to R.string.ph,
                FRUITS_VEGETABLES_NUTS to R.string.fruits_vegetables_nuts,
                COLLAGEN_MEAT_PROTEIN_RATIO to R.string.collagen_meat_protein_ratio,
                COCOA to R.string.cocoa,
                CHLOROPHYL to R.string.chlorophyl

        )

        @JvmField
        val FAT_MAP: MutableMap<String, Int> = hashMapOf(
                SATURATED_FAT to R.string.nutrition_satured_fat,
                MONOUNSATURATED_FAT to R.string.nutrition_monounsaturatedFat,
                POLYUNSATURATED_FAT to R.string.nutrition_polyunsaturatedFat,
                OMEGA_3_FAT to R.string.nutrition_omega3,
                OMEGA_6_FAT to R.string.nutrition_omega6,
                OMEGA_9_FAT to R.string.nutrition_omega9,
                TRANS_FAT to R.string.nutrition_trans_fat,
                CHOLESTEROL to R.string.nutrition_cholesterol
        )

        @JvmField
        val CARBO_MAP: MutableMap<String, Int> = hashMapOf(
                SUGARS to R.string.nutrition_sugars,
                SUCROSE to R.string.nutrition_sucrose,
                GLUCOSE to R.string.nutrition_glucose,
                FRUCTOSE to R.string.nutrition_fructose,
                LACTOSE to R.string.nutrition_lactose,
                MALTOSE to R.string.nutrition_maltose,
                MALTODEXTRINS to R.string.nutrition_maltodextrins
        )

        @JvmField
        val PROT_MAP: MutableMap<String, Int> = hashMapOf(
                CASEIN to R.string.nutrition_casein,
                SERUM_PROTEINS to R.string.nutrition_serum_proteins,
                NUCLEOTIDES to R.string.nutrition_nucleotides
        )

        @JvmField
        val VITAMINS_MAP: MutableMap<String, Int> = hashMapOf(
                VITAMIN_A to R.string.vitamin_a,
                BETA_CAROTENE to R.string.vitamin_a,
                VITAMIN_D to R.string.vitamin_d,
                VITAMIN_E to R.string.vitamin_e,
                VITAMIN_K to R.string.vitamin_k,
                VITAMIN_C to R.string.vitamin_c,
                VITAMIN_B1 to R.string.vitamin_b1,
                VITAMIN_B2 to R.string.vitamin_b2,
                VITAMIN_PP to R.string.vitamin_pp,
                VITAMIN_B6 to R.string.vitamin_b6,
                VITAMIN_B9 to R.string.vitamin_b9,
                VITAMIN_B12 to R.string.vitamin_b12,
                BIOTIN to R.string.biotin,
                PANTOTHENIC_ACID to R.string.pantothenic_acid
        )

    }

    private val additionalProperties: MutableMap<String, Any?> = HashMap()
    private var containsMinerals = false
    private var containsVitamins = false
    fun getEnergyKjValue(isDataPerServing: Boolean): String {
        return if (isDataPerServing) {
            getServing(ENERGY_KJ)
        } else {
            get100g(ENERGY_KJ)
        }
    }

    fun getEnergyKcalValue(isDataPerServing: Boolean): String {
        return if (isDataPerServing) {
            getServing(ENERGY_KCAL)
        } else {
            get100g(ENERGY_KCAL)
        }
    }

    operator fun get(nutrimentName: String): Nutriment? {
        return if (nutrimentName.isEmpty() || additionalProperties[nutrimentName] == null) {
            null
        } else try {
            Nutriment(nutrimentName,
                    additionalProperties[nutrimentName].toString(),
                    get100g(nutrimentName),
                    getServing(nutrimentName),
                    getUnit(nutrimentName),
                    getModifier(nutrimentName))
        } catch (e: NullPointerException) {
            // In case one of the getters was unable to get data as string
            val stacktrace = Log.getStackTraceString(e)
            Log.e("NUTRIMENTS-MODEL", stacktrace)
            null
        }
    }

    /**
     * @return [StringUtils.EMPTY] if there is no serving value for the specified nutriment
     */
    fun getServing(nutrimentName: String): String {
        return getAdditionalProperty(nutrimentName, ApiFields.Suffix.SERVING)
    }

    /**
     * @return [StringUtils.EMPTY] if there is no serving value for the specified nutriment
     */
    fun get100g(nutrimentName: String): String {
        return getAdditionalProperty(nutrimentName, ApiFields.Suffix.VALUE_100G)
    }

    fun getUnit(nutrimentName: String): String {
        return getAdditionalProperty(nutrimentName, ApiFields.Suffix.UNIT, DEFAULT_UNIT)
    }

    fun getModifier(nutrimentName: String): String {
        return getAdditionalProperty(nutrimentName, ApiFields.Suffix.MODIFIER, DEFAULT_MODIFIER)
    }

    /**
     * Get the nutriment modifier if it is different from [DEFAULT_MODIFIER]
     *
     * @return The nutriment modifier if different from [DEFAULT_MODIFIER], otherwise an empty string `""`
     */
    fun getModifierIfNotDefault(nutrimentName: String): String {
        val modifier = getModifier(nutrimentName)
        return getModifierNonDefault(modifier)
    }
    

    private fun getAdditionalProperty(nutrimentName: String, suffix: String, defaultValue: String = StringUtils.EMPTY) =
            additionalProperties[nutrimentName + suffix]?.toString() ?: defaultValue

    operator fun contains(nutrimentName: String) = additionalProperties.containsKey(nutrimentName)

    fun hasVitamins() = containsVitamins

    fun hasMinerals() = containsMinerals

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any?> {
        return additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any?) {
        additionalProperties[name] = value
        if (VITAMINS_MAP.containsKey(name)) {
            containsVitamins = true
        } else if (MINERALS_MAP.containsKey(name)) {
            containsMinerals = true
        }
    }

    class Nutriment internal constructor(
            val key: String,
            val name: String,
            private val for100g: String,
            private val forServing: String,
            unit: String,
            val modifier: String
    ) {
        val unit: String = getRealUnit(unit)
        val displayStringFor100g: String
            get() {
                val builder = StringBuilder()
                val mod = getModifierNonDefault(modifier)
                if (mod != "") {
                    builder.append(mod).append(" ")
                }
                return builder.append(getRoundNumber(for100gInUnits)).append(" ").append(unit).toString()
            }

        /**
         * All the values given by the api are in gram. For all unit it's possible to convert back to th
         *
         * @param unit the initial unit
         * @return if the unit is % DV, the api gives the value in g
         */
        @Contract(pure = true)
        private fun getRealUnit(unit: String) = if (unit.contains("%")) Units.UNIT_GRAM else unit

        /**
         * Returns the amount of nutriment per 100g
         * of product in the units stored in [Nutriment.unit]
         */
        val for100gInUnits: String
            get() = getValueInUnits(for100g, unit)

        /**
         * Returns the amount of nutriment per serving
         * of product in the units stored in [Nutriment.unit]
         */
        val forServingInUnits: String
            get() = getValueInUnits(forServing, unit)

        private fun getValueInUnits(valueInGramOrMl: String, unit: String): String {
            if (valueInGramOrMl.isBlank()) {
                return StringUtils.EMPTY
            }
            if (valueInGramOrMl.isEmpty() || unit == Units.UNIT_GRAM) {
                return valueInGramOrMl
            }
            var value = valueInGramOrMl.toFloat()
            value = convertFromGram(value, unit)
            return getRoundNumber(value)
        }

        /**
         * Calculates the nutriment value for a given amount of this product. For example,
         * calling getForAnyValue(1, "kg") will give you the amount of this nutriment
         * given 1 kg of the product.
         *
         * @param userSetServing amount of this product used to calculate nutriment value
         * @param otherUnit units in either "g", "kg", or "mg" to define userSetServing
         * @return nutriment value for a given amount of this product
         */
        fun getForAnyValue(userSetServing: Float, otherUnit: String?): String {
            val strValue = for100gInUnits
            if (strValue.isEmpty() || strValue.contains("%")) {
                return strValue
            }
            try {
                val valueFor100g = strValue.toFloat()
                val portionInGram = convertToGrams(userSetServing, otherUnit)
                return getRoundNumber(valueFor100g / 100 * portionInGram)
            } catch (fmt: NumberFormatException) {
                Log.w(Nutriments::class.java.simpleName, "getForAnyValue can't parse value $strValue", fmt)
            }
            return StringUtils.EMPTY
        }
    }
}