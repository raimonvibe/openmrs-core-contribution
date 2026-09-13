/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.initialization;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class TestInstallUtilTest {

	@Test
	public void openHttpUrl_shouldRejectNonHttpSchemes() {
		assertThrows(IOException.class, () -> TestInstallUtil.openHttpUrl("file:///etc/passwd"));
		assertThrows(IOException.class, () -> TestInstallUtil.openHttpUrl("ftp://example.com/openmrs"));
	}

	@Test
	public void openHttpUrl_shouldRejectEmbeddedCredentials() {
		assertThrows(IOException.class, () -> TestInstallUtil.openHttpUrl("http://user:pass@example.com/openmrs"));
	}

	@Test
	public void openHttpUrl_shouldRejectMissingHost() {
		assertThrows(IOException.class, () -> TestInstallUtil.openHttpUrl("http:///openmrs"));
	}

	@Test
	public void openHttpUrl_shouldRejectLoopbackAndLinkLocalTargets() {
		IOException loopback = assertThrows(IOException.class, () -> TestInstallUtil.openHttpUrl("http://127.0.0.1/openmrs"));
		assertTrue(loopback.getMessage().contains("internal"));

		IOException localhost = assertThrows(IOException.class, () -> TestInstallUtil.openHttpUrl("http://localhost/openmrs"));
		assertTrue(localhost.getMessage().contains("internal"));

		IOException metadata = assertThrows(IOException.class,
		    () -> TestInstallUtil.openHttpUrl("http://169.254.169.254/latest/meta-data/"));
		assertTrue(metadata.getMessage().contains("internal"));
	}
}
