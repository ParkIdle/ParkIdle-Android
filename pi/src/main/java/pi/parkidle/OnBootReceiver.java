package pi.parkidle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by simonestaffa on 05/03/18.
 */

public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, MQTTSubscribe.class));
        context.startService(new Intent(context, MyLocationService.class));
    }


}
