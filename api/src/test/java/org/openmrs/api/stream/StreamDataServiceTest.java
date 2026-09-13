/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

public class StreamDataServiceTest {

	@TempDir
	Path tempDir;

	@Test
	public void streamData_shouldSurfaceWriterFailureWhenCopying() throws IOException {
		StreamDataService service = new StreamDataService(new SimpleAsyncTaskExecutor());
		InputStream in = service.streamData(out -> {
			out.write(1);
			throw new IOException("Failure during writing");
		}, null);

		Path target = tempDir.resolve("copy.bin");
		assertThrows(IOException.class, () -> Files.copy(in, target));
	}
}
