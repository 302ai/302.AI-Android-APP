package com.ai302.app.http
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/4/9
 * desc   :
 * version: 1.0
 */
import androidx.room.Query
import androidx.versionedparcelable.ParcelField
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @GET("users/{userId}")
    fun getUserAsync(@Path("userId") userId: String): Deferred<User>

    @POST("v1/chat/completions")
    suspend fun postChatCompletion(
        @Header("Accept") accept: String = "application/json",
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body requestBody: Any
    ): ChatCompletionResponse

    @POST("v1/chat/completions")
    suspend fun postChatCompletionStr(
        @Header("Accept") accept: String = "text/event-stream", // 关键：接受 SSE 流
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body requestBody: Any
    ): Response<ResponseBody> // 返回 ResponseBody 处理流


    @GET("v1/models")
    suspend fun get302AiModels(
        @Header("Accept") accept: String = "application/json",
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @retrofit2.http.Query("llm") llm : String
    ) : ModelListResponse


    @Multipart  // 关键修复：添加多部分表单注解
    @POST("/v1/audio/transcriptions")
    suspend fun postAudioToText(
        @Header("Accept") accept: String = "application/json",
        @Header("Authorization") authorization: String,
        @Part("model") model: RequestBody,  // 字符串参数（参数名"model"）
        @Part file: MultipartBody.Part  // 文件部分（参数名由createFormData的第一个参数决定）
    ): AudioToTextResponse

    @Multipart
    @POST("https://test-api2.proxy302.com/gpt/api/upload/gpts/image") // 硬编码接口路径
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("prefix") prefix: Any,
        @Part("need_compress") needCompress: Boolean
    ): UploadImageResponse // 返回类型为UploadResponse



}

data class ModelListResponse(
    val data:List<ModelList>,
    //val `object`:String
    //val objectType: String = "list"
    val error:ErrorData
)

data class ModelList(
    val id: String,
    //val `object`: String,
)

// 嵌套数据结构
data class ModelArchitecture(
    val modality: String?,
    val input_modalities: List<String>?,
    val output_modalities: List<String>?,
    val tokenizer: String?,
    val instruct_type: String?
)

data class ModelPricing(
    val prompt: String?,
    val completion: String?,
    val request: String?,
    val image: String?,
    val web_search: String?,
    val internal_reasoning: String?
)

data class ModelProvider(
    val context_length: Int?,
    val max_completion_tokens: Int?,
    val is_moderated: Boolean?
)

data class Architecture(
    val modality: String,
    val input_modalities: List<String>,
    val output_modalities: List<String>,
    val tokenizer: String,
    val instruct_type: Any?
)

data class Pricing(
    val prompt: String,
    val completion: String,
    val request: String,
    val image: String,
    val web_search: String,
    val internal_reasoning: String
)

data class TopProvider(
    val context_length: Long,
    val max_completion_tokens: Int,
    val is_moderated: Boolean
)

data class ErrorData(
    val err_code: Int,
    val message: String,
    val message_cn: String,
    val message_jp: String,
    val type: String
)

data class ChatCompletionResponse(
    val id: String,
    val `object`: String, // "object" 是 Kotlin 保留字，需用反引号包裹
    val created: Long,
    val choices: List<Choice>,
    val usage: Usage,
    val citations: List<String>? = null, // 新增的引用链接列表
    val model: String, // 新增的模型名称
    val service_tier: String, // 新增的服务层
    val system_fingerprint: Any? // 系统指纹（可能为 null，用 Any? 或具体类型）
)

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String,
    val logprobs: Any? // 原返回值中为 null，根据实际情况可能为对象或 null
)

// 修正后的 Message 类（新增 annotations 和 refusal 字段）
data class Message(
    val role: String,
    val content: String,
    val reasoning_content:String,//深度思考过程
    val annotations: List<Any>, // 注解列表（示例中为空数组，用 Any 或具体类型）
    val refusal: Any? // 拒绝原因（示例中为 null，用 Any 或具体类型）
)

// 修正后的 Usage 类（新增详细令牌统计字段）
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int,
    val prompt_tokens_details: PromptTokensDetails, // 输入令牌详情
    val completion_tokens_details: CompletionTokensDetails // 输出令牌详情
)

// 输入令牌详情（prompt_tokens_details）
data class PromptTokensDetails(
    val audio_tokens: Int, // 音频令牌数（示例中为 0）
    val cached_tokens: Int // 缓存令牌数（示例中为 0）
)

// 输出令牌详情（completion_tokens_details）
data class CompletionTokensDetails(
    val accepted_prediction_tokens: Int, // 接受的预测令牌数
    val audio_tokens: Int, // 音频令牌数
    val reasoning_tokens: Int, // 推理令牌数
    val rejected_prediction_tokens: Int // 拒绝的预测令牌数
)


data class ChatCompletionRequest(
    val model: String = "gpt-3.5-turbo-web-search",

    val `web-search`: Boolean = false,
    val messages: List<RequestMessage>,
    val userid: String = "",
    val stream: Boolean = false
)

data class ChatCompletionRequest1(
    val model: String = "gpt-3.5-turbo-web-search",
    val messages: List<RequestMessage1>,
    val stream: Boolean = false,
    val userid: String? = ""
)

data class ChatCompletionLlamaRequest(
    val model: String = "gpt-3.5-turbo-web-search",

    val `web-search`: Boolean = true,
    val messages: List<RequestMessage>,

    val stream: Boolean = false
)

data class RequestMessage(
    val role: String,
    val content: String
)

data class RequestMessage1(
    val role: String,
    val content: List<QuestionMessage>
)

data class QuestionMessage(
    val type: String,
    val text: String
)

data class User(
    val id: String,
    val name: String,
    val email: String
)


data class AudioToTextRequest(
    val model: String = "gpt-3.5-turbo-web-search",
    val file: MultipartBody.Part
)

data class AudioToTextResponse(
    val text: String,
    val error: Any?

)

data class UploadImageResponse(
    val code: Int,
    val msg: String,
    val data: UploadData?
)

data class UploadData(
    val url: String,
    val encoding: String
)

// 图片分析请求模型
data class ChatRequestImage(
    val model: String = "gpt-3.5-turbo-ocr",
    val ocr_model: String = "gpt-4o-mini",
    val stream: Boolean = false,
    val messages: List<MessageImage>
)

data class MessageImage(
    val role: String = "user",
    val content: List<ContentImage>
)

//@JsonClass(generateAdapter = true)
data class ContentImage (
    //@JsonClass(generateAdapter = true)
    //data class TextContent(val type: String,val text: String) : ContentImage()
    //@JsonClass(generateAdapter = true)
    //data class ImageContent(val type: String,val image_url: ImageUrl) : ContentImage()
    val type : String,
    val text : String,
    val image_url : ImageUrl? = null
)
//@JsonClass(generateAdapter = true)
data class ImageUrl(val url: String)


/*





data class ChatCompletionResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)*/

//流式处理
// 将 ResponseBody 转换为 Flow<String>
fun Response<ResponseBody>.bodyAsFlow(): Flow<String> = callbackFlow {
    if (!isSuccessful) {
        close(IOException("HTTP error: ${code()}"))
        return@callbackFlow
    }

    val body = body() ?: run {
        close(IOException("No response body"))
        return@callbackFlow
    }

    // 使用 BufferedReader 逐行读取 SSE 数据
    val reader = BufferedReader(InputStreamReader(body.byteStream()))
    var line: String?

    try {
        while (reader.readLine().also { line = it } != null) {
            if (line!!.startsWith("data: ")) {
                // 提取 JSON 数据（去掉 "data: " 前缀）
                val data = line!!.substring("data: ".length).trim()
                if (data != "[DONE]") { // 忽略结束标记
                    send(data)
                }
            }
        }
    } catch (e: IOException) {
        close(e)
    } finally {
        reader.close()
        body.close()
        close()
    }
}


// 处理流式请求的函数
suspend fun processChatStream(
    apiService: ApiService,
    requestBody: Any,
    authorization: String,
    onData: (String) -> Unit, // 处理每个数据块
    onError: (Throwable) -> Unit, // 处理错误
    onComplete: () -> Unit // 流结束
) {
    try {
        val response = apiService.postChatCompletionStr(
            authorization = authorization,
            requestBody = requestBody
        )

        // 将响应体转换为 Flow
        response.bodyAsFlow().collect { data ->
            // 在主线程处理数据
            onData(data)
        }
    } catch (e: Exception) {
        onError(e)
    } finally {
        onComplete()
    }
}

// 流式响应数据模型（与之前一致）
data class StreamResponse(
    val choices: List<ChoiceStr>,
    val created: Long,
    val id: String,
    val model: String,
//    val citations: List<String>, // 新增的引用链接列表
    val citations: List<String>? = null, // 关键修改：允许 citations 为 null
    @SerializedName("object") val objType: String,
    val system_fingerprint: String? = null,
    val usage: Usage? = null
)

data class ChoiceStr(
    val delta: Delta,
    val finish_reason: String?,
    val index: Int,
    val logprobs: Any?
)

data class Delta(
    val content: String? = null,
    val role: String? = null,
    val reasoning_content:String,//深度思考过程
    val annotations: List<Any>, // 注解列表（示例中为空数组，用 Any 或具体类型）
    val refusal: Any? // 拒绝原因（示例中为 null，用 Any 或具体类型）
)

