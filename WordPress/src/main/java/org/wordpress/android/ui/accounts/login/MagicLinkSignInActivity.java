package org.wordpress.android.ui.accounts.login;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.wordpress.android.R;
import org.wordpress.android.models.Account;
import org.wordpress.android.models.AccountHelper;
import org.wordpress.android.ui.accounts.SignInActivity;

public class MagicLinkSignInActivity extends SignInActivity implements WPComMagicLinkFragment.OnMagicLinkFragmentInteraction, MagicLinkSignInFragment.OnMagicLinkRequestListener {
    private ProgressDialog mProgressDialog;

    @Override
    protected void onResume() {
        super.onResume();

        handleMagicLoginIntent();
    }



    @Override
    protected void onPause() {
        super.onPause();
        cancelProgressDialog();
    }

    private void cancelProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }

    @Override
    public MagicLinkSignInFragment getSignInFragment() {
        if (mSignInFragment != null && mSignInFragment instanceof MagicLinkSignInFragment) {
            return (MagicLinkSignInFragment) mSignInFragment;
        } else {
            return new MagicLinkSignInFragment();
        }
    }

    @Override
    public void onMagicLinkSent() {
        MagicLinkSentFragment magicLinkSentFragment = new MagicLinkSentFragment();
        slideInFragment(magicLinkSentFragment);
    }

    @Override
    public void onEnterPasswordRequested() {
        MagicLinkSignInFragment magicLinkSignInFragment = getSignInFragment();
        slideInFragment(magicLinkSignInFragment);
    }

    @Override
    public void onMagicLinkRequestSuccess(String email) {
        saveEmailToAccount(email);

        WPComMagicLinkFragment wpComMagicLinkFragment = WPComMagicLinkFragment.newInstance(email);
        slideInFragment(wpComMagicLinkFragment);
    }

    private void handleMagicLoginIntent() {
        String action = getIntent().getAction();
        Uri uri = getIntent().getData();

        if (Intent.ACTION_VIEW.equals(action) && uri != null) {
            if (uri.getHost().contains("magic-login")) {
                attemptLoginWithToken(uri);
            } else {
                // handle error
            }
        }
    }

    private void attemptLoginWithToken(Uri uri) {
        getSignInFragment().setToken(uri.getQueryParameter("token"));
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MagicLinkSignInFragment magicLinkSignInFragment = getSignInFragment();
        fragmentTransaction.replace(R.id.fragment_container, magicLinkSignInFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        mProgressDialog = ProgressDialog.show(this, "", "Logging in", true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                getSignInFragment().setToken("");
            }
        });
    }

    private void saveEmailToAccount(String email) {
        Account account = AccountHelper.getDefaultAccount();
        account.setUserName(email);
        account.save();
    }

    private void slideInFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.hs__slide_in_from_right, R.anim.hs__slide_out_to_left);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
