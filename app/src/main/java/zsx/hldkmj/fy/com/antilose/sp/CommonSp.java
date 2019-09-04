package zsx.hldkmj.fy.com.antilose.sp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * SharedPreferences 存取的封装
 * 
 * @author roamer
 */
public class CommonSp {
	private SharedPreferences mSharePre;

	protected CommonSp(Context context, String spName) {
		mSharePre = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
	}

	/* get and put String value */
	protected void setValue(String key, String value) {
		mSharePre.edit().putString(key, value).commit();
	}

	protected String getValue(String key, String defValue) {
		return mSharePre.getString(key, defValue);
	}

	/* get and put boolean value */
	protected void setValue(String key, boolean value) {
		mSharePre.edit().putBoolean(key, value).commit();
	}

	protected boolean getValue(String key, boolean defValue) {
		return mSharePre.getBoolean(key, defValue);
	}

	/* get and put int value */
	protected void setValue(String key, int value) {
		mSharePre.edit().putInt(key, value).commit();
	}

	protected int getValue(String key, int defValue) {
		return mSharePre.getInt(key, defValue);
	}

	/* get and put int value */
	protected void setValue(String key, float value) {
		mSharePre.edit().putFloat(key, value).commit();
	}

	protected float getValue(String key, float defValue) {
		return mSharePre.getFloat(key, defValue);
	}

	/* get and put long value */
	protected void setValue(String key, long value) {
		mSharePre.edit().putLong(key, value).commit();
	}

	protected long getValue(String key, long defValue) {
		return mSharePre.getLong(key, defValue);
	}

	/* get and put string set value */
	protected void setValue(String key, Set<String> value) {
		mSharePre.edit().putStringSet(key, value).commit();
	}

	protected Set<String> getValue(String key, Set<String> defValue) {
		return mSharePre.getStringSet(key, defValue);
	}

}
