package com.ai302.app.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ai302.app.R
import com.ai302.app.adapter.SettingSelectAdapter
import com.ai302.app.databinding.PopupSettingBinding
import com.zyyoona7.popup.BasePopup
//import kotlinx.android.synthetic.main.popup_setting.view.*

/**
 * @ClassName:      DevicePortPopUp$
 * @Description:     java类作用描述
 * @Author:         Lee
 * @CreateDate:     2025 0010$
 * @UpdateUser:     更新者：
 * @UpdateDate:
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */


class SettingSelectPopUp(
    private var context: Context,
    private var w: Int = 240,
    private var h: Int = 300,
    private var selectItem: (item: String) -> Unit
) :
    BasePopup<SettingSelectPopUp>() {

    private val adapter: SettingSelectAdapter by lazy {
        SettingSelectAdapter()
    }

    private lateinit var binding: PopupSettingBinding

    override fun initAttributes() {
        //setContentView(context, R.layout.popup_setting, w, h)
        binding = PopupSettingBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root, w, h)
    }

    override fun initViews(p0: View?, p1: SettingSelectPopUp?) {
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter.setOnItemClickListener { _, _, position ->
            adapter.data[position].let {
                selectItem.invoke(it)
            }
            dismiss()
        }
    }

    fun setData(dataList: ArrayList<String>): SettingSelectPopUp {
        adapter.data = dataList
        adapter.notifyDataSetChanged()
        return this.self()
    }
}