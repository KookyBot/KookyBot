package com.github.zly2006.khlkt.exception

import java.net.http.HttpRequest

class KhlRemoteException(
    val code: Int,
    override val message: String?,
    val request: HttpRequest? = null
) : Exception() {
    override fun printStackTrace() {
        System.err.println("code=$code,request url=${request?.uri()}")
        super.printStackTrace()
    }
}