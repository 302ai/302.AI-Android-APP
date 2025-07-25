package com.ai302.app.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ai302.app.ChatItem


/**
 * author :
 * e-mail :
 * time   : 2025/4/15
 * desc   :
 * version: 1.0
 */
@Dao
interface ChatDao {
    // 插入时若 title 冲突，删除旧记录并插入新记录
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChat(chat: ChatItemRoom)

    @Query("SELECT * FROM chat_items")
    fun getAllChats(): List<ChatItemRoom>

    @Query("SELECT EXISTS(SELECT 1 FROM chat_items WHERE title = :title)")
    fun checkTitleExists(title: String): Boolean

    @Query("SELECT * FROM chat_items WHERE title = :title LIMIT 1")
    fun getChatByTitle(title: String): ChatItemRoom?

    @Query("SELECT * FROM chat_items WHERE messages LIKE '%' || :searchString || '%'")
    fun getChatsWithMessageContaining(searchString: String): List<ChatItemRoom>

    @Query("SELECT * FROM chat_items WHERE title LIKE '%' || :searchString || '%'")
    fun getChatsWithTitleContaining(searchString: String): List<ChatItemRoom>

    @Query("DELETE FROM chat_items WHERE title = :title")
    fun deleteChatByTitle(title: String)

    // 返回 LiveData，自动监听数据变化
    @Query("SELECT * FROM chat_items")
    fun getAllChatsLiveData(): LiveData<List<ChatItemRoom>>

    // 查询最后一条记录（按主键倒序排列，取第一条）
    @Query("SELECT * FROM chat_items ORDER BY id DESC LIMIT 1")
    fun getLastChatItem(): ChatItemRoom?

    // 查询第一条记录（按主键升序排列，取第一条）
    @Query("SELECT * FROM chat_items ORDER BY id ASC LIMIT 1")
    fun getFirstChatItem(): ChatItemRoom?

    @Update
    fun updateChat(chat: ChatItemRoom)

}