package com.mercadopago.android.px.tracking.mocks;

import android.support.test.InstrumentationRegistry;
import com.mercadopago.android.px.model.AppInformation;
import com.mercadopago.android.px.model.DeviceInfo;
import com.mercadopago.android.px.tracking.internal.utils.JsonConverter;

public class TrackingStaticMock {

    public static AppInformation getApplicationInformation() {
        try {
            String json = MockUtils.getFile(InstrumentationRegistry.getContext(), "mocks/app_information.json");
            return JsonConverter.getInstance().fromJson(json, AppInformation.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public static DeviceInfo getDeviceInformation() {
        try {
            String json = MockUtils.getFile(InstrumentationRegistry.getContext(), "mocks/device_information.json");
            return JsonConverter.getInstance().fromJson(json, DeviceInfo.class);
        } catch (Exception ex) {
            return null;
        }
    }
}