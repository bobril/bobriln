package com.bobril.bobriln;

import java.util.Map;

public interface Gateway {
    interface NativeCall {
        void Run(Decoder params, Encoder result);
    }

    interface EventResultCallback {
        void EventResult(boolean result);
    }

    void RegisterNativeMethod(String name, NativeCall implementation);
    void RegisterResetMethod(Runnable implementation);

    void RegisterTag(String tag, IVNodeFactory factory);

    void emitJSEvent(String name, Map<String, Object> param, int nodeId);
    void emitJSEvent(String name, Map<String, Object> param, int nodeId, long time, EventResultCallback callback);
}
