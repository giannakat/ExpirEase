import android.app.Activity
import android.content.Context
import android.widget.EditText
import android.widget.Toast

fun EditText.isNotValid(): Boolean{
    return this.text.toString().isNullOrEmpty();
}
fun Context.toast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

