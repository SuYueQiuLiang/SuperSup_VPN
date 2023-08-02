package com.svper.supvpn.beans
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep


class MyServerList : ArrayList<MyServer>(){

}
@Keep
data class MyServer(
    val idjkort: Int = 0,
    val idjkp: String = "",
    val idjktry: String = "",
    val idjkway: String = "",
    val idjkword: String = "",
    val idjky: String = ""
) : Parcelable {

    fun getName() : String{
        return if(idjky == "")
            idjktry
        else "$idjktry-$idjky"
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(idjkort)
        parcel.writeString(idjkp)
        parcel.writeString(idjktry)
        parcel.writeString(idjkway)
        parcel.writeString(idjkword)
        parcel.writeString(idjky)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyServer> {
        override fun createFromParcel(parcel: Parcel): MyServer {
            return MyServer(parcel)
        }

        override fun newArray(size: Int): Array<MyServer?> {
            return arrayOfNulls(size)
        }
    }
}