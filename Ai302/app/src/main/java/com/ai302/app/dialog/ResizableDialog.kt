import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity

import com.ai302.app.R

@SuppressLint("MissingInflatedId")
class ResizableDialog(context: Context) : Dialog(context) {

    private lateinit var expandButton: ImageView
    private lateinit var shrinkButton: ImageView
    private lateinit var closeButton: ImageView
    private lateinit var dialogContainer: ConstraintLayout

    private var isExpanded = false
    private var originalWidth: Int = 0
    private var originalHeight: Int = 0

    init {
        setContentView(R.layout.dialog_layout)

        val textView = findViewById<TextView>(R.id.dialogName)
        val fullText = "1.此聊天机器人由302.AI用户xingWei创建，302.AI是一个生成和分享属于自己的AI的平台，可以一键生成和分享属于自己的AI工具。"
        val spannableString = SpannableString(fullText)
        // 找到特定文字的起始和结束位置
        val startIndex = fullText.indexOf("xingWei")
        val endIndex = startIndex + "xingWei".length

        // 设置特定文字的颜色
        val colorSpan = ForegroundColorSpan(Color.BLACK)
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // 设置特定文字的大小
        val sizeSpan = RelativeSizeSpan(1.2f)
        spannableString.setSpan(sizeSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // 设置特定文字的点击事件
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // 处理点击事件
                //android.widget.Toast.makeText(this@MainActivity, "你点击了特定文字", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // 让 TextView 可点击
        textView.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        textView.text = spannableString

        //官网地址
        val textView1 = findViewById<TextView>(R.id.dialogUrl)
        val fullText1 = "4.更多信息请访问：302.AI"
        val spannableString1 = SpannableString(fullText1)
        // 找到特定文字的起始和结束位置
        val startIndex1 = fullText1.indexOf("302.AI")
        val endIndex1 = startIndex1 + "302.AI".length

        // 设置特定文字的颜色
        val colorSpan1 = ForegroundColorSpan(Color.BLUE)
        spannableString1.setSpan(colorSpan1, startIndex1, endIndex1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // 设置特定文字的大小
        val sizeSpan1 = RelativeSizeSpan(1.2f)
        spannableString1.setSpan(sizeSpan1, startIndex1, endIndex1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // 设置特定文字的点击事件
        val clickableSpan1 = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // 处理点击事件
                android.widget.Toast.makeText(context, "你点击了官网地址", android.widget.Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://302.ai/") // 设置要跳转的网址
                context.startActivity(intent) // 启动活动
            }
        }
        spannableString1.setSpan(clickableSpan1, startIndex1, endIndex1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // 让 TextView 可点击
        textView1.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        textView1.text = spannableString1


        expandButton = findViewById(R.id.expandButton)
        shrinkButton = findViewById(R.id.shrinkButton)
        closeButton = findViewById(R.id.dialogClose)
        dialogContainer = findViewById(R.id.dialogContainer)

        expandButton.setOnClickListener {
            expandDialog()
        }

        shrinkButton.setOnClickListener {
            shrinkDialog()
        }

        closeButton.setOnClickListener {
            dismiss()
        }
    }

    fun showDialog() {
        show()
    }

    private fun expandDialog() {
        if (!isExpanded) {
            // 记录原始尺寸（改为记录 Dialog 的尺寸）
            originalWidth = window?.attributes?.width ?: ViewGroup.LayoutParams.WRAP_CONTENT
            originalHeight = window?.attributes?.height ?: ViewGroup.LayoutParams.WRAP_CONTENT

            // 修改窗口尺寸为全屏
            val params = window?.attributes
            params?.width = ViewGroup.LayoutParams.MATCH_PARENT
            params?.height = ViewGroup.LayoutParams.MATCH_PARENT
            window?.attributes = params

            expandButton.visibility = View.GONE
            shrinkButton.visibility = View.VISIBLE
            isExpanded = true
        }
    }

    private fun shrinkDialog() {
        if (isExpanded) {
            // 恢复原始窗口尺寸
            val params = window?.attributes
            params?.width = originalWidth
            params?.height = originalHeight
            window?.attributes = params

            expandButton.visibility = View.VISIBLE
            shrinkButton.visibility = View.GONE
            isExpanded = false
        }
    }
}