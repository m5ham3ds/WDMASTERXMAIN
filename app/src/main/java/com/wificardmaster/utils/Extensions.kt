package com.wdmaster.app.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.wdmaster.app.data.model.TestStats
import java.text.DecimalFormat
import java.util.*

// Context Extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.getClipboardText(): String? {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    return clipboard.primaryClip?.getItemAt(0)?.text?.toString()
}

fun Context.copyToClipboard(text: String, label: String = "Text") {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

// Fragment Extensions
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    context?.showToast(message, duration)
}

fun Fragment.hideKeyboard() {
    view?.let {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun Fragment.showKeyboard(editText: EditText) {
    editText.requestFocus()
    editText.postDelayed({
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }, 100)
}

// View Extensions
fun View.visible() {    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

// String Extensions
fun String.isValidMACPrefix(): Boolean {
    return matches(Regex("^([0-9A-Fa-f]{2}:){2}[0-9A-Fa-f]{2}$")) ||
           matches(Regex("^([0-9A-Fa-f]{2}-){2}[0-9A-Fa-f]{2}$")) ||
           matches(Regex("^[0-9A-Fa-f]{6}$"))
}

fun String.isValidCardLength(): Boolean {
    return length in Constants.MIN_CARD_LENGTH..Constants.MAX_CARD_LENGTH
}

fun String.normalizeMACPrefix(): String {
    return replace("-", ":")
        .uppercase()
        .let {
            if (it.length == 6) {
                it.chunked(2).joinToString(":")
            } else {
                it
            }
        }
}

fun String.truncate(maxLength: Int, suffix: String = "..."): String {
    return if (length > maxLength) {
        take(maxLength - suffix.length) + suffix
    } else {
        this
    }
}

fun String.maskCard(showLast: Int = 4): String {
    return if (length > showLast) {
        "*".repeat(length - showLast) + takeLast(showLast)
    } else {        this
    }
}

// Long Extensions (Time)
fun Long.formatETA(): String {
    return when {
        this < 60 -> "${this}s"
        this < 3600 -> "${this / 60}m ${this % 60}s"
        this < 86400 -> "${this / 3600}h ${(this % 3600) / 60}m"
        else -> "${this / 86400}d ${(this % 86400) / 3600}h"
    }
}

fun Long.toDateString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))
}

fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    return when {
        diff < Constants.MS_PER_MINUTE -> "Just now"
        diff < Constants.MS_PER_HOUR -> "${diff / Constants.MS_PER_MINUTE}m ago"
        diff < Constants.MS_PER_DAY -> "${diff / Constants.MS_PER_HOUR}h ago"
        diff < Constants.MS_PER_DAY * 7 -> "${diff / Constants.MS_PER_DAY}d ago"
        else -> toDateString("MMM dd, yyyy")
    }
}

// Double Extensions
fun Double.formatSpeed(): String {
    return DecimalFormat("0.00").format(this)
}

fun Double.formatPercentage(): String {
    return DecimalFormat("0.0").format(this) + "%"
}

// TestStats Extensions
fun TestStats.getFormattedSpeed(): String = speed.formatSpeed() + "/sec"
fun TestStats.getFormattedETA(): String = etaSeconds.formatETA()
fun TestStats.getSuccessRate(): Int = if (tested > 0) ((success * 100) / tested).toInt() else 0
fun TestStats.getSuccessRateFormatted(): String = getSuccessRate().toString() + "%"

// List Extensions
fun <T> List<T>.safeGet(index: Int): T? = getOrNull(index)
fun <T> List<T>.lastSafe(): T? = lastOrNull()
fun <T> List<T>.firstSafe(): T? = firstOrNull()
// Boolean Extensions
inline fun Boolean.ifTrue(action: () -> Unit) {
    if (this) action()
}

inline fun Boolean.ifFalse(action: () -> Unit) {
    if (!this) action()
}

// Int Extensions
fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

fun Int.pxToDp(context: Context): Int {
    return (this / context.resources.displayMetrics.density).toInt()
}

// Throwable Extensions
fun Throwable.getErrorMessage(): String {
    return message ?: "Unknown error occurred"
}

fun Throwable.logError(tag: String = "AppError") {
    android.util.Log.e(tag, getErrorMessage(), this)
}