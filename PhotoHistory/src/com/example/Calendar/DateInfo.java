package com.example.Calendar;
public class DateInfo {
	private String Id;
	private String Memo;

	public DateInfo(String Id, String memo) {
		super();
		this.Id = Id;
		this.Memo = memo;
	}

	public String Id() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public String getMemo() {
		return Memo;
	}
	public void setMemo(String memo) {
		this.Memo = memo;
	}
}
