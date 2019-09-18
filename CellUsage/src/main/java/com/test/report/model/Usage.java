package com.test.report.model;

import java.util.Date;

public class Usage {
	private Integer employeeId;
	private Date date;
	private Integer minutesUsed;
	private Double dataUsed;
	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	 
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Integer getMinutesUsed() {
		return minutesUsed;
	}
	public void setMinutesUsed(Integer minutesUsed) {
		this.minutesUsed = minutesUsed;
	}
	public Double getDataUsed() {
		return dataUsed;
	}
	public void setDataUsed(Double dataUsed) {
		this.dataUsed = dataUsed;
	}
	


}
