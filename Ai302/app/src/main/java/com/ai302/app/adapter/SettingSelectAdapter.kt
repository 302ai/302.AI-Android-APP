package com.ai302.app.adapter

import android.text.TextWatcher
import android.widget.TextView
import com.ai302.app.R
import com.ai302.app.databinding.ItemSettingSelectBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
//import kotlinx.android.synthetic.main.item_setting_select.view.*

/**
 * @ClassName:      SettingSelectAdapter$
 * @Description:     java类作用描述
 * @Author:         Lee
 * @CreateDate:     2025/3/25 0010$
 * @UpdateUser:     更新者：
 * @UpdateDate:
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */
class SettingSelectAdapter :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_setting_select) {
    private lateinit var tv_select: TextView

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemSettingSelectBinding.inflate(android.view.LayoutInflater.from(parent.context), parent, false)
        tv_select = binding.tvSelect
        return BaseViewHolder(binding.root)
    }

    override fun convert(helper: BaseViewHolder, item: String) {
        helper.itemView.apply {
            tv_select.text = item
        }
    }

}