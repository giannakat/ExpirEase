import android.widget.EditText

fun EditText.isNotValid(): Boolean{
    return this.text.toString().isNullOrEmpty();
}

