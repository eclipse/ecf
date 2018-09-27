package org.eclipse.ecf.tests.core.util;

import java.io.Serializable;

public interface ITestEnum extends Serializable {

	enum ColumnType {
		METADATA,
		ICON
	}

	enum DataType {
		STRING,
		DATE,
		INTEGER,
		FILE_SIZE,
		USER
	}
	
	ColumnType getColumnType();
}
