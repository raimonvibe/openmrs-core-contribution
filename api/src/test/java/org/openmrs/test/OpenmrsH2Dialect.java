/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test;

import java.sql.Types;

import org.hibernate.dialect.H2Dialect;

/**
 * Hibernate's H2Dialect emits {@code varchar(2147483647)} for LONGVARCHAR, which H2 2.x rejects
 * (max precision is 1_000_000_000). Map those columns to CLOB so hbm2ddl can create TEXT fields.
 */
public class OpenmrsH2Dialect extends H2Dialect {
	
	public OpenmrsH2Dialect() {
		super();
		registerColumnType(Types.LONGVARCHAR, "clob");
		registerColumnType(Types.CLOB, "clob");
	}
}
