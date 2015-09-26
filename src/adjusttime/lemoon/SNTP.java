package adjusttime.lemoon;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.TimeZone;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;

import android.content.Context;
import android.os.Handler;
import android.app.Service;
import android.os.IBinder;
import android.provider.Settings;
import android.os.SystemProperties;


////////////////////////////////
// 该源码要在android源码下编译
//  mm -B
///////////////////////////////


/**
 * {@hide} Simple SNTP client class for retrieving network time. Sample usage:
 * 
 * <pre>
 * SntpClient client = new SntpClient();
 * if (client.requestTime(&quot;203.117.180.36&quot;)) {
 * 	long now = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
 * }
 * </pre>
 */

/**
 * 使用前提
   将系统的自动对时功能关闭， 时区设为中国吧 
 
 只需要系统权限即可
 
    启动方法
	Intent intent=new Intent("adjusttime.lemoon.SNTP");    
	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);     
	ComponentName cn=new ComponentName("adjusttime.lemoon","adjusttime.lemoon.SNTP");    
	intent.setComponent(cn);    
	startService(intent);
 *
 */

public class SNTP extends Service {

	private static final String TAG = "SNTP--->";

	private static final boolean DBUG = true;

	private static Context mContext;

	/*
	 * public static void SNTP( Context context ) { mContext = context ; }
	 */

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		mSntpHandler.removeCallbacks(mSntpTask);
		if (DBUG)
			Logger.LOGD(TAG, " SNTP Service end !! ");
	}

	boolean mRunning  = false;
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		//获取固件版本后，共享给 内容管理app
		
		SharedPreferences sp = getSharedPreferences("firm_info", MODE_MULTI_PROCESS|MODE_WORLD_READABLE);
        sp.edit().putString("ota_ver", SystemProperties.get("ro.ota.firmware", "")).commit();  
        sp.edit().putString("firm_ver", SystemProperties.get("ro.product.firmware", "")).commit();
 
		//关闭usb调试
//		boolean enableAdb = (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
//		if(enableAdb)
//			Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
//		
		if(!mRunning){
			//步骤2：启动handerhandler这个线程;
			mht.start();
			
			mSntpHandler = new Handler(mht.getLooper());	
			mRunning = true;
		}

		
		if (DBUG)
			Logger.LOGD(TAG, " start SNTP Service !! ");
		startSyncSNTP();
	}

	public void startSyncSNTP() {
		mSntpHandler.postDelayed(mSntpTask, 10);
	}

	HandlerThread mht = new HandlerThread("hander_thread");
 
	
	private Handler mSntpHandler;

	private Runnable mSntpTask = new Runnable() {
		public void run() {
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					 mSntpHandler.removeCallbacks(mSntpTask);
					 if (!syncSNTP()) {
						 //如果失败，等3s继续执行，反复直到成功
						 mSntpHandler.postDelayed(mSntpTask, 3000);
					 }
					 else{
						 // 如果成功，则等2小时候再对时一次。
						 mSntpHandler.postDelayed(mSntpTask, 3600000*2);
					 }					
				}
			}).start();
		}
	};

	String [] mHosts = new String[]{"time.windows.com", "s1a.time.edu.cn", "time.nist.gov", "time-a.nist.gov", "time-b.nist.gov", "42.96.205.244"};
	int mHostIdx = 0;
	
	public boolean syncSNTP() {
		if(DBUG)
			Logger.LOGD(TAG, "fun syncSNTP ..");
		SntpClient client = new SntpClient();
		if (client.requestTime(mHosts[mHostIdx], 3000)) {

			long now = client.getNtpTime() + SystemClock.elapsedRealtime()
					- client.getNtpTimeReference();
			// Calendar ca = Calendar.getInstance(TimeZone.getDefault());
			// ca.getTimeInMillis();
			if(DBUG)
				Logger.LOGD(TAG, "now - System.currentTimeMillis()= "+ (now - System.currentTimeMillis()));
			
			//如果小于5秒，则默认不对时，说明时间也相差无几
			if (now - System.currentTimeMillis() < 5000){
				//return false;
				return true;  //要返回true，否则一直去服务器取数据。
			}

			CharSequence ch = DateFormat.format("hh:mm:ss", now);
			if (DBUG)
				Logger.LOGD(TAG, "ch=" + ch);
			CharSequence date = DateFormat.format("yyyy MM dd", now);
			if (DBUG)
				Logger.LOGD(TAG, "date=" + date);

			if (SystemClock.setCurrentTimeMillis(now)) {
				if (DBUG)
					Logger.LOGD(TAG,
							"set Current Time =" + System.currentTimeMillis());
			} else {
				if (DBUG)
					Logger.LOGD(TAG, " set Current Time  false !");
			}

		} else {
			if (DBUG)
				Logger.LOGD(TAG, "sntp request time false !!");
			
			mHostIdx ++;
			mHostIdx = mHostIdx % mHosts.length;
			
			return false;
		}

		return true;
	}

	public class SntpClient {
		private static final int NTP_PACKET_SIZE = 48;

		private static final int NTP_PORT = 123;

		private static final int NTP_MODE_CLIENT = 3;

		private static final int NTP_VERSION = 3;

		/*
		 * Number of seconds between Jan 1, 1900 and Jan 1, 1970 70 years plus
		 * 17 leap days
		 */
		private static final long OFFSET_1900_TO_1970 = ((365L * 70L) + 17L) * 24L * 60L * 60L;

		private static final int TRANSMIT_TIME_OFFSET = 40;

		private static final int ORIGINATE_TIME_OFFSET = 24;

		private static final int RECEIVE_TIME_OFFSET = 32;

		// system time computed from NTP server response
		private long mNtpTime;

		// value of SystemClock.elapsedRealtime() corresponding to mNtpTime
		private long mNtpTimeReference;

		// round trip time in milliseconds
		private long mRoundTripTime;

		public boolean requestTime(String host, int timeout) {

			try {
				DatagramSocket socket = new DatagramSocket();
				socket.setSoTimeout(timeout);

				InetAddress address = InetAddress.getByName(host);
				byte[] buffer = new byte[NTP_PACKET_SIZE];
				DatagramPacket request = new DatagramPacket(buffer,
						buffer.length, address, NTP_PORT);
				/*
				 * set mode = 3 (client) and version = 3 mode is in low 3 bits
				 * of first byte version is in bits 3-5 of first byte
				 */
				buffer[0] = NTP_MODE_CLIENT | (NTP_VERSION << 3);
				// get current time and write it to the request packet
				long requestTime = System.currentTimeMillis();
				if (DBUG)
					Logger.LOGD(TAG, "System.currentTimeMillis(): " + requestTime
							+ " ms");
				long requestTicks = SystemClock.elapsedRealtime();
				writeTimeStamp(buffer, TRANSMIT_TIME_OFFSET, requestTime);
				socket.send(request);

				// read the response
				DatagramPacket response = new DatagramPacket(buffer,
						buffer.length);
				socket.receive(response);
				long responseTicks = SystemClock.elapsedRealtime();
				long responseTime = requestTime
						+ (responseTicks - requestTicks);
				socket.close();

				// extract the results
				long originateTime = readTimeStamp(buffer,
						ORIGINATE_TIME_OFFSET);
				long receiveTime = readTimeStamp(buffer, RECEIVE_TIME_OFFSET);
				long transmitTime = readTimeStamp(buffer, TRANSMIT_TIME_OFFSET);
				long roundTripTime = responseTicks - requestTicks
						- (transmitTime - receiveTime);
				long clockOffset = (receiveTime - originateTime)
						+ (transmitTime - responseTime);
				if (DBUG)
					Logger.LOGD(TAG, "round trip: " + roundTripTime + " ms");
				if (DBUG)
					Logger.LOGD(TAG, "clock offset: " + clockOffset + " ms");

				// save our results
				mNtpTime = receiveTime;// requestTime + clockOffset;
				mNtpTimeReference = requestTicks;
				mRoundTripTime = roundTripTime;

			} catch (Exception e) {
				// TODO: handle exception
				if (DBUG)
					Logger.LOGD(TAG, "error: " + e);
				return false;
			}

			return true;
		}

		/**
		 * Returns the time computed from the NTP transaction.
		 * 
		 * @return time value computed from NTP server response.
		 */
		public long getNtpTime() {
			return mNtpTime;
		}

		/**
		 * Returns the reference clock value (value of
		 * SystemClock.elapsedRealtime()) corresponding to the NTP time.
		 * 
		 * @return reference clock corresponding to the NTP time.
		 */
		public long getNtpTimeReference() {

			return mNtpTimeReference;
		}

		/**
		 * Returns the round trip time of the NTP transaction
		 * 
		 * @return round trip time in milliseconds.
		 */
		public long getRoundTripTime() {
			return mRoundTripTime;
		}

		/**
		 * Reads an unsigned 32 bit big endian number from the given offset in
		 * the buffer.
		 */
		private long read32(byte[] buffer, int offset) {
			byte b0 = buffer[offset];
			byte b1 = buffer[offset + 1];
			byte b2 = buffer[offset + 2];
			byte b3 = buffer[offset + 3];

			// convert signed bytes to unsigned values
			int i0 = ((b0 & 0x80) == 0x80 ? (b0 & 0x7F) + 0x80 : b0);
			int i1 = ((b1 & 0x80) == 0x80 ? (b1 & 0x7F) + 0x80 : b1);
			int i2 = ((b2 & 0x80) == 0x80 ? (b2 & 0x7F) + 0x80 : b2);
			int i3 = ((b3 & 0x80) == 0x80 ? (b3 & 0x7F) + 0x80 : b3);
			return ((long) i0 << 24) + ((long) i1 << 16) + ((long) i2 << 8)
					+ (long) i3;
		}

		/**
		 * Reads the NTP time stamp at the given offset in the buffer and
		 * returns it as a system time (milliseconds since January 1, 1970).
		 */
		private long readTimeStamp(byte[] buffer, int offset) {
			long seconds = read32(buffer, offset);
			long fraction = read32(buffer, offset + 4);
			return ((seconds - OFFSET_1900_TO_1970) * 1000)
					+ ((fraction * 1000L) / 0x100000000L);
		}

		/**
		 * Writes system time (milliseconds since January 1, 1970) as an NTP
		 * time stamp at the given offset in the buffer.
		 */
		private void writeTimeStamp(byte[] buffer, int offset, long time) {
			long seconds = time / 1000L;
			long milliseconds = time - seconds * 1000L;
			seconds += OFFSET_1900_TO_1970;

			// write seconds in big endian format
			buffer[offset++] = (byte) (seconds >> 24);
			buffer[offset++] = (byte) (seconds >> 16);
			buffer[offset++] = (byte) (seconds >> 8);
			buffer[offset++] = (byte) (seconds >> 0);
			long fraction = milliseconds * 0x100000000L / 1000L;

			// write fraction in big endian format
			buffer[offset++] = (byte) (fraction >> 24);
			buffer[offset++] = (byte) (fraction >> 16);
			buffer[offset++] = (byte) (fraction >> 8);

			// low order bits should be random data
			buffer[offset++] = (byte) (Math.random() * 255.0);

		}
	}// class SntpClient

}