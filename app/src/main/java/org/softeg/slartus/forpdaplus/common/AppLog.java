package org.softeg.slartus.forpdaplus.common;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.MalformedChunkCodingException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.App;

import java.net.SocketException;
import java.net.SocketTimeoutException;

public final class AppLog {

    private static final String TAG = "AppLog";

    public static void e(Throwable ex) {
        e(null, ex, null);
    }

    public static void e(Context context, Throwable ex) {
        e(context, ex, null);
    }

    public static void toastE(Context context, Throwable ex) {
        android.util.Log.e(TAG, ex.toString());
        try {
            Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
        } catch (Throwable ignoredEx) {

        }
    }

    public static void e(Context context, Throwable ex, Runnable netExceptionAction) {
        if (tryShowNetException(context != null ? context : App.getInstance(), ex, netExceptionAction))
            return;
        if (isException(ex, NotReportException.class)) {
            try {
                new AlertDialog.Builder(context)
                        .setTitle("Ошибка")
                        .setMessage(ex.getMessage())
                        .setPositiveButton("ОК", null).create().show();
            } catch (Throwable ignored) {
                org.acra.ACRA.getErrorReporter().handleException(ex);
            }
        } else {
            org.acra.ACRA.getErrorReporter().handleException(ex);

        }
    }

    public static boolean tryShowNetException(Context context, Throwable ex, final Runnable netExceptionAction) {
        try {

            String message = getLocalizedMessage(ex, null);
            if (message == null)
                return false;
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("Проверьте соединение")
                    .setMessage(message)
                    .setPositiveButton("ОК", null);


            if (netExceptionAction != null) {
                builder.setNegativeButton("Повторить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        netExceptionAction.run();
                    }
                });
            }
            builder.create().show();
            return true;

        } catch (Throwable loggedEx) {
            android.util.Log.e(TAG, ex.toString());
            return true;
        }
    }

    public static String getLocalizedMessage(Throwable ex, String defaultValue) {
        if (isHostUnavailableException(ex))
            return "Сервер недоступен или не отвечает";
        if (isTimeOutException(ex))
            return "Превышен таймаут ожидания";
        if (isException(ex, MalformedChunkCodingException.class))
            return "Целевой сервер не в состоянии ответить";
        if (isException(ex, SocketException.class))
            return "Соединение разорвано";
        return defaultValue;
    }

    private static Boolean isException(Throwable ex, Class c) {
        return isException(ex, false, c);
    }

    private static Boolean isException(Throwable ex, Boolean isCause, Class c) {
        return ex != null && (ex.getClass() == c || (!isCause && isException(ex.getCause(), true, c)));
    }

    private static Boolean isHostUnavailableException(Throwable ex) {
        return isHostUnavailableException(ex, false);
    }

    private static Boolean isHostUnavailableException(Throwable ex, Boolean isCause) {

        if (ex == null) return false;
        Class clazz = ex.getClass();
        return clazz == java.net.UnknownHostException.class ||
                clazz == HttpHostConnectException.class ||
                clazz == ClientProtocolException.class ||
                clazz == NoHttpResponseException.class ||
                clazz == org.apache.http.conn.HttpHostConnectException.class ||
                (!isCause && isHostUnavailableException(ex.getCause(), true));
    }

    private static Boolean isTimeOutException(Throwable ex) {
        return isTimeOutException(ex, false);
    }

    private static Boolean isTimeOutException(Throwable ex, Boolean isCause) {
        if (ex == null) return false;
        return( ex.getClass() == ConnectTimeoutException.class) || ex.getClass() == SocketTimeoutException.class ||
                (ex.getClass() == SocketException.class
                        && "recvfrom failed: ETIMEDOUT (Connection timed out)".equals(ex.getMessage())) ||
                (!isCause && isTimeOutException(ex.getCause(), true));
    }

    public static void i(Context mContext, Throwable ex) {
        Log.i(TAG,ex.toString());
    }

    public static void eToast(Context context, Throwable e) {
        toastE(context,e);
    }
}