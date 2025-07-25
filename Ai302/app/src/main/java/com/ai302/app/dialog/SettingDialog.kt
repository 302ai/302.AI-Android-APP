import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ai302.app.R
import com.ai302.app.datastore.DataStoreManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import androidx.lifecycle.lifecycleScope
import com.ai302.app.MyApplication.Companion.myApplicationContext
import com.ai302.app.infa.OnSettingDialogClickListener
import com.ai302.app.utils.CustomUrlSpan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("MissingInflatedId")
class SettingDialog(context: Context, private val listener: OnSettingDialogClickListener) : Dialog(context) {

    private lateinit var serviceSelect: LinearLayout
    private lateinit var modelSelect: LinearLayout
    private lateinit var closeButton: ImageView
    private lateinit var dialogContainer: ConstraintLayout
    private lateinit var editApiKey: EditText
    private lateinit var editCustomizeApiKey: EditText
    private lateinit var editOpenAiApiKey: EditText
    private lateinit var editAnthropicApiKey: EditText

    private lateinit var editApiService: EditText
    private lateinit var serviceCustomizeEdit: EditText


    private lateinit var editModelId: EditText
    private lateinit var serviceText: TextView

    private lateinit var getApiKey: TextView

    private lateinit var dataStoreManager: DataStoreManager

    private lateinit var modelSelectCon: ConstraintLayout
    private lateinit var modelIdCon: ConstraintLayout

    private lateinit var modelText: TextView

    private lateinit var buttonTestApiKey:Button

    private var isFirst = true
    private var originalWidth: Int = 0
    private var originalHeight: Int = 0

    private lateinit var popupWindow: PopupWindow
    private lateinit var popupWindow1: PopupWindow
    private val options1 = mutableListOf("302.AI","OpenAI","Anthropic","自定义")
    private var options = mutableListOf("OpenAI模型")
    private var mApikey = ""
    private var mCustomizeApikey = ""
    private var mOpenAiApikey = ""
    private var mAnthropicApikey = ""
    private var modelURL = "https://dash.302.ai/login"
    private var isChange = false
    private var serviceProvider = "302.AI"
    private var serviceUrl = ""
    private var serviceCustomizeUrl = ""
    private var modelId = ""
    private var positionModeType = 0

    private lateinit var apiKeyLine: LinearLayout
    private lateinit var apiKeyCustomizeLine: LinearLayout

    private lateinit var apiKeyOpenAiLine: LinearLayout
    private lateinit var apiKeyAnthropicLine: LinearLayout



    private lateinit var serviceEditLine: LinearLayout
    private lateinit var serviceCustomizeEditLine: LinearLayout

    init {
        setContentView(R.layout.dialog_setting_layout)



        dataStoreManager = DataStoreManager(myApplicationContext)

        serviceSelect = findViewById(R.id.serviceSelect)
        modelSelect = findViewById(R.id.modelSelect)
        closeButton = findViewById(R.id.dialogSettingClose)
        dialogContainer = findViewById(R.id.dialogContainer)
        editApiKey = findViewById(R.id.edit_apiKey)
        editCustomizeApiKey = findViewById(R.id.edit_customize_apiKey)
        editOpenAiApiKey = findViewById(R.id.edit_openAi_apiKey)
        editAnthropicApiKey = findViewById(R.id.edit_Anthropic_apiKey)
        editApiService = findViewById(R.id.serviceEdit)
        serviceCustomizeEdit = findViewById(R.id.serviceCustomizeEdit)

        getApiKey = findViewById(R.id.text21)
        serviceText = findViewById(R.id.serviceText)
        modelSelectCon = findViewById(R.id.modelSelectCon)
        modelIdCon = findViewById(R.id.modelIdCon)
        modelText = findViewById(R.id.modelText)
        buttonTestApiKey = findViewById(R.id.buttonTestApiKey)
        editModelId = findViewById(R.id.edit_modelID)

        apiKeyLine = findViewById(R.id.apiKeyLine)
        apiKeyCustomizeLine = findViewById(R.id.apiKeyCustomizeLine)
        apiKeyOpenAiLine = findViewById(R.id.apiKeyOpenAiLine)
        apiKeyAnthropicLine = findViewById(R.id.apiKeyAnthropicLine)

        serviceEditLine = findViewById(R.id.serviceEditLine)
        serviceCustomizeEditLine = findViewById(R.id.serviceCustomizeEditLine)

        CoroutineScope(Dispatchers.IO).launch {
            serviceProvider = dataStoreManager.readServiceProviderData.first()?:"302.AI"
            serviceUrl = dataStoreManager.readServiceUrl.first()?:""
            modelId = dataStoreManager.readCustomizeModelIdData.first()?:""
            serviceCustomizeUrl = dataStoreManager.readCustomizeServiceUrlData.first()?:""

            CoroutineScope(Dispatchers.Main).launch {
                serviceText.text = serviceProvider

                if (serviceText.text.equals("302.AI")){
                    //isChange = false
                    editApiService.setText("https://api.302.ai/")
                    modelURL = "https://dash.302.ai/login"
                    apiKeyLine.visibility = View.VISIBLE
                    apiKeyCustomizeLine.visibility = View.GONE
                    apiKeyAnthropicLine.visibility = View.GONE
                    apiKeyOpenAiLine.visibility = View.GONE

                    serviceEditLine.visibility = View.VISIBLE
                    serviceCustomizeEditLine.visibility = View.GONE
                }else if (serviceText.text.equals("OpenAI")){
                    //isChange = true
                    editApiService.setText("api.openai.com/")
                    modelURL = "https://api.openai.com/login"
                    apiKeyLine.visibility = View.GONE
                    apiKeyCustomizeLine.visibility = View.GONE
                    apiKeyAnthropicLine.visibility = View.GONE
                    apiKeyOpenAiLine.visibility = View.VISIBLE


                    serviceEditLine.visibility = View.VISIBLE
                    serviceCustomizeEditLine.visibility = View.GONE
                }else if (serviceText.text.equals("claude")){
                    //isChange = true
                    editApiService.setText("api.anthropic.com/")
                    modelURL = "https://api.anthropic.com/login"
                    apiKeyLine.visibility = View.VISIBLE
                    apiKeyCustomizeLine.visibility = View.GONE
                    serviceEditLine.visibility = View.VISIBLE
                    serviceCustomizeEditLine.visibility = View.GONE
                }else if (serviceText.text.equals("Anthropic")){
                    //isChange = true
                    editApiService.setText("api.anthropic.com/")
                    modelURL = "https://api.anthropic.com/login"
                    apiKeyLine.visibility = View.GONE
                    apiKeyCustomizeLine.visibility = View.GONE
                    apiKeyAnthropicLine.visibility = View.VISIBLE
                    apiKeyOpenAiLine.visibility = View.GONE

                    serviceEditLine.visibility = View.VISIBLE
                    serviceCustomizeEditLine.visibility = View.GONE
                }else if (serviceText.text.equals("自定义")){
                    serviceCustomizeEdit.setText(serviceCustomizeUrl)
                    editModelId.setText(modelId)
                    //modelIdCon.visibility = View.VISIBLE
                    //modelSelectCon.visibility = View.GONE

                    apiKeyLine.visibility = View.GONE
                    apiKeyCustomizeLine.visibility = View.VISIBLE
                    apiKeyAnthropicLine.visibility = View.GONE
                    apiKeyOpenAiLine.visibility = View.GONE

                    serviceEditLine.visibility = View.GONE
                    serviceCustomizeEditLine.visibility = View.VISIBLE
                }
            }
        }





        serviceSelect.setOnClickListener {
            setupPopupWindow(options1,false)
            showPopup(it)
        }

        modelSelect.setOnClickListener {
            if (options.isEmpty() || options.size < 2){
                /*if (serviceText.text.equals("302.AI")){
                    Toast.makeText(context, "返回模型为空，请检查域名或者apikey并点击检测重试", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "暂未获取模型", Toast.LENGTH_SHORT).show()
                }*/
                Toast.makeText(context, "返回模型为空，请检查域名或者apikey并点击检测重试", Toast.LENGTH_SHORT).show()
            }else{
                setupPopupWindow(options,true)
                showPopup(it)
            }

        }

        closeButton.setOnClickListener {
            dismiss()
        }

        getApiKey.setOnClickListener {
            if (serviceText.text.equals("302.AI")){
                modelURL = "https://dash.302.ai/sso/login?app=302+AI+Studio&name=302+AI+Studio&icon=https://file.302.ai/gpt/imgs/5b36b96aaa052387fb3ccec2a063fe1e.png&weburl=https://302.ai/&redirecturl=https://dash.302.ai/dashboard/overview&lang=zh-CN"
            }else if (serviceText.text.equals("OpenAI")){
                modelURL = "https://api.openai.com/login"
            }else if (serviceText.text.equals("claude")){
                modelURL = "https://api.anthropic.com/login"
            }else if (serviceText.text.equals("Anthropic")){
                modelURL = "https://api.anthropic.com/login"
            }
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.data = Uri.parse(modelURL) // 设置要跳转的网址
//            context.startActivity(intent) // 启动活动

            listener.onModelTypeClick("login",modelURL)
        }

        editApiKey.setOnClickListener {
            Log.e("ceshi","输入：editApiKey")


        }

        editModelId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                CoroutineScope(Dispatchers.IO).launch {
                    Log.e("ceshi","输入存储自定义id：${s}，，，$isChange")

                    if (serviceProvider == "自定义"){
                        dataStoreManager.saveCustomizeModelIdData(s.toString())
                        modelId = s.toString()
                    }


                }
            }
        })

        editApiService.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                CoroutineScope(Dispatchers.IO).launch {
                    Log.e("ceshi","输入存储服务地址：${s}，，，$isChange,,$serviceProvider")

                    if (serviceProvider == "自定义" && !isChange){

//                        CoroutineScope(Dispatchers.Main).launch {
//                            if (CustomUrlSpan.UrlValidator.isValid302Url(s.toString())) {
//                                Toast.makeText(context, "验证成功，输入的地址格式正确", Toast.LENGTH_LONG).show()
//                            } else {
//                                Toast.makeText(context, "输入错误，请输入有效的地址（例如：${"https://api.302.ai/"}）", Toast.LENGTH_LONG).show()
//                            }
//                        }


                        dataStoreManager.saveServiceUrl(s.toString())
                    }


                }
            }
        })

        serviceCustomizeEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                CoroutineScope(Dispatchers.IO).launch {
                    Log.e("ceshi","输入存储自定义服务地址：${s}，，，$isChange,,$serviceProvider")

//                    CoroutineScope(Dispatchers.Main).launch {
//                        if (CustomUrlSpan.UrlValidator.isValid302Url(s.toString())) {
//                            Toast.makeText(context, "验证成功，输入的地址格式正确", Toast.LENGTH_LONG).show()
//                        } else {
//                            Toast.makeText(context, "输入错误，请输入有效的地址（例如：${"https://api.302.ai/"}）", Toast.LENGTH_LONG).show()
//                        }
//                    }


                    dataStoreManager.saveCustomizeServiceUrlData(s.toString())
                    serviceCustomizeUrl = s.toString()


                }
            }
        })

        editApiKey.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                CoroutineScope(Dispatchers.IO).launch {
                    Log.e("ceshi","输入存储：${s}，，，$isChange")
                    dataStoreManager.saveData(s.toString())


                }
            }
        })

        editCustomizeApiKey.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                CoroutineScope(Dispatchers.IO).launch {
                    Log.e("ceshi","自定义输入存储：${s}，，，$isChange")
//                    if (!isChange && serviceProvider != "自定义"){
//                        dataStoreManager.saveData(s.toString())
//                    }
//
//                    if (serviceProvider == "自定义"){
//                        dataStoreManager.saveCustomizeKeyData(s.toString())
//                    }
                    dataStoreManager.saveCustomizeKeyData(s.toString())


                }
            }
        })

        editOpenAiApiKey.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                CoroutineScope(Dispatchers.IO).launch {
                    Log.e("ceshi","自定义输入存储：${s}，，，$isChange")
//                    if (!isChange && serviceProvider != "自定义"){
//                        dataStoreManager.saveData(s.toString())
//                    }
//
//                    if (serviceProvider == "自定义"){
//                        dataStoreManager.saveCustomizeKeyData(s.toString())
//                    }
                    dataStoreManager.saveOpenAiKeyData(s.toString())


                }
            }
        })

        editAnthropicApiKey.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                CoroutineScope(Dispatchers.IO).launch {
                    Log.e("ceshi","自定义输入存储：${s}，，，$isChange")
//                    if (!isChange && serviceProvider != "自定义"){
//                        dataStoreManager.saveData(s.toString())
//                    }
//
//                    if (serviceProvider == "自定义"){
//                        dataStoreManager.saveCustomizeKeyData(s.toString())
//                    }
                    dataStoreManager.saveAnthropiocKeyData(s.toString())


                }
            }
        })

        // 仅在打开时读取一次数据
//        CoroutineScope(Dispatchers.IO).launch {
//            val data = dataStoreManager.readData.first()
//            data?.let {
//                editApiKey.setText(it)
//            }
//        }

        buttonTestApiKey.setOnClickListener {
            listener.onModelTypeClick("testApiKey",serviceProvider)

            if (serviceProvider == "自定义"){

                CoroutineScope(Dispatchers.Main).launch {
                    if (CustomUrlSpan.UrlValidator.isValid302Url(serviceCustomizeUrl)) {
                        Toast.makeText(context, "验证成功，输入的地址格式正确", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "输入错误，请输入有效的地址（例如：${"https://api.302.ai/"}）", Toast.LENGTH_LONG).show()
                    }
                }

            }
        }




    }

    fun showDialog() {
        isChange = false
        CoroutineScope(Dispatchers.IO).launch {
            val data = dataStoreManager.readData.first()
            val readCustomizeKeyData = dataStoreManager.readCustomizeKeyData.first()?:""
            val readAnthropicKeyData = dataStoreManager.readAnthropicKeyData.first()?:""
            val readOpenAiKeyData = dataStoreManager.readOpenAiKeyData.first()?:""
            Log.e("ceshi","apikey是：$data，，服务商是:$serviceProvider,,$readCustomizeKeyData")
            if (serviceText.text.equals("302.AI")){

                apiKeyLine.visibility = View.VISIBLE
                apiKeyCustomizeLine.visibility = View.GONE
                apiKeyAnthropicLine.visibility = View.GONE
                apiKeyOpenAiLine.visibility = View.GONE

                serviceEditLine.visibility = View.VISIBLE
                serviceCustomizeEditLine.visibility = View.GONE

                getApiKey.visibility = View.VISIBLE

            }else if(serviceText.text.equals("自定义")){

                apiKeyLine.visibility = View.GONE
                apiKeyCustomizeLine.visibility = View.VISIBLE
                apiKeyAnthropicLine.visibility = View.GONE
                apiKeyOpenAiLine.visibility = View.GONE

                serviceEditLine.visibility = View.GONE
                serviceCustomizeEditLine.visibility = View.VISIBLE

                getApiKey.visibility = View.GONE

            }else if (serviceText.text.equals("Anthropic")){
                apiKeyLine.visibility = View.GONE
                apiKeyCustomizeLine.visibility = View.GONE
                apiKeyAnthropicLine.visibility = View.VISIBLE
                apiKeyOpenAiLine.visibility = View.GONE

                getApiKey.visibility = View.VISIBLE

            }else if (serviceText.text.equals("OpenAI")){
                apiKeyLine.visibility = View.GONE
                apiKeyCustomizeLine.visibility = View.GONE
                apiKeyAnthropicLine.visibility = View.GONE
                apiKeyOpenAiLine.visibility = View.VISIBLE

                getApiKey.visibility = View.VISIBLE

            }

            data?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    editApiKey.setText(it)
                    mApikey= it
                }

            }

            CoroutineScope(Dispatchers.Main).launch {
                editCustomizeApiKey.setText(readCustomizeKeyData)
                mCustomizeApikey= readCustomizeKeyData
                mOpenAiApikey = readOpenAiKeyData
                mAnthropicApikey = readAnthropicKeyData
            }


            val readModelType = dataStoreManager.readModelType.first()
            Log.e("ceshi","获取选择模型：$readModelType")
            readModelType?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    if (serviceText.text.equals("302.AI")){
                        modelText.text = it
                    }else{
                        modelText.text = "暂未获取模型"
                    }
                }

            }



        }
        show()
    }


    private fun setupPopupWindow(options0:MutableList<String>,isModel:Boolean) {
        val popupView = LayoutInflater.from(context).inflate(R.layout.setting_popup_list, null)
        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            isOutsideTouchable = true
            animationStyle = android.R.style.Animation_Dialog
        }

        val recyclerView = popupView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.popupRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        if (isModel){
            recyclerView.layoutManager?.scrollToPosition(positionModeType)
        }
        recyclerView.adapter = object : BaseQuickAdapter<String, BaseViewHolder>(
            R.layout.item_popup_select, options0
        ) {
            override fun convert(holder: BaseViewHolder, item: String) {
                holder.setText(R.id.itemTextView, item)
            }
        }.apply {
            setOnItemClickListener { _, _, position ->
                popupWindow.dismiss()
                if (isModel){
                    findViewById<TextView>(R.id.modelText).text = options0[position]
                    positionModeType = position
                    CoroutineScope(Dispatchers.IO).launch {
                        dataStoreManager.saveModelType(options0[position])
                        dataStoreManager.saveIsChangeModelSetting(true)
                    }
                }else{

                    Log.e("ceshi","输入厂家：${options0[position]},$modelId,$mApikey,$mCustomizeApikey,$serviceCustomizeUrl")
                    CoroutineScope(Dispatchers.IO).launch {
                        dataStoreManager.saveServiceProviderData(options0[position])
                    }
                    serviceProvider = options0[position]

                    isChange = true
                    if (options0[position].equals("302.AI")){
                        editApiService.setText("https://api.302.ai/")
                        getApiKey.visibility = View.VISIBLE
//                        modelIdCon.visibility = View.GONE
//                        modelSelectCon.visibility = View.VISIBLE
                        editApiKey.setText(mApikey)

                        apiKeyLine.visibility = View.VISIBLE
                        apiKeyCustomizeLine.visibility = View.GONE
                        apiKeyAnthropicLine.visibility = View.GONE
                        apiKeyOpenAiLine.visibility = View.GONE

                        serviceEditLine.visibility = View.VISIBLE
                        serviceCustomizeEditLine.visibility = View.GONE

                        modelText.text = "gpt-4o"
                        CoroutineScope(Dispatchers.IO).launch {
                            dataStoreManager.saveModelType("gpt-4o")
                            dataStoreManager.saveIsChangeModelSetting(true)
                        }
                        options = mutableListOf("OpenAI模型")

                    }else if (options0[position].equals("OpenAI")){
                        editApiService.setText("https://api.openai.com/")
                        getApiKey.visibility = View.VISIBLE
//                        modelIdCon.visibility = View.GONE
//                        modelSelectCon.visibility = View.VISIBLE
                        //editApiKey.setText("")
                        editOpenAiApiKey.setText(mOpenAiApikey)

                        apiKeyLine.visibility = View.GONE
                        apiKeyCustomizeLine.visibility = View.GONE
                        apiKeyAnthropicLine.visibility = View.GONE
                        apiKeyOpenAiLine.visibility = View.VISIBLE

                        serviceEditLine.visibility = View.VISIBLE
                        serviceCustomizeEditLine.visibility = View.GONE

                        modelText.text = "暂未获取模型"
                        CoroutineScope(Dispatchers.IO).launch {
                            dataStoreManager.saveModelType("gpt-4o")
                            dataStoreManager.saveIsChangeModelSetting(true)
                        }
                        options = mutableListOf("OpenAI模型")
                    }else if (options0[position].equals("自定义")){
                        getApiKey.visibility = View.GONE
//                        modelIdCon.visibility = View.VISIBLE
//                        modelSelectCon.visibility = View.GONE
                        serviceCustomizeEdit.setText(serviceCustomizeUrl)
                        editModelId.setText(modelId)
                        editCustomizeApiKey.setText(mCustomizeApikey)

                        apiKeyLine.visibility = View.GONE
                        apiKeyCustomizeLine.visibility = View.VISIBLE
                        apiKeyAnthropicLine.visibility = View.GONE
                        apiKeyOpenAiLine.visibility = View.GONE

                        serviceEditLine.visibility = View.GONE
                        serviceCustomizeEditLine.visibility = View.VISIBLE

                        modelText.text = "暂未获取模型"
                        CoroutineScope(Dispatchers.IO).launch {
                            dataStoreManager.saveModelType("Qwen/Qwen2.5-7B-Instruct")
                            dataStoreManager.saveIsChangeModelSetting(true)
                        }
                        options = mutableListOf("OpenAI模型")

                    }else if (options0[position].equals("claude")){
                        editApiService.setText("https://api.anthropic.com/")
                        getApiKey.visibility = View.VISIBLE
//                        modelIdCon.visibility = View.GONE
//                        modelSelectCon.visibility = View.VISIBLE
                        editApiKey.setText("")

                        apiKeyLine.visibility = View.GONE
                        apiKeyCustomizeLine.visibility = View.GONE
                        apiKeyAnthropicLine.visibility = View.VISIBLE
                        apiKeyOpenAiLine.visibility = View.GONE

                        serviceEditLine.visibility = View.VISIBLE
                        serviceCustomizeEditLine.visibility = View.GONE

                        modelText.text = "暂未获取模型"
                        CoroutineScope(Dispatchers.IO).launch {
                            dataStoreManager.saveModelType("gpt-4o")
                            dataStoreManager.saveIsChangeModelSetting(true)
                        }
                        options = mutableListOf("OpenAI模型")
                    }else if (options0[position].equals("Anthropic")){
                        editApiService.setText("https://api.anthropic.com/")
                        getApiKey.visibility = View.VISIBLE
//                        modelIdCon.visibility = View.GONE
//                        modelSelectCon.visibility = View.VISIBLE
                        //editApiKey.setText("")
                        editAnthropicApiKey.setText(mAnthropicApikey)

                        apiKeyLine.visibility = View.GONE
                        apiKeyCustomizeLine.visibility = View.GONE
                        apiKeyAnthropicLine.visibility = View.VISIBLE
                        apiKeyOpenAiLine.visibility = View.GONE

                        serviceEditLine.visibility = View.VISIBLE
                        serviceCustomizeEditLine.visibility = View.GONE

                        modelText.text = "暂未获取模型"
                        CoroutineScope(Dispatchers.IO).launch {
                            dataStoreManager.saveModelType("gpt-4o")
                            dataStoreManager.saveIsChangeModelSetting(true)
                        }
                        options = mutableListOf("OpenAI模型")
                    }
                    findViewById<TextView>(R.id.serviceText).text = options0[position]
                }

            }
        }
    }

    private fun showPopup(anchorView: View) {
        popupWindow.showAsDropDown(
            anchorView,
            -(anchorView.width - popupWindow.width) / 2,
            8
        )
    }

    fun setModelList(options:MutableList<String>){
        this.options = options
    }

    override fun onStart() {
        super.onStart()
        Log.e("ceshi","SettingDialog--onStart")
    }


}