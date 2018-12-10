package com.xiaoxin.library.baiduasr

import java.util.*

internal enum class Scheme(private val scheme: String) {
    HTTP("http"), HTTPS("https"),
    FILE("file"), CONTENT("content"),
    ASSETS("assets"), DRAWABLE("drawable"), UNKNOWN("");

    private val uriPrefix: String = "$scheme://"

    private fun belongsTo(uri: String): Boolean {
        return uri.toLowerCase(Locale.US).startsWith(uriPrefix)
    }

    /**
     * Appends scheme to incoming path
     */
    fun wrap(path: String): String {
        return uriPrefix + path
    }

    /**
     * Removed scheme part ("scheme://") from incoming URI
     */
    fun crop(uri: String): String {
        if (!belongsTo(uri)) {
            throw IllegalArgumentException(
                String.format(
                    "URI [%1\$s] doesn't have expected scheme [%2\$s]",
                    uri,
                    scheme
                )
            )
        }
        return uri.substring(uriPrefix.length)
    }

    companion object {

        /**
         * Defines scheme of incoming URI
         *
         * @param uri URI for scheme detection
         * @return Scheme of incoming URI
         */
        fun ofUri(uri: String?): Scheme {
            if (uri != null) {
                for (s in values()) {
                    if (s.belongsTo(uri)) {
                        return s
                    }
                }
            }
            return UNKNOWN
        }
    }
}