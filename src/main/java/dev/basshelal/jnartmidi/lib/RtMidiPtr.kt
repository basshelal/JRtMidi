package dev.basshelal.jnartmidi.lib

import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder

/**
 * The struct used by RtMidi to wrap an RtMidi object for C function return statuses.
 */
@FieldOrder("ptr", "data", "ok", "msg")
open class RtMidiPtr : Structure {

    /**
     * C type : void*
     */
    @JvmField
    var ptr: Pointer? = null

    /**
     * C type : void*
     */
    @JvmField
    var data: Pointer? = null

    /**
     * C type : bool
     */
    @JvmField
    var ok = false

    /**
     * C type : const char*
     */
    @JvmField
    var msg: String? = null

    constructor() : super()

    constructor(peer: Pointer?) : super(peer)

    constructor(ptr: Pointer?, data: Pointer?, ok: Boolean, msg: String?) : super() {
        this.ptr = ptr
        this.data = data
        this.ok = ok
        this.msg = msg
    }

    constructor(other: RtMidiPtr) : this(other.pointer) {
        ptr = other.ptr
        data = other.data
        ok = other.ok
        msg = other.msg
    }
}