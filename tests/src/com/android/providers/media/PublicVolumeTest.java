/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.providers.media;

import static com.android.providers.media.tests.utils.PublicVolumeSetupHelper.createNewPublicVolume;
import static com.android.providers.media.tests.utils.PublicVolumeSetupHelper.deletePublicVolumes;
import static com.android.providers.media.tests.utils.PublicVolumeSetupHelper.partitionPublicVolume;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.android.providers.media.util.FileUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class PublicVolumeTest {
    @BeforeClass
    public static void setUp() throws Exception {
        createNewPublicVolume();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        deletePublicVolumes();
    }

    public boolean containsDefaultFolders(String rootPath) {
        for (String dirName : FileUtils.DEFAULT_FOLDER_NAMES) {
            final File defaultFolder = new File(rootPath, dirName);
            if (!defaultFolder.exists()) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void testPublicVolumeDefaultFolders() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();

        // Reformat the volume, which should make sure we have default folders
        partitionPublicVolume();
        List<StorageVolume> volumes = context.
                getSystemService(StorageManager.class).getStorageVolumes();
        for (StorageVolume volume : volumes) {
            // We only want to verify reliable public volumes (not OTG)
            if (!volume.isPrimary() && volume.getPath().startsWith("/storage")) {
                assertTrue(containsDefaultFolders(volume.getPath()));
            }
        }

        // We had a bug before where we didn't re-create public volumes when the same
        // volume was re-formatted. Repartition it and try again.
        partitionPublicVolume();

        volumes = context.getSystemService(StorageManager.class).getStorageVolumes();
        for (StorageVolume volume : volumes) {
            // We only want to verify reliable public volumes (not OTG)
            if (!volume.isPrimary() && volume.getPath().startsWith("/storage")) {
                assertTrue(containsDefaultFolders(volume.getPath()));
            }
        }
    }
}

