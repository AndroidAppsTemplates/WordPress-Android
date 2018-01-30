package org.wordpress.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import org.wordpress.android.R;
import org.wordpress.android.analytics.AnalyticsTracker;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.store.AccountStore;
import org.wordpress.android.ui.RequestCodes;
import org.wordpress.android.ui.WPWebViewActivity;

import static org.wordpress.android.ui.WPWebViewActivity.JETPACK_CONNECTION_DEEPLINK;

public class JetpackUtils {

    public static void showJetpackNonConnectedAlert(final Activity activity, final SiteModel site, final AccountStore mAccountStore) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (site.isSelfHostedAdmin()) {
            builder.setMessage(activity.getString(R.string.jetpack_not_connected_message))
                    .setTitle(activity.getString(R.string.jetpack_not_connected));
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startJetpackFlow(site, mAccountStore, activity);
                    AnalyticsTracker.track(AnalyticsTracker.Stat.STATS_SELECTED_CONNECT_JETPACK);
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog. Hide current activity.
                    activity.finish();
                }
            });
        } else {
            builder.setMessage(activity.getString(R.string.jetpack_message_not_admin))
                    .setTitle(activity.getString(R.string.jetpack_not_found));
            builder.setPositiveButton(R.string.yes, null);
        }

        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // User pressed the back key. Hide current activity.
                activity.finish();
            }
        });
        dialog.show();
    }

    public static void showInstallJetpackAlert(final Activity activity, final SiteModel site, final AccountStore mAccountStore) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (site.isSelfHostedAdmin()) {
            builder.setMessage(activity.getString(R.string.jetpack_message))
                    .setTitle(activity.getString(R.string.jetpack_not_found));
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startJetpackFlow(site, mAccountStore, activity);
                    AnalyticsTracker.track(AnalyticsTracker.Stat.STATS_SELECTED_INSTALL_JETPACK);
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog. Hide current activity.
                    activity.finish();
                }
            });
        } else {
            builder.setMessage(activity.getString(R.string.jetpack_message_not_admin))
                    .setTitle(activity.getString(R.string.jetpack_not_found));
            builder.setPositiveButton(R.string.yes, null);
        }

        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // User pressed the back key. Hide current activity.
                activity.finish();
            }
        });
        dialog.show();
    }

    private static void startJetpackFlow(SiteModel site, AccountStore mAccountStore, Activity activity) {
        String stringToLoad = "https://wordpress.com/jetpack/connect?"
                + "url=" + site.getUrl()
                + "&mobile_redirect="
                + JETPACK_CONNECTION_DEEPLINK;
        if (mAccountStore.hasAccessToken()) {
            WPWebViewActivity.openJetpackConnectionFlow(activity, stringToLoad);
        } else {
            WPWebViewActivity.openUnauthorizedJetpackConnectionFlow(activity, stringToLoad);
        }
        activity.finish();
    }
}
