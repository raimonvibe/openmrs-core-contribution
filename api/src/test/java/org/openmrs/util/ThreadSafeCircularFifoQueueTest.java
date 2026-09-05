/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.Queue;

import org.apache.commons.collections4.queue.AbstractQueueTest;

/**
 * Unit tests for the {@link ThreadSafeCircularFifoQueue} class
 * 
 * This class uses the tests from Apache Commons Collections for Queues to ensure the correctness of the implementation.
 * The inherited test methods are picked up automatically, so they are not re-declared here.
 * @param <E>
 */
public class ThreadSafeCircularFifoQueueTest<E> extends AbstractQueueTest<E> {
	// TODO We should add some tests related to concurrent access

	/* Configuration */
	
	@Override
	public boolean isFailFastSupported() {
		return false;
	}

	@Override
	public boolean isNullSupported() {
		return false;
	}

	// NB This is marked as false not because we don't support serialization, but because we don't provide the artefacts
	// needed for these tests
	@Override
	public boolean isTestSerialization() {
		return false;
	}

	@Override
	public Queue<E> makeObject() {
		return new ThreadSafeCircularFifoQueue<>(100);
	}
}
