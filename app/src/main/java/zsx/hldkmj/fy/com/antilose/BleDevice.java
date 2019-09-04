package zsx.hldkmj.fy.com.antilose;



public class BleDevice {
	public static final int DEVICE_DISCONNECTED = 0;// 没有连接
	public static final int DEVICE_CONNECTED = 1;// 已经连接
	public static final int DEVICE_CONNECTING = 2;// 正在连接
	public static final int DEVICE_WARING = 3;// 正在报警
	public static final int DEVICE_RECONNECTING = 4;// 连接断开，正在重新连接
	
	private int id;
	private String address;//mac地址
	private String name;//名字
	private String iconPath;//设备头像
	private int rssi;
	//防丢设置
	private boolean isAntilossOn=true;//是否开启防丢
	private boolean isAntilossLightOn=true;
	private int antilossVolume=5;//音量，设置为10个级别
	private int antilossRing=0;//铃声index
	
//	alarm.mp3                   0
//	alarm_bird.mp3              1
//	alarm_car.mp3               2
//	alarm_cat.mp3               3
//	alarm_chatcall.mp3          4
//	alarm_dog.mp3               5
//	alarm_fire.mp3              6
//	alarm_music.mp3             7
//	alarm_radar.mp3             8
//	alarm_trumpet.mp3           9
//	alarm_whistle.mp3           10

	//寻找设置
	private boolean isFindLightOn=true;
	private int findVolume=5;//音量，设置为10个级别
	private int findRing=0;//铃声index
	
	
	private int status = 0;// 已经连接(实时状态，不持久化到数据库)
	private boolean isWaringVoice=false;//是否正在报警发出声音
	private boolean isWaringColor=false;//是否正在报警改变颜色
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	
	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	public boolean isAntilossOn() {
		return isAntilossOn;
	}

	public void setAntilossOn(boolean isAntilossOn) {
		this.isAntilossOn = isAntilossOn;
	}

	public boolean isAntilossLightOn() {
		return isAntilossLightOn;
	}

	public void setAntilossLightOn(boolean isAntilossLightOn) {
		this.isAntilossLightOn = isAntilossLightOn;
	}

	public int getAntilossVolume() {
		return antilossVolume;
	}

	public void setAntilossVolume(int antilossVolume) {
		this.antilossVolume = antilossVolume;
	}

	public int getAntilossRing() {
		return antilossRing;
	}

	public void setAntilossRing(int antilossRing) {
		this.antilossRing = antilossRing;
	}

	public boolean isFindLightOn() {
		return isFindLightOn;
	}

	public void setFindLightOn(boolean isFindLightOn) {
		this.isFindLightOn = isFindLightOn;
	}

	public int getFindVolume() {
		return findVolume;
	}

	public void setFindVolume(int findVolume) {
		this.findVolume = findVolume;
	}

	public int getFindRing() {
		return findRing;
	}

	public void setFindRing(int findRing) {
		this.findRing = findRing;
	}

	public boolean isWaringVoice() {
		return isWaringVoice;
	}

	public void setWaringVoice(boolean isWaringVoice) {
		this.isWaringVoice = isWaringVoice;
	}

	public boolean isWaringColor() {
		return isWaringColor;
	}

	public void setWaringColor(boolean isWaringColor) {
		this.isWaringColor = isWaringColor;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi){
		this.rssi=rssi;
	}

	public String getStatusDes() {
		if (status == DEVICE_DISCONNECTED) {
			return MyApplication.getInstance().getResources().getString(R.string.disconnect);
		} else if (status == DEVICE_CONNECTED) {
			return MyApplication.getInstance().getResources().getString(R.string.connected);
		} else if (status == DEVICE_CONNECTING) {
			return MyApplication.getInstance().getResources().getString(R.string.connecting);
		} else if (status == DEVICE_WARING) {
			return MyApplication.getInstance().getResources().getString(R.string.connected);
		}else if (status == DEVICE_RECONNECTING) {
			return MyApplication.getInstance().getResources().getString(R.string.connecting);
		}
		return MyApplication.getInstance().getResources().getString(R.string.disconnect);
	}
	public String getStatusNextOperation() {
		if (status == DEVICE_DISCONNECTED) {
			return MyApplication.getInstance().getResources().getString(R.string.connect);
		} else if (status == DEVICE_CONNECTED) {
			return MyApplication.getInstance().getResources().getString(R.string.call_police);
		} else if (status == DEVICE_CONNECTING) {
			return MyApplication.getInstance().getResources().getString(R.string.connect);
		}else if (status == DEVICE_WARING) {
			return MyApplication.getInstance().getResources().getString(R.string.cancel_police);
		}else if (status == DEVICE_RECONNECTING) {
			return MyApplication.getInstance().getResources().getString(R.string.cancel_again_connect);
		}
		return MyApplication.getInstance().getResources().getString(R.string.connect);
	}
}
