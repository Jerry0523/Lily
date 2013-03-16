package com.jerry.model;

public class Settings {

	private String sign;
	private int fontSize;
	private boolean isLogin;
	private boolean isShowPic;
	private boolean isSendMail;
	private boolean isNight;
	
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public boolean isLogin() {
		return isLogin;
	}
	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}
	public boolean isShowPic() {
		return isShowPic;
	}
	public void setShowPic(boolean isShowPic) {
		this.isShowPic = isShowPic;
	}
	public boolean isSendMail() {
		return isSendMail;
	}
	public void setSendMail(boolean isSendMail) {
		this.isSendMail = isSendMail;
	}
	public boolean isNight() {
		return isNight;
	}
	public void setNight(boolean isNight) {
		this.isNight = isNight;
	}
	public int getFontSize() {
		return fontSize;
	}
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

}
