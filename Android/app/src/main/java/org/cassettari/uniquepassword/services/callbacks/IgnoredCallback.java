package org.cassettari.uniquepassword.services.callbacks;

import android.util.Log;

import org.cassettari.uniquepassword.PasswordActivity;

public class IgnoredCallback<T> extends SuccessCallback<T>
{
	@Override
	protected void onResult(T result)
	{
		Log.i(PasswordActivity.class.getName(), "IgnoredCallback.onResult: " + result);
	}
}