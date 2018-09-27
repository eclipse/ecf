package org.eclipse.ecf.tests.core.util;

public class TestEnumImpl implements ITestEnum {

	private static final long serialVersionUID = 1L;

	private ColumnType columnType;

	public ColumnType getColumnType() {
		return columnType;
	}
	
	public TestEnumImpl() {
		this.columnType = ITestEnum.ColumnType.METADATA;
	}

}
