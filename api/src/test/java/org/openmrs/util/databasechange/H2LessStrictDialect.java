/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util.databasechange;

import java.sql.Types;
import org.hibernate.dialect.H2Dialect;

/**
 * H2 version we use behaves differently from H2Dialect bundled with Hibernate so we provide a
 * custom implementation for the purpose of validation in {@link DatabaseUpgradeTestUtil}
 */
public class H2LessStrictDialect extends H2Dialect {
	
	public H2LessStrictDialect() {
		super();
		
		// H2Dialect incorrectly sets these to synonyms in H2
		//
		registerColumnType(Types.BIGINT, "integer");
		
		// H2 2 reports CLOB/TEXT as CHARACTER LARGE OBJECT; Hibernate 5 still emits LONGVARCHAR/clob.
		//
		registerColumnType(Types.CLOB, "character large object");
		registerColumnType(Types.LONGVARCHAR, "character large object");
		
		// H2 maps 'FLOAT' to 'double' as per http://www.h2database.com/html/datatypes.html#double_type
		//
		// Without mapping 'float' to 'double' the validation of Hibernate mappings fails:
		//   Schema-validation: 
		//     wrong column type encountered in column [sort_weight] in table [form_field]; 
		//     found [double (Types#DOUBLE)], but expecting [float (Types#FLOAT)]
		//
		registerColumnType(Types.FLOAT, "double");
		
		//person.birthdate is not a timestamp, but date in db
		//
		registerColumnType(Types.TIMESTAMP, "date");
		
		// UUIDs are created as char(38). H2 2 reports that JDBC type as CHARACTER, not CHAR.
		//
		registerColumnType(Types.CHAR, "character");
		registerColumnType(Types.VARCHAR, 38, "character");
		
		// These mappings are required for "long" fields of type java.lang.String that are declared as 'text' 
		// in Hibernate change sets.
		//
		registerColumnType(Types.VARCHAR, 250, "character large object");
		registerColumnType(Types.VARCHAR, 500, "character large object");
		registerColumnType(Types.VARCHAR, 1024, "character large object");
		registerColumnType(Types.VARCHAR, 65535, "character large object");
		registerColumnType(Types.VARCHAR, 16777215, "character large object");
		registerColumnType(Types.VARCHAR, 2147483647, "character large object");
		registerColumnType(Types.LONGVARCHAR, 2147483647, "character large object");
	}
	
	/**
	 * H2 2 JDBC metadata uses BOOLEAN, CHARACTER, and CLOB where Hibernate 5 still emits BIT,
	 * VARCHAR, and LONGVARCHAR. Treat those families as the same during schema validation.
	 */
	@Override
	public boolean equivalentTypes(int typeCode1, int typeCode2) {
		if (super.equivalentTypes(typeCode1, typeCode2)) {
			return true;
		}
		return isStringLike(typeCode1) && isStringLike(typeCode2) || isBooleanLike(typeCode1) && isBooleanLike(typeCode2)
		        || isIntegerLike(typeCode1) && isIntegerLike(typeCode2) || isDateLike(typeCode1) && isDateLike(typeCode2);
	}
	
	private static boolean isStringLike(int typeCode) {
		return typeCode == Types.CHAR || typeCode == Types.VARCHAR || typeCode == Types.LONGVARCHAR || typeCode == Types.CLOB
		        || typeCode == Types.NCHAR || typeCode == Types.NVARCHAR || typeCode == Types.LONGNVARCHAR
		        || typeCode == Types.NCLOB;
	}
	
	private static boolean isBooleanLike(int typeCode) {
		return typeCode == Types.BOOLEAN || typeCode == Types.BIT || typeCode == Types.TINYINT;
	}
	
	private static boolean isIntegerLike(int typeCode) {
		return typeCode == Types.TINYINT || typeCode == Types.SMALLINT || typeCode == Types.INTEGER
		        || typeCode == Types.BIGINT;
	}
	
	private static boolean isDateLike(int typeCode) {
		return typeCode == Types.DATE || typeCode == Types.TIME || typeCode == Types.TIMESTAMP
		        || typeCode == Types.TIME_WITH_TIMEZONE || typeCode == Types.TIMESTAMP_WITH_TIMEZONE;
	}
}
