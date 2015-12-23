package org.cassettari.uniquepassword.fragments;

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
import android.widget.ListView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import org.cassettari.uniquepassword.KnownDomain;
import org.cassettari.uniquepassword.PasswordActivity;
import org.cassettari.uniquepassword.R;
import org.cassettari.uniquepassword.fragments.adapters.DomainListAdapter;
import org.cassettari.uniquepassword.listeners.OnDomainChangedListener;
import org.cassettari.uniquepassword.services.DomainsService;
import org.cassettari.uniquepassword.services.callbacks.IgnoredCallback;
import org.cassettari.uniquepassword.services.callbacks.SuccessCallback;
import org.cassettari.uniquepassword.storage.DomainsDbHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class DomainsFragment extends TitledFragment
		implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
{
	private static final String API_BASE_URL = "https://uniquepassword.azurewebsites.net/api/";

	private DomainsService domains;
	private DomainsDbHelper databaseHelper;
	private DomainListAdapter listAdapter;
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

		values.put(DomainsDbHelper.DomainEntry.COLUMN_NAME_URL, knownDomain.getWebsite());
		values.put(DomainsDbHelper.DomainEntry.COLUMN_NAME_LENGTH, knownDomain.getMaximumLength());
		values.put(DomainsDbHelper.DomainEntry.COLUMN_NAME_SPECIALS, knownDomain.getSpecialCharacters());

		if (alreadyExisted)
		{
			listAdapter.remove(knownDomain);

			final String website = knownDomain.getWebsite();
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
				.setTitle(String.format("Remove %s?", knownDomain.getWebsite()))
				.setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						listAdapter.remove(knownDomain);

						final String website = knownDomain.getWebsite();
						final SQLiteDatabase db = databaseHelper.getWritableDatabase();
						final String selection = DomainsDbHelper.DomainEntry.COLUMN_NAME_URL + " LIKE ?";
						final String[] arguments = {website};

						db.delete(DomainsDbHelper.DomainEntry.TABLE_NAME, selection, arguments);

						domains.delete(website).enqueue(new IgnoredCallback<KnownDomain>());
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{

					}
				})
				.setIcon(R.drawable.ic_delete_grey600_48dp)
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

		listAdapter = new DomainListAdapter(getContext(), knownDomains);

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
				final String url = cursor.getString(cursor.getColumnIndex(DomainsDbHelper.DomainEntry.COLUMN_NAME_URL));
				final Integer length = cursor.getInt(cursor.getColumnIndex(DomainsDbHelper.DomainEntry.COLUMN_NAME_LENGTH));
				final String specials = cursor.getString(cursor.getColumnIndex(DomainsDbHelper.DomainEntry.COLUMN_NAME_SPECIALS));
				final KnownDomain domain = new KnownDomain(url);

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
						saveKnownDomain(domain.getWebsite(), domain.getMaximumLength(), domain.getSpecialCharacters(), false);
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
}
