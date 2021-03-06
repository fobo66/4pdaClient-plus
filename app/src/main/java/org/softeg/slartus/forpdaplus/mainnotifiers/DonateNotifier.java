package org.softeg.slartus.forpdaplus.mainnotifiers;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.prefs.DonateActivity;

/*
 * Created by slartus on 03.06.2014.
 */
public class DonateNotifier extends MainNotifier {
    public DonateNotifier(NotifiersManager notifiersManager) {
        super(notifiersManager,"Donate", 31);
    }

    public void start(FragmentActivity fragmentActivity){
        if(!needShow())
            return;
        saveTime();
        showNotify(fragmentActivity);
        saveSettings();
    }

    public void showNotify(final FragmentActivity fragmentActivity) {
        try {
            addToStack(new AlertDialogBuilder(fragmentActivity)
                            .setTitle("Неофициальный 4pda клиент")
                            .setMessage("Ваша поддержка - единственный стимул к дальнейшей разработке и развитию программы\n" +
                                    "\n" +
                                    "Вы можете сделать это позже через меню>>настройки>>Помочь проекту")
                            .setPositiveButton("Помочь проекту..",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                            Intent settingsActivity = new Intent(
                                                    fragmentActivity, DonateActivity.class);
                                            fragmentActivity.startActivity(settingsActivity);

                                        }
                                    }
                            )
                            .setNegativeButton("Позже",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();

                                        }
                                    }
                            ).create());
        } catch (Throwable ex) {
            Log.e(fragmentActivity, ex);
        }

    }

    protected boolean needShow() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
        if (prefs.getBoolean("donate.DontShow", false)) return false;


        String appVersion = getAppVersion(MyApp.getContext());
        if (prefs.getString("DonateShowVer", "").equals(appVersion)) {
            if (!isTime()) return false;
        }
        prefs.edit().putString("DonateShowVer",appVersion).commit();
        return true;
    }

    protected void saveSettings() {
        saveTime();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("DonateShowVer", getAppVersion(MyApp.getContext()));
        editor.commit();
    }
}
