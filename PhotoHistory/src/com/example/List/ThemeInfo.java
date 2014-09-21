package com.example.List;
public class ThemeInfo {
	private String Name;
	private String Memo;

	public ThemeInfo(String Name, String memo) {
		super();
		this.Name = Name;
		this.Memo = memo;
	}

	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getMemo() {
		return Memo;
	}
	public void setMemo(String memo) {
		this.Memo = memo;
	}
}
