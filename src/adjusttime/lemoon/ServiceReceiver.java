package adjusttime.lemoon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.util.Log;

public class ServiceReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent i = new Intent(context, SNTP.class);
			context.startService(i);
		} else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			ConnectivityManager connManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (null != connManager
					&& null != connManager.getActiveNetworkInfo()) {
				State state = connManager.getActiveNetworkInfo().getState();
				Logger.LOGD("tag", "state===============" + state);
				if (State.CONNECTED == state) {
					context.startService(new Intent(context, SNTP.class));
				}
			}
		}
	}
}