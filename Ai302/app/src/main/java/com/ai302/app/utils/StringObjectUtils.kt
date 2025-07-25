package com.ai302.app.utils

import android.graphics.Color
import android.text.style.URLSpan
import android.util.Log
import io.noties.markwon.linkify.LinkifyPlugin
import java.util.regex.Pattern

/**
 * author :
 * e-mail :
 * time   : 2025/5/23
 * desc   :
 * version: 1.0
 */
object StringObjectUtils {
    /**
     * 从包含HTML代码块的字符串中提取HTML内容
     * @param source 原始字符串（格式示例："前缀：```html...```后缀"）
     * @return 提取到的HTML内容（无匹配时返回空字符串）
     */
    fun extractHtmlFromMarkdown(source: String): String {
        // 定义Markdown代码块的起始和结束标记
        var startTag = "```html"
        if (source.contains("html")){
            startTag = "```html"
        }else if (source.contains("python")){
            startTag = "```python"
        }else if (source.contains("java"))
            startTag = "```java"
        else {
            startTag = "```"
        }
        val endTag = "```"

        // 查找起始标记的位置（从0开始搜索）
        val startIndex = source.indexOf(startTag)
        if (startIndex == -1) {
            return "" // 无起始标记，直接返回空
        }

        // 计算HTML内容的起始位置（起始标记结束后的位置）
        val htmlStart = startIndex + startTag.length

        // 查找结束标记的位置（从htmlStart之后开始搜索）
        val endIndex = source.indexOf(endTag, startIndex = htmlStart)
        if (endIndex == -1) {
            return "" // 无结束标记，返回空
        }

        // 截取HTML内容（注意endIndex是结束标记的起始位置，因此结束位置是endIndex）
        return source.substring(htmlStart, endIndex)
    }

    fun extractHtmlFromMarkdownCode1(source: String): String {
        // 提取所有可能的代码块
        val codeBlockRegex = Regex("```(\\w+)?\\s*([\\s\\S]*?)\\s*```")
        val matches = codeBlockRegex.findAll(source)

        // 查找第一个匹配的代码块（优先按语言类型）
        for (match in matches) {
            val language = match.groupValues[1]?.lowercase() ?: ""
            val codeContent = match.groupValues[2]

            // 如果找到了目标语言的代码块，返回其内容
            if (language == "python" || language == "html" || language == "java") {
                return codeContent
            }

            // 如果没有指定语言，但代码块存在，也返回
            if (language.isEmpty() && codeContent.isNotEmpty()) {
                return codeContent
            }
        }

        return "" // 没有找到任何代码块
    }

    fun extractHtmlFromMarkdownCode(source: String): String {
        // 定义Markdown代码块的正则表达式，包含开始和结束标记
        val codeBlockRegex = Regex("```(\\w+)?[\\s\\S]*?```")

        // 查找第一个匹配的代码块
        val matchResult = codeBlockRegex.find(source)

        // 如果找到匹配项，则返回完整的代码块（包括开始和结束标记）
        return matchResult?.value ?: ""
    }


    /**
     * 从字符串中提取```html代码块内容
     * @param source 原始字符串（格式示例："前缀：```html...```后缀"）
     * @return 提取到的HTML内容（无匹配时返回空字符串）
     */
    fun extractHtml(source: String): String {
        // 定义代码块的起始和结束标记
        var startMarker = "```html"
        if (source.contains("html")){
            startMarker = "```html"
        }else{
            startMarker = "```"
        }
        val endMarker = "```"

        // 查找起始标记的位置（注意：起始标记可能包含换行，这里假设紧接内容）
        val startIndex = source.indexOf(startMarker)
        if (startIndex == -1) return ""  // 无起始标记

        // 计算HTML内容的起始位置（跳过起始标记）
        val contentStart = startIndex + startMarker.length

        // 查找结束标记的位置（从内容起始位置后开始搜索）
        val endIndex = source.indexOf(endMarker, startIndex = contentStart)
        if (endIndex == -1) return ""  // 无结束标记

        // 截取并返回HTML内容（注意：endIndex是结束标记的起始位置）
        return "$startMarker${source.substring(contentStart, endIndex)}$endMarker"
    }

    /**
     * 自动识别并转换公式格式
     * 将 (公式) 转换为 $$公式$$
     */
    fun processFormulas1(text: String): String {
        // 使用正则表达式匹配所有被括号包裹的公式
        val regex = """\(([^()]+)\)""".toRegex()

        // 替换匹配的公式
        return regex.replace(text) { matchResult ->
            val formula = matchResult.groupValues[1]
            // 转换为块级公式格式
            "\n\n$$$formula$$\n\n"
        }
    }

    fun convertLatexFormat(text: String): String {
        // 将 \(...\) 转换为 $...$
        return text.replace(Regex("\\\\\\((.*?)\\\\\\)|\\\\\\[(.*?)\\\\\\]|\\\\[(.*?)\\\\]|\\\\[\\\\s*([^\\\\]](.*?)\\\\s*\\\\]|\\\$(.*?)\\\$")) { match ->
            // 获取第一个捕获组（\(...\)）或第二个捕获组（\[...\]）的内容
            val formula = match.groupValues[0].takeIf { it.isNotEmpty() }
                ?: match.groupValues[1].takeIf { it.isNotEmpty() }
                ?: match.groupValues[2].takeIf { it.isNotEmpty() }
                ?: match.groupValues[3].takeIf { it.isNotEmpty() }
                ?: match.groupValues[4]
            if (formula.contains("\n")) {
                "$$${formula}$$"  // 包含换行符的转换为块级公式
            }else if (match.groupValues[4].isNotEmpty()){
                "$${formula}$"
            }
            else {
                "$$${formula}$$"  // 行内公式
            }
        }
    }


    /**
     * 自动识别并转换公式格式
     * 支持两种格式：\(...\) 和 (...)
     */
    fun processFormulas(text: String): String {
        // 处理 \(...\) 格式的公式
        val processed1 = processBackslashFormulas(text)
        // 处理 (...) 格式的公式
        return processParenthesisFormulas(processed1)
    }

    /**
     * 处理 \(...\) 格式的公式
     */
    private fun processBackslashFormulas(text: String): String {
        val regex = """\\\((.*?)\\\)""".toRegex()
        return regex.replace(text) { matchResult ->
            val formula = matchResult.groupValues[1]
            // 转换为行内公式格式
            "\$$formula\$"
        }
    }

    /**
     * 处理 (...) 格式的公式
     */
    private fun processParenthesisFormulas(text: String): String {
        val regex = """\(([^()]+)\)""".toRegex()
        return regex.replace(text) { matchResult ->
            val formula = matchResult.groupValues[1]
            // 转换为行内公式格式
            "\$$formula\$"
        }
    }


    //正则表达式提取字符串中的URL地址
    fun extractAllUrls(text: String): List<String> {
        val regex = Regex("https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
        return regex.findAll(text).map { it.value }.toList()
    }

    fun extractUrl(text: String): String? {
        // 通用URL正则表达式（简化版，实际使用可优化）
        val regex = Regex("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
        return regex.find(text)?.value
    }

    // 创建更宽松的URL识别模式
    private val URL_PATTERN = Pattern.compile(
        """
        (?:(?:https?|ftp)://)?                      # 协议部分（可选）
        (?:www\.|[-a-zA-Z0-9@:%._+~#=]{2,256}\.)  # www. 或域名前缀
        [-a-zA-Z0-9@:%._+~#=]{1,256}               # 域名主体
        (?:/[-a-zA-Z0-9@:%_+.~#?&/=]*)?            # 路径部分（可选）
        """.trimIndent(), Pattern.CASE_INSENSITIVE
    )







}
// 创建自定义URLSpan，移除下划线
class CustomUrlSpan(url: String) : URLSpan(url) {
    override fun updateDrawState(ds: android.text.TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = true // 移除下划线
        ds.color = Color.BLUE // 使用默认链接颜色，或自定义颜色
    }



    object UrlValidator {
        // 验证URL格式的正则表达式（支持http/https，域名，路径等）
        private val URL_PATTERN = "^(https?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(:[0-9]{1,5})?(/.*)?$".toRegex()

        // 验证是否为302.ai相关的合法URL
        fun isValid302Url(url: String): Boolean {
            // 1. 验证URL格式
            if (!URL_PATTERN.matches(url)) {
                return false
            }
            // 2. 验证域名是否包含api.302.ai
            //return url.contains("api.302.ai", ignoreCase = true)
            return true
        }
    }


}