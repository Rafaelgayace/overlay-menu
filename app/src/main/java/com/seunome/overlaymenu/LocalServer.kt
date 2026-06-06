package com.seunome.overlaymenu

import fi.iki.elonen.NanoHTTPD

class LocalServer(port: Int) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
        val action = OverlayService.pendingActions.poll() ?: ""
        val r = newFixedLengthResponse(action)
        r.addHeader("Access-Control-Allow-Origin", "*")
        return r
    }
}
