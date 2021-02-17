package com.github.basshelal.jnartmidi.api;

import com.github.basshelal.jnartmidi.lib.RtMidiLibrary;

public class RtMidi {

    private RtMidi() {}

    public static RtMidiApi[] getAvailableApis() throws RtMidiException {
        int[] arr = new int[RtMidiLibrary.RtMidiApi.RTMIDI_API_NUM];
        int written = RtMidiLibrary.getInstance().rtmidi_get_compiled_api(arr, arr.length);

        if (written < 0) throw new RtMidiException("Error trying to get compiled apis");
        else {
            RtMidiApi[] result = new RtMidiApi[written];
            for (int i = 0; i < written; i++) {
                result[i] = RtMidiApi.fromInt(arr[i]);
            }
            return result;
        }
    }

}
