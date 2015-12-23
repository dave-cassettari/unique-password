package org.cassettari.uniquepassword.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import org.cassettari.uniquepassword.KnownDomain;
import org.cassettari.uniquepassword.PasswordActivity;
import org.cassettari.uniquepassword.R;
import org.cassettari.uniquepassword.listeners.OnDomainChangedListener;
import org.cassettari.uniquepassword.services.DomainsService;
import org.cassettari.uniquepassword.storage.DomainsDbHelper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class DomainsFragment extends TitledFragment
		implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
{
	private static final String API_BASE_URL = "https://uniquepassword.azurewebsites.net/api/";

	private DomainsService domains;
	private DomainsDbHelper databaseHelper;
	private KnownDomainAdapter listAdapter;
	private OnDomainChangedListener domainChangedListener;

	private final List<KnownDomain> knownDomains;

	public DomainsFragment()
	{
		knownDomains = new LinkedList<>();
	}

	public void saveKnownDomain(final String domain, Integer maxLength, String specialChars)
	{
		saveKnownDomain(domain, maxLength, specialChars, true);
	}

	public void saveKnownDomain(final String domain, Integer maxLength, String specialChars, boolean updateApi)
	{
		if (Objects.equals(maxLength, SettingsFragment.DEFAULT_MAXIMUM_LENGTH))
		{
			maxLength = null;
		}

		if (Objects.equals(specialChars, SettingsFragment.DEFAULT_SPECIAL_CHARACTERS))
		{
			specialChars = null;
		}

		KnownDomain knownDomain = new KnownDomain(domain);

		int existingIndex = knownDomains.indexOf(knownDomain);
		boolean alreadyExisted = (existingIndex >= 0);

		if (alreadyExisted)
		{
			knownDomain = knownDomains.get(existingIndex);
		}

		knownDomain.setMaximumLength(maxLength);
		knownDomain.setSpecialCharacters(specialChars);

		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		final ContentValues values = new ContentValues();

		values.put(DomainsDbHelper.DomainEntry.COLUMN_NAME_URL, knownDomain.getDomain());
		values.put(DomainsDbHelper.DomainEntry.COLUMN_NAME_LENGTH, knownDomain.getMaximumLength());
		values.put(DomainsDbHelper.DomainEntry.COLUMN_NAME_SPECIALS, knownDomain.getSpecialCharacters());

		if (alreadyExisted)
		{
			listAdapter.remove(knownDomain);

			final String website = knownDomain.getDomain();
			final String selection = DomainsDbHelper.DomainEntry.COLUMN_NAME_URL + " LIKE ?";
			final String[] arguments = {website};

			db.update(DomainsDbHelper.DomainEntry.TABLE_NAME, values, selection, arguments);

			if (updateApi)
			{
				domains.update(website, knownDomain).enqueue(new IgnoredCallback<KnownDomain>());
			}
		}
		else
		{
			db.insert(DomainsDbHelper.DomainEntry.TABLE_NAME, null, values);

			if (updateApi)
			{
				domains.create(knownDomain).enqueue(new IgnoredCallback<KnownDomain>());
			}
		}

		listAdapter.insert(knownDomain, 0);
	}

	private void loadKnownDomain(KnownDomain knownDomain)
	{
		if (domainChangedListener != null)
		{
			domainChangedListener.onDomainChanged(knownDomain);
		}
	}

	private void removeKnownDomain(final KnownDomain knownDomain)
	{
		new AlertDialog.Builder(getContext())
				.setTitle(String.format("Remove %s?", knownDomain.getDomain()))
				.setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						listAdapter.remove(knownDomain);

						final String domain = knownDomain.getDomain();
						final SQLiteDatabase db = databaseHelper.getWritableDatabase();
						final String selection = DomainsDbHelper.DomainEntry.COLUMN_NAME_URL + " LIKE ?";
						final String[] arguments = {domain};

						db.delete(DomainsDbHelper.DomainEntry.TABLE_NAME, selection, arguments);

						domains.delete(domain).enqueue(new IgnoredCallback<KnownDomain>());
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{

					}
				})
				.setIcon(android.R.drawable.ic_delete)
				.show();
	}

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);

		databaseHelper = new DomainsDbHelper(context);

		if (context instanceof OnDomainChangedListener)
		{
			domainChangedListener = (OnDomainChangedListener) context;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View fragment = inflater.inflate(R.layout.fragment_domains, container, false);
		final ListView listDomains = (ListView) fragment.findViewById(R.id.listDomains);

		listAdapter = new KnownDomainAdapter(getContext(), knownDomains);

		listDomains.setAdapter(listAdapter);
		listDomains.setOnItemClickListener(this);
		listDomains.setOnItemLongClickListener(this);
		listAdapter.setNotifyOnChange(true);

		final String sortOrder = DomainsDbHelper.DomainEntry.COLUMN_NAME_UPDATED_ON + " ASC";
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		final String[] projection = {
				DomainsDbHelper.DomainEntry.COLUMN_NAME_URL,
				DomainsDbHelper.DomainEntry.COLUMN_NAME_LENGTH,
				DomainsDbHelper.DomainEntry.COLUMN_NAME_SPECIALS,
		};

		final Cursor cursor = db.query(
				DomainsDbHelper.DomainEntry.TABLE_NAME,
				projection,
				null,
				null,
				null,
				null,
				sortOrder
		);

		if (cursor.moveToFirst())
		{
			do
			{
				String url = cursor.getString(cursor.getColumnIndex(DomainsDbHelper.DomainEntry.COLUMN_NAME_URL));
				Integer length = cursor.getInt(cursor.getColumnIndex(DomainsDbHelper.DomainEntry.COLUMN_NAME_LENGTH));
				String specials = cursor.getString(cursor.getColumnIndex(DomainsDbHelper.DomainEntry.COLUMN_NAME_SPECIALS));
				KnownDomain domain = new KnownDomain(url);

				if (length != 0)
				{
					domain.setMaximumLength(length);
				}

				domain.setSpecialCharacters(specials);

				listAdapter.add(domain);
			}
			while (cursor.moveToNext());
		}

		cursor.close();

		final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

		final OkHttpClient httpClient = new OkHttpClient();

		httpClient.interceptors().add(interceptor);

		final Retrofit api = new Retrofit.Builder()
				.baseUrl(API_BASE_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.client(httpClient)
				.build();

		domains = api.create(DomainsService.class);

		domains.get().enqueue(new SuccessCallback<List<KnownDomain>>()
		{
			@Override
			protected void onResult(final List<KnownDomain> domains)
			{
				if (domains != null)
				{
					Log.i(PasswordActivity.class.getName(), "Domains retrieved: " + domains.size());

					for (final KnownDomain domain : domains)
					{
						Log.i(PasswordActivity.class.getName(), "Domain: " + domain.getDomain() + ", " + domain.getMaximumLength() + ", " + domain.getSpecialCharacters());

						saveKnownDomain(domain.getDomain(), domain.getMaximumLength(), domain.getSpecialCharacters(), false);
					}
				}
			}
		});

		return fragment;
	}

	@Override
	public String getPageTitle()
	{
		return "Recent";
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		if (position < 0 || position >= knownDomains.size())
		{
			return;
		}

		loadKnownDomain(knownDomains.get(position));
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		if (position < 0 || position >= knownDomains.size())
		{
			return false;
		}

		removeKnownDomain(knownDomains.get(position));

		return true;
	}

	private class KnownDomainAdapter extends ArrayAdapter<KnownDomain>
	{
		public KnownDomainAdapter(Context context, List<KnownDomain> objects)
		{
			super(context, android.R.layout.two_line_list_item, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			KnownDomain item = getItem(position);

			if (view == null)
			{
				view = ((Activity) getContext()).getLayoutInflater().inflate(android.R.layout.two_line_list_item, null);
			}

			TextView textView1 = (TextView) view.findViewById(android.R.id.text1);
			TextView textView2 = (TextView) view.findViewById(android.R.id.text2);

			if (item != null)
			{
				String text2;

				if (item.getMaximumLength() != null && item.getSpecialCharacters() != null)
				{
					text2 = String.format("Length: %s, Extras: %s", item.getMaximumLength(), item.getSpecialCharacters());
				}
				else if (item.getMaximumLength() != null)
				{
					text2 = String.format("Length: %s", item.getMaximumLength());
				}
				else if (item.getSpecialCharacters() != null)
				{
					text2 = String.format("Extras: %s", item.getSpecialCharacters());
				}
				else
				{
					text2 = null;
				}

				textView1.setText(item.getDomain());
				textView2.setText(text2);
			}

			return view;
		}
	}

	private class IgnoredCallback<T> extends SuccessCallback<T>
	{
		@Override
		protected void onResult(T result)
		{
			Log.i(PasswordActivity.class.getName(), "IgnoredCallback.onResult: " + result);
		}
	}

	private abstract class SuccessCallback<T> implements Callback<T>
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
}
