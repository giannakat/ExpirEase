package com.example.expirease.data

import android.os.Parcel
import android.os.Parcelable
import com.example.expirease.R

enum class ItemStatus {
    ACTIVE,
    CONSUMED,
    EXPIRED,
    DELETED
}

data class Item(
    var name: String = "",
    var quantity: Int = 0,
    var status: ItemStatus = ItemStatus.ACTIVE,
    var expiryDate: Long = 0L,
    var categoryId: String = "",
    var photoResource: Int = R.drawable.img_product_banana
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        ItemStatus.valueOf(parcel.readString() ?: ItemStatus.ACTIVE.name),
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(quantity)
        parcel.writeString(status.name)
        parcel.writeLong(expiryDate)
        parcel.writeString(categoryId)
        parcel.writeInt(photoResource)
    }

    override fun describeContents(): Int = 0

    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "quantity" to quantity,
            "status" to status.name,
            "expiryDate" to expiryDate,
            "categoryId" to categoryId,
            "photoResource" to photoResource
        )
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Item> {
            override fun createFromParcel(parcel: Parcel) = Item(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Item>(size)
        }

        fun fromMap(map: Map<String, Any>): Item {
            return Item(
                name = map["name"] as? String ?: "",
                quantity = (map["quantity"] as? Long)?.toInt() ?: (map["quantity"] as? Int ?: 0),
                status = ItemStatus.valueOf(map["status"] as? String ?: ItemStatus.ACTIVE.name),
                expiryDate = map["expiryDate"] as? Long ?: 0L,
                categoryId = map["categoryId"] as? String ?: "",
                photoResource = (map["photoResource"] as? Long)?.toInt() ?: (map["photoResource"] as? Int ?: R.drawable.img_product_banana)
            )
        }
    }
}

