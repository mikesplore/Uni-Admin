package com.mike.uniadmin.model.userchat

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mike.uniadmin.model.users.UserEntity

@Entity(tableName = "userChats")
data class UserChatEntity(
    @PrimaryKey var id: String,
    var message: String = "",
    var senderID: String = "",
    var timeStamp: String = "",
    var date: String = "",
    var recipientID: String = "",
    var path: String = "",
    var deliveryStatus: DeliveryStatus = DeliveryStatus.SENT,

    ) {
    constructor() : this("", "", "", "", "", "", "", DeliveryStatus.SENT)
}


data class UserChatsWithDetails(
    @Embedded val userChat: UserChatEntity,
    @Embedded(prefix = "sender_") val sender: UserEntity,
    @Embedded(prefix = "receiver_") val receiver: UserEntity,
    val senderState: String = "",
    val receiverState: String = "",
    val unreadCount: Int = 0
) {
    constructor() : this(UserChatEntity(), UserEntity(), UserEntity(), "", "", 0)
}


enum class DeliveryStatus {
    SENT,
    READ,

}