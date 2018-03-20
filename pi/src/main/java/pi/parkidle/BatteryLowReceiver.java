package pi.parkidle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;
import static pi.parkidle.MainActivity.editor;
import static pi.parkidle.MainActivity.sharedPreferences;

/**
 * Created by Matteo on 18/03/18.
 */

public class BatteryLowReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;
        Log.w("BATTERY RECEIVER","La mia percentuale di batteria è: "+batteryPct+" GRAZIE!");
        if(batteryPct <= 0.2){
            Log.w("BATTERY RECEIVER","Disattivo DetectedActivitiesIntentService => Batteria < 20%");
            context.stopService(new Intent(context,DetectedActivitiesIntentService.class));
        }
        /*Log.w("BATTERY RECEIVER","La mia percentuale di batteria è: "+batteryPct+" GRAZIE!");
        Log.w("BATTERY RECEIVER","La mia percentuale di batteria è: "+batteryPct+" GRAZIE!");
        Log.w("BATTERY RECEIVER","La mia percentuale di batteria è: "+batteryPct+" GRAZIE!");
        Log.w("BATTERY RECEIVER","La mia percentuale di batteria è: "+batteryPct+" GRAZIE!");*/


    }
}
