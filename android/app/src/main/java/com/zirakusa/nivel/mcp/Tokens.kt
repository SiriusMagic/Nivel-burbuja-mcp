package com.zirakusa.nivel.mcp

import java.security.SecureRandom
import java.util.Base64

/** As in the sibling apps. Nivel only has reading tools today, so both see the same; WRITE is kept for the hub and later tools. */
enum class Access(val key: String) { READ("read"), WRITE("write") }

/**
 * A token the handler accepts. The app serves only Arka's own (WRITE): Arka turns the Modo MCP on or off, and the hub
 * decides what each AI may do. READ exists so the rules for read-only tokens stay tested.
 */
class Token(val secret: String, val access: Access = Access.WRITE, val name: String = "Arka") {
    companion object {
        /** "niv_" + 256 random bits: the prefix tells the hub (and secret scanners) which app it belongs to. */
        fun secret(): String = "niv_" + Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also(SecureRandom()::nextBytes))
        fun id(): String = ByteArray(6).also(SecureRandom()::nextBytes).joinToString("") { "%02x".format(it) }
    }
}
