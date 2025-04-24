package com.example.expirease.data

import android.icu.text.SimpleDateFormat
import android.os.Parcel
import android.os.Parcelable
import com.example.expirease.R
import java.util.Date
import java.util.Locale

enum class ItemStatus {
    ACTIVE,
    CONSUMED,
    EXPIRED,
    DELETED
}

data class Item(
    var name: String = "",
    var quantity: Int = 0,
    var status :ItemStatus = ItemStatus.ACTIVE,
    var expiryDate: Long = 0L, // ✅ Add default value
    var category: Category = Category.OTHER,
    var photoResource: Int = R.drawable.img_product_banana
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", //name
        parcel.readInt(), //quantity
            ItemStatus.valueOf(parcel.readString() ?: ItemStatus.ACTIVE.name), // status
        parcel.readLong(), //expiryDate
        Category.valueOf(parcel.readString() ?: Category.OTHER.name),
        parcel.readInt()//photo
    )

    val expiryDateString: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expiryDate))


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(quantity)
        parcel.writeString(status.name)
        parcel.writeLong(expiryDate)
        parcel.writeString(category.name)
        parcel.writeInt(photoResource)
    }

    override fun describeContents(): Int = 0

    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "quantity" to quantity,
                "status" to status.name,
            "expiryDate" to expiryDate,
            "category" to category.name,
            "photoResource" to photoResource
        )
    }

    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item = Item(parcel)
        override fun newArray(size: Int): Array<Item?> = arrayOfNulls(size)
    }
}
