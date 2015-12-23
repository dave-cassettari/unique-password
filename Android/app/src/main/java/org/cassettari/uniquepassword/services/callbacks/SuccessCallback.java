package org.cassettari.uniquepassword.services.callbacks;

import android.util.Log;

import com.squareup.okhttp.ResponseBody;

import org.cassettari.uniquepassword.PasswordActivity;

import java.io.IOException;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public abstract class SuccessCallback<T> implements Callback<T>
{
	protected abstract void onResult(T result);

	@Override
	public void onResponse(final Response<T> response, final Retrofit retrofit)
	{
		final ResponseBody errorBody = response.errorBody();

		if (errorBody != null)
		{
			try
			{
				final String errorText = errorBody.string();

				Log.e(PasswordActivity.class.getName(), errorText);

				return;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		final T body = response.body();

		if (body != null)
		{
			onResult(body);
		}
	}

	@Override
	public void onFailure(Throwable t)
	{
		Log.e(PasswordActivity.class.getName(), "SuccessCallback.onFailure: " + t);
	}
}