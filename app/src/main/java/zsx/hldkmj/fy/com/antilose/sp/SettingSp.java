package zsx.hldkmj.fy.com.antilose.sp;

import android.content.Context;

public class SettingSp extends CommonSp {
	private static final String SP_NAME = "UserInfo";// FILE_NAME
	private static SettingSp instance;

	/* known key */
	private static final String KEY_NOT_DISTURB = "not_disturb";// 防打扰 boolean
	private static final String KEY_PASSWORD = "password";// 密码
	private static final String KEY_MAP_SELECT = "map_select";// 地图 0表示google
																// 1、百度
	private static final String KEY_DOUBLE_CLICK = "double_click";// 设备双击设置 0报警
																	// 1录音
	private static final String KEY_PHOTO_AUTO = "photo_auto";// 连拍
	private static final String KEY_PHOTO_GAP = "photo_gap";// 连拍间隔
    private static final String KEY_PASSWORD_STATE = "passwordState";   //app密码状态    0-关闭状态，   1-打开状态，   2-更改密码状态
	
	
	public static final SettingSp getInstance(Context context) {
		if (instance == null) {
			synchronized (SettingSp.class) {
				if (instance == null) {
					instance = new SettingSp(context);
				}
			}
		}
		return instance;
	}

	private SettingSp(Context context) {
		super(context, SP_NAME);
	}
	/**
	 * 获取报警距离
	 */
	public int getAlarmDis()
	{
		return getValue("KEY_ALARM_DIS", 5);
	}
	/**
	 * 设置报警距离
	 */
	public void setAlarmDis(int dis)
	{
		setValue("KEY_ALARM_DIS", dis);
	}
	
	// 防打扰
	public boolean getNotDisturb() {
		return getValue(KEY_NOT_DISTURB, false);
	}

	public void setNotDisturb(boolean value) {
		setValue(KEY_NOT_DISTURB, value);
	}

	// 密码
	public String getPassword() {
		return getValue(KEY_PASSWORD, "");
	}

	public void setPassword(String value) {
		setValue(KEY_PASSWORD, value);
	}

	//密码状态
	public int getPasswordState()
	{
		return getValue(KEY_PASSWORD_STATE, 0);
	}
	
	public void setPasswordState(int value)
	{
		setValue(KEY_PASSWORD_STATE, value);
	}
	
	// 地图选择
	public int getMapSelect() {
		return getValue(KEY_MAP_SELECT, 1);
	}

	public void setMapSelect(int value) {
		setValue(KEY_MAP_SELECT, value);
	}

	// 双击设置
	public int getDoubleClick() {
		return getValue(KEY_DOUBLE_CLICK, 0);
	}

	public void setDoubleClick(int value) {
		setValue(KEY_DOUBLE_CLICK, value);
	}

	// 连拍次数
	public int getPhotoAuto() {
		return getValue(KEY_PHOTO_AUTO, 0);
	}

	public void setPhotoAuto(int value) {
		setValue(KEY_PHOTO_AUTO, value);
	}

	// 连拍时间
	public int getPhotoGap() {
		return getValue(KEY_PHOTO_GAP, 0);
	}

	public void setPhotoGap(int value) {
		setValue(KEY_PHOTO_GAP, value);
	}

}
