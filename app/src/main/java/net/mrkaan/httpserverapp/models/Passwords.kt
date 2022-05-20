package net.mrkaan.httpserverapp.models

import androidx.databinding.BaseObservable

class Passwords : BaseObservable() {
    var passwords: List<Password>? = null
    var isFilled: Boolean = false
}