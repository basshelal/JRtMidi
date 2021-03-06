@file:Suppress("RedundantVisibilityModifier")

package dev.basshelal.jrtmidi.api

import dev.basshelal.jrtmidi.lib.Build
import dev.basshelal.jrtmidi.lib.RtMidiApis
import dev.basshelal.jrtmidi.lib.RtMidiLibrary
import dev.basshelal.jrtmidi.lib.library
import jnr.ffi.LibraryLoader
import jnr.ffi.LibraryOption

/**
 * The entry point to the JRtMidi Library.
 *
 * ### Getting Started:
 *
 *
 * Create a [ReadableMidiPort] by getting a [MidiPort.Info] from [RtMidi.readableMidiPorts],
 * similarly for [WritableMidiPort]s by calling [RtMidi.writableMidiPorts].
 *
 * [MidiPort]s have a constructor
 * which allows you to choose the [RtMidiApi] to use on the port.
 * The [RtMidiApi]s available to use on the client's machine can be retrieved using [RtMidi.compiledApis].
 *
 * @author Bassam Helal
 */
public object RtMidi {

    @JvmStatic
    public fun isPlatformSupported(): Boolean = Build.isPlatformSupported

    /**
     * @return true if this platform supports virtual ports, false otherwise,
     * currently only Windows does not support virtual ports
     */
    @JvmStatic
    public fun supportsVirtualPorts(): Boolean = Build.supportsVirtualPorts && !Config.disallowVirtualPorts

    /**
     * @return the list of all [RtMidiApi]s that RtMidi detected when the native library of RtMidi was compiled that
     * are usable on this machine, this should be at most 2, ie on Unix (ALSA and JACK).
     * If an [RtMidiApi] was found by RtMidi at its compile time but then removed later (for example JACK on a Unix
     * system) it will still be reported as compiled.
     * @throws RtMidiNativeException if an error occurred in RtMidi's native code
     */
    @JvmStatic
    public fun compiledApis(): List<RtMidiApi> {
        val arr = IntArray(RtMidiApis.RTMIDI_API_NUM)
        return library.rtmidi_get_compiled_api(arr, arr.size).let { size ->
            if (size < 0) throw RtMidiNativeException("Error trying to get compiled apis")
            else List(size) { RtMidiApi.fromInt(arr[it]) }
        }
    }

    /**
     * @return a list of [MidiPort.Info]s for all [ReadableMidiPort]s on the system
     * @throws RtMidiNativeException if an error occurred in RtMidi's native code
     */
    @JvmStatic
    public fun readableMidiPorts(): List<MidiPort.Info> {
        val ptr = library.rtmidi_in_create_default()
        val portCount = library.rtmidi_get_port_count(ptr)
        if (!ptr.ok.get()) throw RtMidiNativeException(ptr)
        val result = List(portCount) {
            MidiPort.Info(name = library.rtmidi_get_port_name(ptr, it),
                    index = it, type = MidiPort.Info.Type.READABLE)
        }
        library.rtmidi_in_free(ptr)
        return result
    }

    /**
     * @return a list of [MidiPort.Info]s for all [WritableMidiPort]s on the system
     * @throws RtMidiNativeException if an error occurred in RtMidi's native code
     */
    @JvmStatic
    public fun writableMidiPorts(): List<MidiPort.Info> {
        val ptr = library.rtmidi_out_create_default()
        val portCount = library.rtmidi_get_port_count(ptr)
        if (!ptr.ok.get()) throw RtMidiNativeException(ptr)
        val result = List(portCount) {
            MidiPort.Info(name = library.rtmidi_get_port_name(ptr, it),
                    index = it, type = MidiPort.Info.Type.WRITABLE)
        }
        library.rtmidi_out_free(ptr)
        return result
    }

    public object Config {
        /** When true, no more configuring is allowed */
        internal var loaded: Boolean = false

        private var useBundledLibraries: Boolean = true

        private var customLibraryPaths: MutableList<String> = mutableListOf()

        internal var disallowJACK: Boolean = false

        internal var disallowVirtualPorts: Boolean = false

        @JvmStatic
        public fun load() {
            val libPaths = mutableListOf<String>()
            if (useBundledLibraries) libPaths.add("libs/${Build.buildPath}")
            else libPaths.addAll(customLibraryPaths)
            library = try {
                LibraryLoader.loadLibrary(RtMidiLibrary::class.java,
                        mapOf(LibraryOption.LoadNow to true, LibraryOption.IgnoreError to true),
                        mapOf(RtMidiLibrary.LIBRARY_NAME to libPaths),
                        RtMidiLibrary.LIBRARY_NAME)
                // TODO: 30-Jun-2021 @basshelal: When jnr-ffi 2.2.5 releases ensure that
                //  we have indeed loaded the correct library and force using ours over system for GNU/Linux
            } catch (e: LinkageError) {
                System.err.println("Error linking RtMidi:\nPlatform: ${Build.platformName}\nLibPaths:\n${libPaths.joinToString()}")
                throw e
            }
            loaded = true
        }

        // TODO: 17-Jun-2021 @basshelal: Add Unload and reload? Useful for config changes,
        //  ie user doesn't want JACK anymore or wants to use custom paths etc


        // TODO: 30-Jun-2021 @basshelal: Instead of disallow JACK we can make users pick a more generic build type
        //  even before load(), which will pick their preferred API combo for each platform
        /**
         * Do not use a build with JACK even if JACK exists on the system, because if a JACK server is not found,
         * you cannot do anything, use [JNAJack](https://github.com/jaudiolibs/jnajack) to interact with JACK on the JVM
         */
        @JvmStatic
        public fun disallowJACK(value: Boolean): Config = apply { if (!loaded) disallowJACK = value }

        /**
         * Disallow virtual ports to keep code truly cross-platform because RtMidi doesn't support virtual ports
         * on Windows
         */
        @JvmStatic
        public fun disallowVirtualPorts(value: Boolean): Config = apply { if (!loaded) disallowVirtualPorts = value }

        /** Use the bundled RtMidi native libraries, else use `customRtMidiLibraryPaths` */
        @JvmStatic // Experimental
        internal fun useBundledLibraries(value: Boolean): Config = apply { if (!loaded) useBundledLibraries = value }

        // TODO: 30-Jun-2021 @basshelal: Probably disallow this, we might bundle custom versions so user-specified
        //  paths may fail

        /** Paths to custom RtMidi libraries, only used if `useBundledLibraries` is false */
        @JvmStatic // Experimental
        internal fun customLibraryPaths(value: MutableList<String>): Config = apply { if (!loaded) customLibraryPaths = value }
    }

}