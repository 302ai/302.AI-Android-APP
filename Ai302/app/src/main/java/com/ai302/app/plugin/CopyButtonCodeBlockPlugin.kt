package com.ai302.app.plugin

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.widget.TextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.core.MarkwonTheme
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Node

class CodeCopyPlugin(private val textView: TextView) : AbstractMarkwonPlugin() {

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(FencedCodeBlock::class.java, object : MarkwonVisitor.NodeVisitor<FencedCodeBlock> {
            override fun visit(visitor: MarkwonVisitor, node: FencedCodeBlock) {
                // 先渲染原始代码块
                visitor.visitChildren(node)

                // 获取代码内容
                val code = node.literal

                // 创建可点击的复制按钮
                val theme = visitor.configuration().theme()
                val copySpan = CodeCopySpan(theme, code, textView)

                // 将复制按钮添加到文本末尾
                val builder = SpannableStringBuilder()
                builder.append("\uFFFC") // 对象替换字符
                builder.setSpan(
                    copySpan,
                    builder.length - 1,
                    builder.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                visitor.builder().append(builder)
            }
        })
    }
}

