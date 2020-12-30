/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.features

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.edit
import androidx.preference.*
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.jobs.LoadTaxonomiesWorker
import openfoodfacts.github.scrachx.openfood.jobs.OfflineProductWorker.Companion.scheduleSync
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfigDao
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryName
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameDao
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLocale
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType
import org.greenrobot.greendao.async.AsyncOperation
import org.greenrobot.greendao.async.AsyncOperationListener
import org.greenrobot.greendao.query.WhereCondition.StringCondition
import java.util.*

/**
 * A class for creating all the ListPreference
 */
class PreferencesFragment : PreferenceFragmentCompat(), INavigationItem, OnSharedPreferenceChangeListener {
    private val disp = CompositeDisposable()
    override val navigationDrawerListener: NavigationDrawerListener? by lazy {
        if (activity is NavigationDrawerListener) activity as NavigationDrawerListener
        else null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.findItem(R.id.action_search).isVisible = false
    }

    override fun onCreatePreferences(bundle: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setHasOptionsMenu(true)

        val activity = requireActivity()
        val settings = activity.getSharedPreferences("prefs", 0)

        val finalLocalValues = ArrayList<String>()
        val finalLocalLabels = ArrayList<String?>()

        val localeValues = activity.resources.getStringArray(R.array.languages_array)
        val localeLabels = arrayOfNulls<String>(localeValues.size)

        localeValues.indices.forEach { i ->
            val current = getLocale(localeValues[i])
            localeLabels[i] = current.getDisplayName(current).capitalize(Locale.getDefault())
            finalLocalLabels.add(localeLabels[i])
            finalLocalValues.add(localeValues[i])

        }
        val languagePreference = requirePreference<ListPreference>("Locale.Helper.Selected.Language")
        languagePreference.entries = finalLocalLabels.toTypedArray()
        languagePreference.entryValues = finalLocalValues.toTypedArray()
        languagePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, locale: Any? ->
            val configuration = activity.resources.configuration
            Toast.makeText(context, getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLocale(getLocale(locale as String?))
                activity.recreate()
            }
            true
        }
        val appThemeEntries = resources.getStringArray(R.array.application_theme_entries)
        requirePreference<ListPreference>("applicationThemePreference").let {
            it.setEntries(R.array.application_theme_entries)
            it.setEntryValues(R.array.application_theme_entries)
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                when (value) {
                    appThemeEntries[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    appThemeEntries[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                true
            }
        }

        requirePreference<Preference>("deleteSearchHistoryPreference").onPreferenceClickListener = OnPreferenceClickListener {
            MaterialDialog.Builder(activity)
                    .content(R.string.search_history_pref_dialog_content)
                    .positiveText(R.string.delete_txt)
                    .onPositive { _, _ ->
                        Toast.makeText(context, getString(R.string.preference_delete_search_history), Toast.LENGTH_SHORT).show()
                        val suggestions = SearchRecentSuggestions(context, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE)
                        suggestions.clearHistory()
                    }
                    .neutralText(R.string.dialog_cancel)
                    .onNeutral { dialog, _ -> dialog.dismiss() }
                    .show()
            true
        }

        val countryLabels = ArrayList<String>()
        val countryTags = ArrayList<String>()

        val countryPreference = requirePreference<ListPreference>(LocaleHelper.USER_COUNTRY_PREFERENCE_KEY)

        val asyncSessionCountries = OFFApplication.daoSession.startAsyncSession()
        val countryNameDao = OFFApplication.daoSession.countryNameDao

        // Set query finish listener
        asyncSessionCountries.listenerMainThread = AsyncOperationListener { operation: AsyncOperation ->
            val countryNames = operation.result as List<CountryName>
            countryNames.indices.forEach { i ->
                countryLabels.add(countryNames[i].name)
                countryTags.add(countryNames[i].countyTag)
            }
            countryPreference.entries = countryLabels.toTypedArray()
            countryPreference.entryValues = countryTags.toTypedArray()
        }
        // Execute query
        asyncSessionCountries.queryList(countryNameDao.queryBuilder()
                .where(CountryNameDao.Properties.LanguageCode.eq(getLanguage(getActivity())))
                .orderAsc(CountryNameDao.Properties.Name).build())

        countryPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference, newValue: Any? ->
            if (preference is ListPreference && preference.getKey() == LocaleHelper.USER_COUNTRY_PREFERENCE_KEY) {
                val country = newValue as String?
                settings.edit { putString(preference.getKey(), country) }
                Toast.makeText(context, getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
            }
            true
        }
        requirePreference<Preference>("contact_team").onPreferenceClickListener = OnPreferenceClickListener {
            val contactIntent = Intent(Intent.ACTION_SENDTO)
            contactIntent.data = Uri.parse(getString(R.string.off_mail))
            contactIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                startActivity(contactIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(getActivity(), R.string.email_not_found, Toast.LENGTH_SHORT).show()
            }
            true
        }
        requirePreference<Preference>("RateUs").onPreferenceClickListener = OnPreferenceClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.packageName)))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + activity.packageName)))
            }
            true
        }
        requirePreference<Preference>("FAQ").onPreferenceClickListener =
                OnPreferenceClickListener { openWebCustomTab(R.string.faq_url) }
        requirePreference<Preference>("Terms").onPreferenceClickListener =
                OnPreferenceClickListener { openWebCustomTab(R.string.terms_url) }
        requirePreference<Preference>("local_translate_help").onPreferenceClickListener =
                OnPreferenceClickListener { openWebCustomTab(R.string.translate_url) }
        val energyUnitPreference = requirePreference<ListPreference>("energyUnitPreference")
        val energyUnits = requireActivity().resources.getStringArray(R.array.energy_units)
        energyUnitPreference.entries = energyUnits
        energyUnitPreference.entryValues = energyUnits
        energyUnitPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            settings.edit { putString("energyUnitPreference", newValue as String?) }
            Toast.makeText(getActivity(), getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
            true
        }
        val volumeUnitPreference = requirePreference<ListPreference>("volumeUnitPreference")
        val volumeUnits = requireActivity().resources.getStringArray(R.array.volume_units)
        volumeUnitPreference.entries = volumeUnits
        volumeUnitPreference.entryValues = volumeUnits
        volumeUnitPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            settings.edit { putString("volumeUnitPreference", newValue as String?) }
            Toast.makeText(getActivity(), getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
            true
        }
        val values = requireActivity().resources.getStringArray(R.array.upload_image)
        requirePreference<ListPreference>("ImageUpload").let {
            it.entries = values
            it.entryValues = values
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                settings.edit { putString("imageUpload", newValue as String?) }
                Toast.makeText(getActivity(), getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
                true
            }
        }


        // Disable photo mode for OpenProductFacts
        if (isFlavors(AppFlavors.OPF)) requirePreference<Preference>("photoMode").isVisible = false

        // Preference to show version name
        requirePreference<Preference>("Version").let {
            it.isEnabled = false
            try {
                val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
                val version = pInfo.versionName
                it.summary = getString(R.string.version_string) + " " + version
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(PreferencesFragment::class.simpleName, "onCreatePreferences", e)
            }
            if (isFlavors(OFF, OBF, OPFF)) {
                getAnalysisTagConfigs(OFFApplication.daoSession)
            } else {
                preferenceScreen.removePreference(preferenceScreen.requirePreference("display_category"))
            }
        }
    }

    private fun buildDisplayCategory(configs: List<AnalysisTagConfig>?) {
        if (!isAdded) return

        val displayCategory = preferenceScreen.findPreference<PreferenceCategory>("display_category")
                ?: throw error("Display category preference does not exist.")
        displayCategory.removeAll()
        preferenceScreen.addPreference(displayCategory)

        // If analysis tag is empty show "Load ingredient detection data" option in order to manually reload taxonomies
        if (configs == null || configs.isEmpty()) {
            val preference = Preference(preferenceScreen.context)
            preference.setTitle(R.string.load_ingredient_detection_data)
            preference.setSummary(R.string.load_ingredient_detection_data_summary)
            preference.onPreferenceClickListener = OnPreferenceClickListener { pref: Preference ->
                pref.onPreferenceClickListener = null
                val manager = WorkManager.getInstance(requireContext())
                val request = OneTimeWorkRequest.from(LoadTaxonomiesWorker::class.java)

                // The service will load server resources only if newer than already downloaded...
                manager.enqueue(request)
                manager.getWorkInfoByIdLiveData(request.id).observe(this@PreferencesFragment, { workInfo: WorkInfo? ->
                    if (workInfo != null) {
                        if (workInfo.state == WorkInfo.State.RUNNING) {
                            preference.setTitle(R.string.please_wait)
                            preference.setIcon(R.drawable.ic_cloud_download_black_24dp)
                            preference.summary = null
                            preference.widgetLayoutResource = R.layout.loading
                        } else if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            getAnalysisTagConfigs(OFFApplication.daoSession)
                        }
                    }
                })
                true
            }
            displayCategory.addPreference(preference)
        } else {
            configs.forEach { config ->
                displayCategory.addPreference(CheckBoxPreference(this.preferenceScreen.context).apply {
                    key = config.type
                    setDefaultValue(true)
                    summary = null
                    summaryOn = null
                    summaryOff = null
                    title = getString(R.string.display_analysis_tag_status, config.typeName.toLowerCase(Locale.getDefault()))
                })
            }
        }
        displayCategory.isVisible = true
    }

    private fun openWebCustomTab(faqUrl: Int): Boolean {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.intent.putExtra("android.intent.extra.REFERRER", Uri.parse("android-app://" + requireContext().packageName))
        CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, Uri.parse(getString(faqUrl)), WebViewFallback())
        return true
    }

    @NavigationDrawerType
    override fun getNavigationDrawerType() = NavigationDrawerListener.ITEM_PREFERENCES

    override fun onResume() {
        super.onResume()
        try {
            (this.activity as? AppCompatActivity)?.supportActionBar!!.title = getString(R.string.action_preferences)
        } catch (e: NullPointerException) {
            Log.e(this::class.simpleName, "on resume error", e)
        }
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onDestroy() {
        disp.dispose()
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "enableMobileDataUpload" -> scheduleSync()
        }
    }

    private fun getAnalysisTagConfigs(daoSession: DaoSession) {
        val language = getLanguage(requireContext())
        Single.fromCallable {
            val analysisTagConfigDao = daoSession.analysisTagConfigDao
            val analysisTagConfigs = analysisTagConfigDao.queryBuilder()
                    .where(StringCondition("1 GROUP BY type"))
                    .orderAsc(AnalysisTagConfigDao.Properties.Type).build().list()
            val analysisTagNameDao = daoSession.analysisTagNameDao
            for (config in analysisTagConfigs) {
                val type = "en:${config.type}"
                var analysisTagTypeName = analysisTagNameDao.queryBuilder()
                        .where(
                                AnalysisTagNameDao.Properties.AnalysisTag.eq(type),
                                AnalysisTagNameDao.Properties.LanguageCode.eq(language),
                        ).unique()
                if (analysisTagTypeName == null) {
                    analysisTagTypeName = analysisTagNameDao.queryBuilder()
                            .where(
                                    AnalysisTagNameDao.Properties.AnalysisTag.eq(type),
                                    AnalysisTagNameDao.Properties.LanguageCode.eq("en")
                            ).unique()
                }
                config.typeName = if (analysisTagTypeName != null) analysisTagTypeName.name else config.type
            }
            analysisTagConfigs
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { configs -> buildDisplayCategory(configs) }
                .addTo(disp)
    }

    companion object {
        const val LOGIN_PREF = "login"
        fun newInstance() = PreferencesFragment().apply { arguments = Bundle() }
    }
}