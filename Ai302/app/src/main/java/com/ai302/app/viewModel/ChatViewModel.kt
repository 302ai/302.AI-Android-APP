package com.ai302.app.viewModel

import android.content.Context
import android.net.http.HttpException
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai302.app.databinding.ActivityChatBinding
import com.ai302.app.http.ApiService
import com.ai302.app.http.AudioToTextRequest
import com.ai302.app.http.ChatCompletionLlamaRequest
import com.ai302.app.http.ChatCompletionRequest
import com.ai302.app.http.ChatCompletionRequest1
import com.ai302.app.http.ChatRequestImage
import com.ai302.app.http.ContentImage
import com.ai302.app.http.ImageUrl
import com.ai302.app.http.MessageImage
import com.ai302.app.http.ModelList
import com.ai302.app.http.NetworkFactory
import com.ai302.app.http.QuestionMessage
import com.ai302.app.http.RequestMessage
import com.ai302.app.http.RequestMessage1
import com.ai302.app.http.StreamResponse
import com.ai302.app.http.processChatStream
import com.ai302.app.utils.SystemUtils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/4/14
 * desc   :
 * version: 1.0
 */
class ChatViewModel :ViewModel(){
    val questionResult = MutableLiveData<String?>()
    val modelListResult = MutableLiveData<MutableList<String>>()
    private var modelList = mutableListOf<String>()
    private var modelListNull = mutableListOf<String>()
    val voiceToTextResult = MutableLiveData<String?>()
    val imageUrlServiceResult = MutableLiveData<String?>()
    //val apiService = NetworkFactory.createApiService(ApiService::class.java)

    private lateinit var request: Any

    val questionResultStr = MutableLiveData<String?>()
    private var mContent = StringBuilder()
    private var mDeepThinkAssistantMessage = StringBuilder()
    private var mCitations = mutableListOf<String>()

    private var contentList = mutableListOf<ContentImage>()
    private var contentMessagesList = mutableListOf<RequestMessage>()


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun sendQuestion1(question:String,modelType:String,isNetWorkThink:Boolean,isDeepThink:Boolean,context:Context,userId:String,imageUrlServiceResult:String,isPrompt:Boolean,apikey: String,isExtract:Boolean,apiService:ApiService){
        Log.e("ceshi","sendQuestion1$apikey")
        val authorizationToken = "Bearer sk-uuhpqdroarauzlltffpybpnvoskghjnlmstmdzynjizybtcb"
        //val authorizationToken = "Bearer $apikey"//sk-RcnM7qzDdqa3i4mylTGPSl5peJzu8CNMx2pe6cauC0es3JCA
        //val authorizationToken1 = "sk-RcnM7qzDdqa3i4mylTGPSl5peJzu8CNMx2pe6cauC0es3JCA"
        Log.e("ceshi","0deep:$isDeepThink,,web:$isNetWorkThink")
        var extract = ""
//        if (isExtract){
//            extract = "-file-parse"
//        }
        /*var request = ChatCompletionRequest(
            messages = listOf(
                RequestMessage(
                    role = "user",
                    content = question
                )
            ),
            model = "$modelType",
            userid = userId
        )*/
        var mModelType = modelType+extract
        if (modelType.contains("deepseek-reasoner")){
            mModelType = "deepseek-reasoner"
        }
        //mModelType = "Qwen/Qwen2.5-7B-Instruct"

        var firstPart = ""
        var secondPart = ""
        if (imageUrlServiceResult == ""){
            if (isPrompt){
                val separator = "&&&"
                val parts = question.split(separator, limit = 2)
                if (parts.size == 2) {
                    firstPart = parts[0]
                    secondPart = parts[1]
                    println("第一部分: $firstPart")
                    println("第二部分: $secondPart")

                }


                request = ChatCompletionRequest1(
                    model = "$mModelType",
                    stream = false,
                    messages = listOf(
                        RequestMessage1(
                            role = "system",
                            content = listOf(QuestionMessage(type = "text", text = firstPart))
                        ),
                        RequestMessage1(
                            role = "user",
                            content = listOf(QuestionMessage(type = "text", text = secondPart))
                        )
                    ),
                    userid = userId

                )
            }else{
                request = ChatCompletionRequest1(
                    model = "$mModelType",
                    stream = false,
                    messages = listOf(
                        RequestMessage1(
                            role = "user",
                            content = listOf(QuestionMessage(type = "text", text = question))
                        )
                    ),
                    userid = userId
                )
            }

        }else{
            request = ChatRequestImage(
                model = "$mModelType",
                stream = false,
                messages = listOf(
                    MessageImage(
                        role = "system",
                        content = listOf(
                            ContentImage(type = "text", text = question, image_url = ImageUrl(url = imageUrlServiceResult)),
                            ContentImage(type = "image_url", text = question ,image_url = ImageUrl(url = imageUrlServiceResult))
                        )
                    )
                ),
                ocr_model = "gpt-4o-mini"
            )
        }

        if (imageUrlServiceResult == ""){
            if (isDeepThink) {
                when {
                    isNetWorkThink -> {
                        request = ChatCompletionRequest1(
                            model = "$mModelType-r1-fusion",
                            stream = false,
                            messages = listOf(
                                RequestMessage1(
                                    role = "system",
                                    content = listOf(QuestionMessage(type = "text", text = question))
                                )
                            ),
                            userid = userId

                        )
                    }
                    else -> {
                        request = ChatCompletionRequest1(
                            model = "$mModelType-r1-fusion",
                            stream = false,
                            messages = listOf(
                                RequestMessage1(
                                    role = "system",
                                    content = listOf(QuestionMessage(type = "text", text = question))
                                )
                            ),
                            userid = userId

                        )
                    }
                }
            } else {
                when {
                    isNetWorkThink -> {
                        request = ChatCompletionRequest1(
                            model = "$mModelType-r1-fusion",
                            stream = false,
                            messages = listOf(
                                RequestMessage1(
                                    role = "system",
                                    content = listOf(QuestionMessage(type = "text", text = question))
                                )
                            ),
                            userid = userId
                        )
                    }
                    else -> {

                    }
                }
            }
        }




        try {
            val response = apiService.postChatCompletion(
                authorization = authorizationToken,
                requestBody = request
            )
            // 处理返回的响应数据
            val assistantMessage = response.choices.firstOrNull()?.message?.content
            val deepThinkAssistantMessage = response.choices.firstOrNull()?.message?.reasoning_content
            val citations = response.citations
            Log.e("ceshi","0返回数据：${citations}")
            viewModelScope.launch(Dispatchers.Main) {
                // 在主线程更新 UI
                if (assistantMessage != null) {
                    // 可以在这里更新 UI 显示结果
                    Log.e("ceshi","深度思考返回数据:$deepThinkAssistantMessage")
                    Log.e("ceshi","返回数据：${assistantMessage}")
                    try {
                        //questionResult.postValue(deepThinkAssistantMessage+assistantMessage)
                        //questionResult.value = deepThinkAssistantMessage+"&&&"+assistantMessage
                        // 创建一个 StringBuilder 用于拼接字符串
                        val stringBuilder = StringBuilder()
                        var resultUrl = ""
                        val job = viewModelScope.launch(Dispatchers.IO) {


                            stringBuilder.append("<br>")
                            // 遍历数组元素
                            if (citations != null){
                                for (element in citations) {
                                    // 在每个元素后面添加换行符
                                    //stringBuilder.append(element).append("<br>")
//                                stringBuilder.append("<a href=\"$element\" style=\"color: blue; text-decoration: underline;\">\n" +
//                                        "                                $element\n" +
//                                        "                                </a>").append("<br>")

                                    stringBuilder.append("<span style=\"color: blue; text-decoration: underline;\">\n" +
                                            "  $element\n" +
                                            "</span>").append("<br>")
                                }

                                // 将 StringBuilder 转换为字符串
                                resultUrl = stringBuilder.toString()
                                Log.e("ceshi","网址：$resultUrl")
                            }

                        }
                        job.join()
                        // 统一使用 postValue
                        if (isNetWorkThink){
                            if (deepThinkAssistantMessage.isNullOrEmpty()){
                                questionResult.postValue("${assistantMessage}${convertUrlsToMarkdown(citations!!)}")
                            }else{
                                questionResult.postValue("${deepThinkAssistantMessage}&&&&&&${assistantMessage}${convertUrlsToMarkdown(citations!!)}")
                            }
                        }else{
                            if (deepThinkAssistantMessage.isNullOrEmpty()){
                                questionResult.postValue("${assistantMessage}")
                            }else{
                                questionResult.postValue("${deepThinkAssistantMessage}&&&&&&${assistantMessage}")
                            }
                        }


                    }catch(t: Throwable) {
                        Log.e("ChatViewModel", "更新 questionResult 时出现异常: ${t.message}")
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "更新 questionResult 时出现异常: ${e.message}")
                    }
                    Log.e("ceshi","1返回数据：${questionResult.value}")
                }
            }
        }catch (e: SocketTimeoutException) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "网络请求超时，请重试", Toast.LENGTH_SHORT).show()
                questionResult.postValue("网络请求超时，请重试")
                //questionResult.value = "网络请求超时，请重试"
            }
        }
        catch (e: HttpException) {
            // 处理 HTTP 错误
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "正在网络请求，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            // 处理网络错误
        } catch (e: retrofit2.HttpException){
            Log.e("ceshi","错误：${e.toString()}")
            viewModelScope.launch(Dispatchers.Main) {
                //Toast.makeText(context, "当前无可用通道，更多请访问 302.AI", Toast.LENGTH_SHORT).show()
                questionResult.postValue("当前无可用通道，更多请访问 302.AI")
                //questionResult.value = "网络请求超时，请重试"
            }
        }
    }

//    fun convertUrlsToMarkdown(urls: List<String>): String {
//        return urls.joinToString("\n") { url ->
//            "[$url]($url)"
//        }
//    }

    fun convertUrlsToMarkdown(urls: List<String>): String {
        return urls
            .mapIndexed { index, url ->  // mapIndexed 获取元素索引（从0开始）
                val order = index + 1     // 序号从1开始（索引+1）
                "<br>$order. [${url}](${url})" // 格式："1. [url](url)"
            }
            .joinToString("\n")  // 用换行符连接所有处理后的字符串
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun get302AiModelList(apikey:String,apiService:ApiService){
        val authorizationToken = "Bearer $apikey"//sk-RcnM7qzDdqa3i4mylTGPSl5peJzu8CNMx2pe6cauC0es3JCA
        //val authorizationToken = "Bearer sk-uuhpqdroarauzlltffpybpnvoskghjnlmstmdzynjizybtcb"//sk-RcnM7qzDdqa3i4mylTGPSl5peJzu8CNMx2pe6cauC0es3JCA
        try {
            val response = apiService.get302AiModels(
                authorization = authorizationToken,
                llm = "1"
            )
            // 处理返回的响应数据
            viewModelScope.launch(Dispatchers.Main) {
                // 在主线程更新 UI
                modelList.clear()
                for (model in response.data){
                    //Log.e("ceshi","get302AiModelList返回数据ID：${model.id}")
                    modelList.add(model.id)
                }
                modelListResult.postValue(modelList)
                //modelList.clear()
            }
        } catch (e: HttpException) {
            // 处理 HTTP 错误
        } catch (e: IOException) {
            // 处理网络错误
        } catch (e: retrofit2.HttpException){
            Log.e("ceshi","错误：${e.toString()}")
            modelListResult.postValue(modelListNull)
        } catch (e: IllegalArgumentException) {
            // 处理异常
            Log.e("Network", "无效的 Authorization 头: ${e.message}")

            // 可选：提供默认值或执行恢复逻辑
        }

    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun audioToText(audioFile: MultipartBody.Part,apikey: String,apiService:ApiService){
        //val authorizationToken = "Bearer sk-RcnM7qzDdqa3i4mylTGPSl5peJzu8CNMx2pe6cauC0es3JCA"
        val authorizationToken = "Bearer $apikey"//sk-RcnM7qzDdqa3i4mylTGPSl5peJzu8CNMx2pe6cauC0es3JCA
        var request = AudioToTextRequest(
            model = "whisper-v3-turbo",
            file = audioFile
        )

        // 在 AudioUploader 的 uploadRecording 方法中
        val modelBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),  // 文本类型 MIME
            "whisper-v3-turbo"  // 你的模型名称（如 "whisper-v3-turbo"）
        )

        try {
            val response = apiService.postAudioToText(
                authorization = authorizationToken,
                model = modelBody,
                file = audioFile
            )
            // 处理返回的响应数据
            val assistantMessage = response.text

            Log.e("ceshi","0返回数据：${response.text}")//https://api.302.ai/v1/audio/transcriptions
            viewModelScope.launch(Dispatchers.Main) {
                // 在主线程更新 UI
                if (assistantMessage != null) {
                    // 可以在这里更新 UI 显示结果
                    voiceToTextResult.postValue(assistantMessage)
                }
            }
        }catch (e: SocketTimeoutException) {
            viewModelScope.launch(Dispatchers.Main) {
                //questionResult.value = "网络请求超时，请重试"
            }
        }
        catch (e: HttpException) {
            // 处理 HTTP 错误
            viewModelScope.launch(Dispatchers.Main) {

            }
        } catch (e: IOException) {
            // 处理网络错误
        } catch (e: HttpException) {
            // 处理 HTTP 错误
        } catch (e: IOException) {
            // 处理网络错误
        } catch (e: retrofit2.HttpException){
            Log.e("ceshi","错误：${e.toString()}")
            viewModelScope.launch(Dispatchers.Main) {
                //Toast.makeText(context, "当前无可用通道，更多请访问 302.AI", Toast.LENGTH_SHORT).show()
                //questionResult.postValue("当前无可用通道，更多请访问 302.AI")
                //questionResult.value = "网络请求超时，请重试"
            }
        }
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun upLoadImage(context: Context, // 用于获取文件路径或处理UI（可选）
                            file: File,
                            prefix: Any = "imags", // 默认为图片参数
                            needCompress: Boolean = false, // 默认为不压缩
                            apiService:ApiService
        ){
        val authorizationToken = "Bearer sk-RcnM7qzDdqa3i4mylTGPSl5peJzu8CNMx2pe6cauC0es3JCA"
        // 动态获取文件MIME类型（推荐）
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(file.extension) ?: "application/octet-stream"
        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)


        try {
            val response = apiService.uploadImage(
                filePart, prefix, needCompress
            )
            // 处理返回的响应数据
            val assistantMessage = response.data?.url

            Log.e("ceshi","0返回数据url：${SystemUtils.replaceUrlString(assistantMessage!!)}")//https://api.302.ai/v1/audio/transcriptions
            viewModelScope.launch(Dispatchers.Main) {
                // 在主线程更新 UI
                if (assistantMessage != null) {
                    // 可以在这里更新 UI 显示结果
                    imageUrlServiceResult.postValue(SystemUtils.replaceUrlString(assistantMessage))
                }
            }
        }catch (e: SocketTimeoutException) {
            viewModelScope.launch(Dispatchers.Main) {
                //questionResult.value = "网络请求超时，请重试"
            }
        }
        catch (e: HttpException) {
            // 处理 HTTP 错误
            viewModelScope.launch(Dispatchers.Main) {

            }
        } catch (e: IOException) {
            // 处理网络错误
        } catch (e: retrofit2.HttpException) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "请求错误，请重试", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun String.toRequestBody(): RequestBody {
        return RequestBody.create("text/plain".toMediaTypeOrNull(), this)
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun sendQuestion(question:String,modelType:String,isNetWorkThink:Boolean,isDeepThink:Boolean,context:Context,userId:String,imageUrlServiceResultList:List<String>? = null,isPrompt:Boolean,apikey: String,isExtract:Boolean,apiService:ApiService,isClearContext:Boolean,messagesList:List<String>,serviceProvider:String){
        //val authorizationToken = "sk-RcnM7qzDdqa3i4mylTGPSl5peJzu8CNMx2pe6cauC0es3JCA"
        Log.e("ceshi","sendQuestion$question,,${imageUrlServiceResultList?.isEmpty()},,$isPrompt")
        var extract = ""
        if (isExtract){
            extract = "-file-parse"
            if (serviceProvider=="自定义"){
                extract = ""
            }
        }
        val authorizationToken = "Bearer $apikey"//sk-RcnM7qzDdqa3i4mylTGPSl5peJzu8CNMx2pe6cauC0es3JCA
        //val authorizationToken = "sk-uuhpqdroarauzlltffpybpnvoskghjnlmstmdzynjizybtcb"
        //val authorizationToken1 = "sk-RcnM7qzDdqa3i4mylTGPSl5peJzu8CNMx2pe6cauC0es3JCA"
        Log.e("ceshi","0deep:$isDeepThink,,web:$isNetWorkThink,,$userId")
        /*var request = ChatCompletionRequest(
            messages = listOf(
                RequestMessage(
                    role = "user",
                    content = question
                )
            ),
            model = "$modelType",
            userid = userId
        )*/
        var mModelType = modelType.removeSuffix("-")+extract
        if (modelType.contains("deepseek-reasoner")){
            mModelType = "deepseek-reasoner"+extract
        }else if (modelType.contains("llama")){
            mModelType = modelType.removeSuffix("-")+extract
        }

        var firstPart = ""
        var secondPart = ""
        if (imageUrlServiceResultList?.isEmpty()!!){
            if (isPrompt){
                val separator = "&&&"
                val parts = question.split(separator, limit = 2)
                if (parts.size == 2) {
                    firstPart = parts[0]
                    secondPart = parts[1]
                    println("第一部分: $firstPart")
                    println("第二部分: $secondPart")

                }


                if (modelType.contains("llama")){
                    request = ChatCompletionLlamaRequest(
                        messages = listOf(
                            RequestMessage(
                                role = "system",
                                content = firstPart
                            ),
                            RequestMessage(
                                role = "user",
                                content = secondPart
                            )
                        ),
                        model = "$mModelType",
                        //userid = userId,
                        stream = true
                    )
                }else{
                    request = ChatCompletionRequest(
                        messages = listOf(
                            RequestMessage(
                                role = "system",
                                content = firstPart
                            ),
                            RequestMessage(
                                role = "user",
                                content = secondPart
                            )
                        ),
                        model = "$mModelType",
                        userid = userId,
                        stream = true
                    )
                }

            }else{
                if (modelType.contains("llama")){
                    request = ChatCompletionLlamaRequest(
                        messages = listOf(
                            RequestMessage(
                                role = "user",
                                content = question
                            )
                        ),
                        model = "$mModelType",
                        stream = true
                    )
                }else{
                    request = ChatCompletionRequest(
                        messages = listOf(
                            RequestMessage(
                                role = "user",
                                content = question
                            )
                        ),
                        model = "$mModelType",
                        userid = userId,
                        stream = true
                    )
                }

            }

        }else{

            // 遍历列表，同时获取索引和元素
            /*if (imageUrlServiceResultList.size > 1){
                imageUrlServiceResultList.forEachIndexed { index, imageResult ->
                    when (index) {
                        0 -> {
                            // 第一次遍历
                            contentList.add(ContentImage(type = "text", text = question, image_url = ImageUrl(url = imageResult)))
                        }
                        else -> {
                            // 其他次数遍历
                            contentList.add(ContentImage(type = "image_url", text = question ,image_url = ImageUrl(url = imageResult)))

                        }
                    }
                }
            }else if (imageUrlServiceResultList.size == 1){

            }*/

            contentList.add(ContentImage(type = "text", text = question))
            for (imageResult in imageUrlServiceResultList){
                contentList.add(ContentImage(type = "image_url", text = question ,image_url = ImageUrl(url = imageResult)))
            }


            request = ChatRequestImage(
                messages = listOf(
                    MessageImage(
                        role = "user",
                        content = contentList
                    )
                ),
                model = "$mModelType",
                ocr_model = "gpt-4o-mini",
                stream = true
            )
        }

        if (isClearContext){

            if (imageUrlServiceResultList?.isEmpty()!!){
                if (isPrompt){
                    if (isDeepThink) {
                        when {
                            isNetWorkThink -> {
                                request = ChatCompletionRequest(
                                    messages = listOf(
                                        RequestMessage(
                                            role = "system",
                                            content = firstPart
                                        ),
                                        RequestMessage(
                                            role = "user",
                                            content = secondPart
                                        )
                                    ),
                                    model = "$mModelType-r1-fusion",
                                    stream = true,
                                    `web-search` = true,
                                    userid = ""

                                )
                            }
                            else -> {
                                request = ChatCompletionRequest(
                                    messages = listOf(
                                        RequestMessage(
                                            role = "system",
                                            content = firstPart
                                        ),
                                        RequestMessage(
                                            role = "user",
                                            content = secondPart
                                        )
                                    ),
                                    model = "$mModelType-r1-fusion",
                                    stream = true,
                                    `web-search` = false,
                                    userid = ""
                                )
                            }
                        }
                    } else {
                        when {
                            isNetWorkThink -> {
                                request = ChatCompletionRequest(
                                    messages = listOf(
                                        RequestMessage(
                                            role = "system",
                                            content = firstPart
                                        ),
                                        RequestMessage(
                                            role = "user",
                                            content = secondPart
                                        )
                                    ),
                                    model = "$mModelType-web-search",
                                    stream = true,
                                    `web-search` = true,
                                    userid = ""

                                )
                            }
                            else -> {
                                request = ChatCompletionRequest(
                                    messages = listOf(
                                        RequestMessage(
                                            role = "system",
                                            content = firstPart
                                        ),
                                        RequestMessage(
                                            role = "user",
                                            content = secondPart
                                        )
                                    ),
                                    model = "$mModelType",
                                    stream = true,
                                    `web-search` = false,
                                    userid = ""

                                )
                            }
                        }
                    }
                }else{
//                    messagesList.forEachIndexed { index, content ->
//                        val role = if (index % 2 == 0) "assistant" else "user"
//                        contentMessagesList.add(RequestMessage(role, content))
//                    }

                    if (isDeepThink) {
                        when {
                            isNetWorkThink -> {
                                request = ChatCompletionRequest(
                                    messages = listOf(
                                        RequestMessage(
                                            role = "user",
                                            content = question
                                        )
                                    ),
                                    model = "$mModelType-r1-fusion",
                                    stream = true,
                                    `web-search` = true,
                                    userid = ""

                                )
                            }
                            else -> {
                                request = ChatCompletionRequest(
                                    messages = listOf(
                                        RequestMessage(
                                            role = "user",
                                            content = question
                                        )
                                    ),
                                    model = "$mModelType-r1-fusion",
                                    stream = true,
                                    `web-search` = false,
                                    userid = ""
                                )
                            }
                        }
                    } else {
                        when {
                            isNetWorkThink -> {
                                request = ChatCompletionRequest(
                                    messages = listOf(
                                        RequestMessage(
                                            role = "user",
                                            content = question
                                        )
                                    ),
                                    model = "$mModelType-web-search",
                                    stream = true,
                                    `web-search` = true,
                                    userid = ""

                                )
                            }
                            else -> {
                                request = ChatCompletionRequest(
                                    messages = listOf(
                                        RequestMessage(
                                            role = "user",
                                            content = question
                                        )
                                    ),
                                    model = "$mModelType",
                                    stream = true,
                                    `web-search` = false,
                                    userid = ""

                                )
                            }
                        }
                    }
                }

            }
        }else{
            if (imageUrlServiceResultList?.isEmpty()!!){
                if (isPrompt){
                    // 从索引1开始遍历（跳过第一个元素）
                    contentMessagesList.add(RequestMessage("system", messagesList[0]))
                    for (i in 1 until messagesList.size) {
                        val content = messagesList[i]
                        if (content.contains("file:///android_asset/loading.html") || content.contains("这是删除过的内容变为空白")) {
                            continue // 跳过该元素
                        }
                        val role = if (i % 2 == 0) "assistant" else "user"
                        contentMessagesList.add(RequestMessage(role, content))
                    }
                    if (isDeepThink) {
                        when {
                            isNetWorkThink -> {
                                request = ChatCompletionRequest(
                                    messages = contentMessagesList,
                                    model = "$mModelType-r1-fusion",
                                    stream = true,
                                    `web-search` = true,
                                    userid = ""

                                )
                            }
                            else -> {
                                request = ChatCompletionRequest(
                                    messages = contentMessagesList,
                                    model = "$mModelType-r1-fusion",
                                    stream = true,
                                    `web-search` = false,
                                    userid = ""
                                )
                            }
                        }
                    } else {
                        when {
                            isNetWorkThink -> {
                                request = ChatCompletionRequest(
                                    messages = contentMessagesList,
                                    model = "$mModelType-web-search",
                                    stream = true,
                                    `web-search` = true,
                                    userid = ""

                                )
                            }
                            else -> {
                                request = ChatCompletionRequest(
                                    messages = contentMessagesList,
                                    model = "$mModelType",
                                    stream = true,
                                    `web-search` = false,
                                    userid = ""

                                )
                            }
                        }
                    }
                }else{
                    // 从索引1开始遍历（跳过第一个元素）
                    for (i in 1 until messagesList.size) {
                        val content = messagesList[i]
                        if (content.contains("file:///android_asset/loading.html") || content.contains("这是删除过的内容变为空白")) {
                            continue // 跳过该元素
                        }
                        val role = if (i % 2 == 0) "assistant" else "user"
                        contentMessagesList.add(RequestMessage(role, content))
                    }
                    if (isDeepThink) {
                        when {
                            isNetWorkThink -> {
                                request = ChatCompletionRequest(
                                    messages = contentMessagesList,
                                    model = "$mModelType-r1-fusion",
                                    stream = true,
                                    `web-search` = true,
                                    userid = ""

                                )
                            }
                            else -> {
                                request = ChatCompletionRequest(
                                    messages = contentMessagesList,
                                    model = "$mModelType-r1-fusion",
                                    stream = true,
                                    `web-search` = false,
                                    userid = ""
                                )
                            }
                        }
                    } else {
                        when {
                            isNetWorkThink -> {
                                request = ChatCompletionRequest(
                                    messages = contentMessagesList,
                                    model = "$mModelType-web-search",
                                    stream = true,
                                    `web-search` = true,
                                    userid = ""

                                )
                            }
                            else -> {
                                request = ChatCompletionRequest(
                                    messages = contentMessagesList,
                                    model = "$mModelType",
                                    stream = true,
                                    `web-search` = false,
                                    userid = ""

                                )
                            }
                        }
                    }
                }

            }
        }


        try {
            Log.e("ceshi","发送数据$request")
            processChatStream(
                apiService = apiService,
                requestBody = request,
                authorization = authorizationToken,
                onData = { data ->
                    try {
                        val response = Gson().fromJson(data, StreamResponse::class.java)
                        val content = response.choices.firstOrNull()?.delta?.content ?: ""
                        val deepThinkAssistantMessage = response.choices.firstOrNull()?.delta?.reasoning_content ?: ""
                        val citations = response.citations
                        val role = response.choices.firstOrNull()?.delta?.role ?: ""
                        Log.e("ceshi","0返回数据网络地址：${citations}")
                        Log.e("ceshi","数据返回聊天$content")
                        Log.e("ceshi","数据返回聊天role$role")
                        Log.e("ceshi","数据返回深度思考$deepThinkAssistantMessage")
                        mContent.append(content)
                        mDeepThinkAssistantMessage.append(deepThinkAssistantMessage)
                        //chattextView.append(content)
                        if (citations != null){
                            mCitations = citations.toMutableList()
                        }

                        if (content=="" && role=="assistant"){
                            //开始
                        }else if (content=="" && role=="" && deepThinkAssistantMessage==""){
                            /*viewModelScope.launch(Dispatchers.Main) {
                                if (isNetWorkThink){
                                    if (mDeepThinkAssistantMessage.isNullOrEmpty()){
                                        questionResult.postValue("${mContent}${convertUrlsToMarkdown(citations)}")
                                    }else{
                                        questionResult.postValue("${mDeepThinkAssistantMessage}&&&&&&${mContent}${convertUrlsToMarkdown(citations)}")
                                    }
                                }else{
                                    if (mDeepThinkAssistantMessage.isNullOrEmpty()){
                                        questionResult.postValue("${mContent}")
                                    }else{
                                        questionResult.postValue("${mDeepThinkAssistantMessage}&&&&&&${mContent}")
                                    }
                                }
                                Log.e("ceshi","整个返回内容:$mContent")
                                Log.e("ceshi","整个返回内容深度:$mDeepThinkAssistantMessage")
                                mContent.clear()
                                mDeepThinkAssistantMessage.clear()
                            }*/

                        }else{
//                            mContent.append(content)
//                            mDeepThinkAssistantMessage.append(deepThinkAssistantMessage)
                        }


                    } catch (e: Exception) {
                        Log.e("SSE", "解析错误: $data", e)
                        //questionResult.postValue("解析错误")
                    }

                },
                onError = { error ->
                    viewModelScope.launch(Dispatchers.Main) {
                        //Toast.makeText(context, "网络错误：${error.message}", Toast.LENGTH_SHORT).show()
                        //questionResult.postValue("网络错误，请稍后再试谢谢")
                    }
                },
                onComplete = {
                    viewModelScope.launch(Dispatchers.Main) {
                        //Toast.makeText(context, "流式响应结束", Toast.LENGTH_SHORT).show()
                        Log.e("ceshi","流式响应结束${mContent.length == 0}")
                        if (mContent.length == 0){
                            //questionResult.postValue("网络错误，请稍后再试谢谢")
                        }else{
                            if (isNetWorkThink){
                                if (mDeepThinkAssistantMessage.isNullOrEmpty()){
                                    questionResult.postValue("${mContent}${convertUrlsToMarkdown(mCitations)}")
                                }else{
                                    questionResult.postValue("${mDeepThinkAssistantMessage}&&&&&&${mContent}${convertUrlsToMarkdown(mCitations)}")
                                }
                            }else{
                                if (mDeepThinkAssistantMessage.isNullOrEmpty()){
                                    questionResult.postValue("${mContent}")
                                }else{
                                    questionResult.postValue("${mDeepThinkAssistantMessage}&&&&&&${mContent}")
                                }
                            }
                        }

                        Log.e("ceshi","整个返回内容:$mContent")
                        Log.e("ceshi","整个返回内容深度:$mDeepThinkAssistantMessage")
                        if (mContent.isEmpty()){
                            questionResult.postValue("网络请求错误，请重试")
                        }
                        mContent.clear()
                        mDeepThinkAssistantMessage.clear()
                    }
                }
            )

        }catch (e: SocketTimeoutException) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "网络请求超时，请重试", Toast.LENGTH_SHORT).show()
                questionResult.postValue("网络请求超时，请重试")
                //questionResult.value = "网络请求超时，请重试"
            }
        }
        catch (e: HttpException) {
            // 处理 HTTP 错误
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "正在网络请求，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            // 处理网络错误
        } catch (e: retrofit2.HttpException){
            Log.e("ceshi","错误：${e.toString()}")
            viewModelScope.launch(Dispatchers.Main) {
                //Toast.makeText(context, "当前无可用通道，更多请访问 302.AI", Toast.LENGTH_SHORT).show()
                questionResult.postValue("当前无可用通道，更多请访问 302.AI")
                //questionResult.value = "网络请求超时，请重试"
            }
        }







        /*try {
            val response = apiService.processChatStream(
                authorization = authorizationToken,
                requestBody = request
            )
            // 处理返回的响应数据
            val assistantMessage = response.choices.firstOrNull()?.message?.content
            val deepThinkAssistantMessage = response.choices.firstOrNull()?.message?.reasoning_content
            val citations = response.citations
            Log.e("ceshi","0返回数据：${citations}")
            viewModelScope.launch(Dispatchers.Main) {
                // 在主线程更新 UI
                if (assistantMessage != null) {
                    // 可以在这里更新 UI 显示结果
                    Log.e("ceshi","深度思考返回数据:$deepThinkAssistantMessage")
                    Log.e("ceshi","返回数据：${assistantMessage}")
                    try {
                        //questionResult.postValue(deepThinkAssistantMessage+assistantMessage)
                        //questionResult.value = deepThinkAssistantMessage+"&&&"+assistantMessage
                        // 创建一个 StringBuilder 用于拼接字符串
                        val stringBuilder = StringBuilder()
                        var resultUrl = ""
                        val job = viewModelScope.launch(Dispatchers.IO) {


                            stringBuilder.append("<br>")
                            // 遍历数组元素
                            if (citations != null){
                                for (element in citations) {
                                    // 在每个元素后面添加换行符
                                    //stringBuilder.append(element).append("<br>")
//                                stringBuilder.append("<a href=\"$element\" style=\"color: blue; text-decoration: underline;\">\n" +
//                                        "                                $element\n" +
//                                        "                                </a>").append("<br>")

                                    stringBuilder.append("<span style=\"color: blue; text-decoration: underline;\">\n" +
                                            "  $element\n" +
                                            "</span>").append("<br>")
                                }

                                // 将 StringBuilder 转换为字符串
                                resultUrl = stringBuilder.toString()
                                Log.e("ceshi","网址：$resultUrl")
                            }

                        }
                        job.join()
                        // 统一使用 postValue
                        if (isNetWorkThink){
                            if (deepThinkAssistantMessage.isNullOrEmpty()){
                                questionResult.postValue("${assistantMessage}${convertUrlsToMarkdown(citations)}")
                            }else{
                                questionResult.postValue("${deepThinkAssistantMessage}&&&&&&${assistantMessage}${convertUrlsToMarkdown(citations)}")
                            }
                        }else{
                            if (deepThinkAssistantMessage.isNullOrEmpty()){
                                questionResult.postValue("${assistantMessage}")
                            }else{
                                questionResult.postValue("${deepThinkAssistantMessage}&&&&&&${assistantMessage}")
                            }
                        }


                    }catch(t: Throwable) {
                        Log.e("ChatViewModel", "更新 questionResult 时出现异常: ${t.message}")
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "更新 questionResult 时出现异常: ${e.message}")
                    }
                    Log.e("ceshi","1返回数据：${questionResult.value}")
                }
            }
        }catch (e: SocketTimeoutException) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "网络请求超时，请重试", Toast.LENGTH_SHORT).show()
                questionResult.postValue("网络请求超时，请重试")
                //questionResult.value = "网络请求超时，请重试"
            }
        }
        catch (e: HttpException) {
            // 处理 HTTP 错误
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "正在网络请求，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            // 处理网络错误
        } catch (e: retrofit2.HttpException){
            Log.e("ceshi","错误：${e.toString()}")
            viewModelScope.launch(Dispatchers.Main) {
                //Toast.makeText(context, "当前无可用通道，更多请访问 302.AI", Toast.LENGTH_SHORT).show()
                questionResult.postValue("当前无可用通道，更多请访问 302.AI")
                //questionResult.value = "网络请求超时，请重试"
            }
        }*/
    }


}