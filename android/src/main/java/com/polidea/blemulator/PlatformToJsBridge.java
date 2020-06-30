package com.polidea.blemulator;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.polidea.multiplatformbleadapter.ConnectionOptions;
import com.polidea.multiplatformbleadapter.Device;
import com.polidea.multiplatformbleadapter.OnErrorCallback;
import com.polidea.multiplatformbleadapter.OnSuccessCallback;
import com.polidea.multiplatformbleadapter.RefreshGattMoment;
import com.polidea.multiplatformbleadapter.errors.BleError;
import com.polidea.multiplatformbleadapter.errors.BleErrorCode;

import androidx.annotation.Nullable;

public class PlatformToJsBridge {
    private static final String TAG = PlatformToJsBridge.class.getSimpleName();
    private final ReactContext reactContext;
    private final JsCallHandler callHandler;

    public PlatformToJsBridge(ReactContext reactContext, JsCallHandler callHandler) {
        this.reactContext = reactContext;
        this.callHandler = callHandler;
    }

    public void startScan(String[] filteredUUIDs,
                          int scanMode,
                          int callbackType,
                          final OnErrorCallback onErrorCallback) {
        WritableMap params = Arguments.createMap();

        if (filteredUUIDs != null) {
            params.putArray(JsArgumentName.FILTERED_UUIDS, Arguments.fromArray(filteredUUIDs));
        } else {
            params.putArray(JsArgumentName.FILTERED_UUIDS, null);
        }

        params.putInt(JsArgumentName.CALLBACK_TYPE, callbackType);
        params.putInt(JsArgumentName.SCAN_MODE, scanMode);

        callMethod(
                MethodName.START_SCAN,
                params,
                new JsCallHandler.Callback() {
                    @Override
                    public void invoke(ReadableMap args) {
                        if (args.hasKey(NativeArgumentName.ERROR)) {
                            onErrorCallback.onError(parseError(args.getMap(NativeArgumentName.ERROR)));
                        }
                    }
                }
        );
    }

    public void stopScan() {
        callMethod(MethodName.STOP_SCAN, null, new JsCallHandler.Callback() {
            @Override
            public void invoke(ReadableMap args) {
                Log.d(TAG, "stopScan callback called");
                //TODO can this throw errors? If so they'll be passed in the callback
            }
        });
    }

    public void connect(String deviceIdentifier,
                        ConnectionOptions connectionOptions,
                        final OnSuccessCallback<Device> onSuccessCallback,
                        final OnErrorCallback onErrorCallback) {
        WritableMap arguments = Arguments.createMap();
        arguments.putString(JsArgumentName.IDENTIFIER, deviceIdentifier);
        arguments.putBoolean(JsArgumentName.IS_AUTO_CONNECT, connectionOptions.getAutoConnect());
        arguments.putInt(JsArgumentName.REQUEST_MTU, connectionOptions.getRequestMTU());
        arguments.putBoolean(JsArgumentName.REFRESH_GATT, connectionOptions.getRefreshGattMoment() == RefreshGattMoment.ON_CONNECTED);
        if (connectionOptions.getTimeoutInMillis() != null) {
            arguments.putInt(JsArgumentName.TIMEOUT, connectionOptions.getTimeoutInMillis().intValue());
        }
        callMethod(MethodName.CONNECT, arguments, new JsCallHandler.Callback() {
            @Override
            public void invoke(ReadableMap args) {
                if (args.hasKey(NativeArgumentName.ERROR)) {
                    onErrorCallback.onError(parseError(args.getMap(NativeArgumentName.ERROR)));
                } else {
                    onSuccessCallback.onSuccess(null);
                }
            }
        });
    }

    public void cancelDeviceConnection(final String deviceIdentifier,
                                       final OnSuccessCallback<Device> onSuccessCallback,
                                       final OnErrorCallback onErrorCallback) {
        WritableMap arguments = Arguments.createMap();
        arguments.putString(JsArgumentName.IDENTIFIER, deviceIdentifier);
        callMethod(
                MethodName.CANCEL_CONNECTION_OR_DISCONNECT,
                arguments,
                new JsCallHandler.Callback() {
                    @Override
                    public void invoke(ReadableMap args) {
                        if (args.hasKey(NativeArgumentName.ERROR)) {
                            onErrorCallback.onError(parseError(args.getMap(NativeArgumentName.ERROR)));
                        } else {
                            onSuccessCallback.onSuccess(null);
                        }
                    }
                });
    }

    private void callMethod(String methodName, @Nullable ReadableMap arguments, JsCallHandler.Callback callback) {
        WritableMap params = Arguments.createMap();
        String callbackId = callHandler.addCallback(callback);
        params.putString("methodName", methodName);
        params.putString("callbackId", callbackId);
        params.putMap("arguments", arguments);
        callJsMethod(params);
    }

    private BleError parseError(ReadableMap mappedError) {
        BleErrorCode matchedErrorCode = BleErrorCode.UnknownError;
        int incomingErrorCode = mappedError.getInt(NativeArgumentName.ERROR_CODE);
        for (BleErrorCode value : BleErrorCode.values()) {
            if (value.code == incomingErrorCode) {
                matchedErrorCode = value;
                break;
            }
        }
        return new BleError(matchedErrorCode, mappedError.getString(NativeArgumentName.ERROR_MESSAGE), -1);
    }

    private void callJsMethod(ReadableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("MethodCall", params);
    }
}