package com.ai302.app.datastore
import android.annotation.SuppressLint
import android.content.Context
import android.media.Image
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/4/14
 * desc   :
 * version: 1.0
 */
// 定义 DataStore 的名称
private const val DATA_STORE_NAME = "my_data_store"

// 扩展属性，用于获取 DataStore 实例
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

class DataStoreManager(private val context: Context) {

    // 定义一个偏好键
    private val API_KEY = stringPreferencesKey("api_key")
    private val CHAT_LIST_NUMBER = intPreferencesKey("chat_list_number")
    private val IS_CHAT = booleanPreferencesKey("is_chat")
    private val SERVICE_URL = stringPreferencesKey("service_url")
    private val MODEL_TYPE = stringPreferencesKey("model_type")
    private val LAST_SELECTED_POSITION = intPreferencesKey("last_selected_position")
    private val IMAGE_URL = stringPreferencesKey("image_url")
    private val CUE_WORDS = stringPreferencesKey("cue_words")
    private val CUE_WORDS_SWITCH = booleanPreferencesKey("cue_words_switch")

    private val CLEAR_WORDS_SWITCH = booleanPreferencesKey("clear_words_switch")
    private val PRE_SWITCH = booleanPreferencesKey("pre_switch")
    private val EXTRACT_SWITCH = booleanPreferencesKey("extract_switch")
    private val OFFICIAL_WORDS_SWITCH = booleanPreferencesKey("official_words_switch")
    private val SEARCH_SWITCH = booleanPreferencesKey("search_switch")

    private val APP_EMOJIS = stringPreferencesKey("app_emojis")
    private val CUSTOMIZE_API_KEY = stringPreferencesKey("customize_api_key")
    private val CUSTOMIZE_SERVICE_URL = stringPreferencesKey("customize_service_url")
    private val CUSTOMIZE_MODEL_ID = stringPreferencesKey("customize_model_id")
    private val SERVICE_PROVIDER = stringPreferencesKey("service_provider")

    private val OPEN_AI_API_KEY = stringPreferencesKey("open_ai_api_key")
    private val ANTHROPIC_API_KEY = stringPreferencesKey("anthropic_api_key")

    private val IS_CHANGE_MODEL_SETTING = booleanPreferencesKey("is_change_Model_Setting")

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: DataStoreManager? = null

        fun getInstance(context: Context): DataStoreManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataStoreManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }



    // 保存数据到 DataStore
    suspend fun saveData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = data
        }
    }

    suspend fun saveChatListNumber(number:Int){
        context.dataStore.edit { preferences ->
            preferences[CHAT_LIST_NUMBER] = number
        }
    }

    suspend fun saveServiceUrl(data: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVICE_URL] = data
        }
    }

    suspend fun saveModelType(data: String) {
        context.dataStore.edit { preferences ->
            preferences[MODEL_TYPE] = data
        }
    }

    suspend fun saveLastPosition(data: Int) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SELECTED_POSITION] = data
        }
    }

    suspend fun saveChatIs(data: String){
        context.dataStore.edit { preferences ->
            preferences[MODEL_TYPE] = data
        }
    }

    suspend fun saveImageUrl(data: String) {
        context.dataStore.edit { preferences ->
            preferences[IMAGE_URL] = data
        }
    }

    suspend fun saveCueWords(data: String) {
        context.dataStore.edit { preferences ->
            preferences[CUE_WORDS] = data
        }
    }

    suspend fun saveCueWordsSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CUE_WORDS_SWITCH] = data
        }
    }

    suspend fun saveOfficialWordsSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[OFFICIAL_WORDS_SWITCH] = data
        }
    }

    suspend fun saveClearWordsSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CLEAR_WORDS_SWITCH] = data
        }
    }

    suspend fun savePreSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PRE_SWITCH] = data
        }
    }

    suspend fun saveExtractSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[EXTRACT_SWITCH] = data
        }
    }

    suspend fun saveSearchSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SEARCH_SWITCH] = data
        }
    }

    suspend fun saveAppEmojisData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_EMOJIS] = data
        }
    }

    suspend fun saveCustomizeKeyData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOMIZE_API_KEY] = data
        }
    }

    suspend fun saveCustomizeServiceUrlData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOMIZE_SERVICE_URL] = data
        }
    }

    suspend fun saveCustomizeModelIdData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOMIZE_MODEL_ID] = data
        }
    }

    suspend fun saveServiceProviderData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVICE_PROVIDER] = data
        }
    }

    suspend fun saveIsChangeModelSetting(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_CHANGE_MODEL_SETTING] = data
        }
    }

    suspend fun saveOpenAiKeyData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[OPEN_AI_API_KEY] = data
        }
    }

    suspend fun saveAnthropiocKeyData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[ANTHROPIC_API_KEY] = data
        }
    }

    // 从 DataStore 读取数据
    val readData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[API_KEY]
        }

    val readChatListNumber: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[CHAT_LIST_NUMBER]
        }

    val readChatIs: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[IS_CHAT]
        }

    val readServiceUrl: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SERVICE_URL]
        }

    val readModelType: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[MODEL_TYPE]
        }

    val readLastPosition: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_SELECTED_POSITION]
        }

    val readImageUrl: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[IMAGE_URL]
        }

    val readCueWords: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CUE_WORDS]
        }

    val readCueWordsSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[CUE_WORDS_SWITCH]
        }

    val readOfficialWordsSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[OFFICIAL_WORDS_SWITCH]
        }

    val readClearWordsSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[CLEAR_WORDS_SWITCH]
        }

    val readPreSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[PRE_SWITCH]
        }

    val readExtractSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[EXTRACT_SWITCH]
        }

    val readSearchSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[SEARCH_SWITCH]
        }

    val readAppEmojisData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[APP_EMOJIS]
        }

    val readCustomizeKeyData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CUSTOMIZE_API_KEY]
        }

    val readCustomizeServiceUrlData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CUSTOMIZE_SERVICE_URL]
        }

    val readCustomizeModelIdData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CUSTOMIZE_MODEL_ID]
        }

    val readServiceProviderData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SERVICE_PROVIDER]
        }

    val readIsChangeModelSetting: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[IS_CHANGE_MODEL_SETTING]
        }

    val readOpenAiKeyData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[OPEN_AI_API_KEY]
        }

    val readAnthropicKeyData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[ANTHROPIC_API_KEY]
        }


}