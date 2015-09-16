package adjusttime.lemoon;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.os.Process;
import android.util.Log;

public class Logger {
	private static final String LOGTAG = "AjustTime";
	static boolean _DEBUG = false;
	
	public static final void LOGD(String Tag, String paramString) {
		if (!_DEBUG)
			return;
		StringBuffer localStringBuffer = new StringBuffer("[pid:" + Process.myPid() + ", tid:" + Process.myTid() + "]======");
		localStringBuffer.append(paramString);
		Log.d(Tag, localStringBuffer.toString());
	}

	public static final void LOGD(String paramString) {
		if (!_DEBUG)
			return;
		StringBuffer localStringBuffer = new StringBuffer("[pid:" + Process.myPid() + ", tid:" + Process.myTid() + "]======");
		localStringBuffer.append(paramString);
		Log.d(LOGTAG, localStringBuffer.toString());
	}

	public static final void LOGE(String tag, String paramString) {
		if (!_DEBUG)
			return;
		StringBuffer localStringBuffer = new StringBuffer("[pid:" + Process.myPid() + ", tid:" + Process.myTid() + "]======");
		localStringBuffer.append(paramString);
		Log.e(tag, localStringBuffer.toString());
	}

	public static final void LOGE(String paramString) {
		if (!_DEBUG)
			return;
		StringBuffer localStringBuffer = new StringBuffer("[pid:" + Process.myPid() + ", tid:" + Process.myTid() + "]======");
		localStringBuffer.append(paramString);
		Log.e(LOGTAG, localStringBuffer.toString());
	}

	public static final void LOGI(String tag, String paramString) {
		if (!_DEBUG)
			return;
		StringBuffer localStringBuffer = new StringBuffer("[pid:" + Process.myPid() + ", tid:" + Process.myTid() + "]======");
		localStringBuffer.append(paramString);
		Log.i(tag, localStringBuffer.toString());
	}

	public static final void LOGI(String paramString) {
		if (!_DEBUG)
			return;
		StringBuffer localStringBuffer = new StringBuffer("[pid:" + Process.myPid() + ", tid:" + Process.myTid() + "]======");
		localStringBuffer.append(paramString);
		Log.i(LOGTAG, localStringBuffer.toString());
	}

	public static final void LOGW(String tag, String paramString) {
		if (!_DEBUG)
			return;
		StringBuffer localStringBuffer = new StringBuffer("[pid:" + Process.myPid() + ", tid:" + Process.myTid() + "]======");
		localStringBuffer.append(paramString);
		Log.w(tag, localStringBuffer.toString());
	}

	public static final void LOGW(String paramString) {
		  if(!_DEBUG)
			  return;
		StringBuffer localStringBuffer = new StringBuffer("[pid:" + Process.myPid() + ", tid:" + Process.myTid() + "]======");
		localStringBuffer.append(paramString);
		Log.w(LOGTAG, localStringBuffer.toString());
	}

	/* Error */
	public static final void saveLogToFile(String nMessage) {
		String bf = "[pid:" + Process.myPid() + ", tid:" + Process.myTid() + "]======"; // v0

		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "crash"); // v1
		if (!dir.exists()) {
			dir.mkdir();
		}
		// cond0
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss"); // v4
		long timetamp = System.currentTimeMillis(); // v8

		String time = format.format(new Date()); // v7
		String name = "crash-" + time + "-" + timetamp + ".nullpointer.log"; // v3

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(new File(dir, name));
			fos.write(dir.toString().getBytes());
			fos.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;
	}
}