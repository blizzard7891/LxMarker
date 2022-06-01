/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.lxmarker.ui;

import android.os.ParcelUuid;

import java.util.UUID;

/**
 * Constants for use in the Bluetooth Advertisements sample
 */
public class Constants {

    /**
     * UUID identified with this app - set as Service UUID for BLE Advertisements.
     * <p>
     * Bluetooth requires a certain format for UUIDs associated with Services.
     * The official specification can be found here:
     * {@link https://www.bluetooth.org/en-us/specification/assigned-numbers/service-discovery}
     */
    public static final ParcelUuid Service_UUID = ParcelUuid
            .fromString("6a36ff63-59e8-4971-8379-9e22f7a9fefb");
    public static final ParcelUuid Advertise_Data1_UUID = ParcelUuid
            .fromString("00008961-0000-1000-8000-00805f9b34fb");
    public static final UUID Characteristic_UUID = UUID.
            fromString("6e40ffc0-b53a-3f93-e0a9-e50e24dcca9e");
    public static final UUID Battery_Service_UUID = UUID
            .fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID Battery_Characteristic_UUID = UUID
            .fromString("00002a19-0000-1000-8000-00805f9b34fb");

    public static final int DIST_CMD_CHAR_IDX = 0;
    public static final int CYCLE_CMD_CHAR_IDX = 1;

    public static final String CMD_BLE_START = "S1E";
    public static final String CMD_BLE_STOP = "S0E";
    public static final String CMD_BLE_CONTINUE = "S2E";
}
