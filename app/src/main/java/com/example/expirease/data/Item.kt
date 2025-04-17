package com.example.expirease.data

import android.os.Parcel
import android.os.Parcelable
import com.example.expirease.R

data class Item(
    var name: String = "",
    var quantity: Int = 0,
    var expiryDate: Long,
    var category: Category = Category.OTHER,
    var photoResource: Int = R.drawable.img_product_banana
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readLong(),
        Category.valueOf(parcel.readString() ?: Category.OTHER.name),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(quantity)
        parcel.writeLong(expiryDate)
        parcel.writeString(category.name)
        parcel.writeInt(photoResource)
    }

    override fun describeContents(): Int = 0

    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "quantity" to quantity,
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
