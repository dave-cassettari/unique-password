package org.cassettari.uniquepassword.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.cassettari.uniquepassword.KnownDomain;
import org.cassettari.uniquepassword.R;
import org.cassettari.uniquepassword.listeners.OnDomainChangedListener;
import org.cassettari.uniquepassword.storage.DomainsDbHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DomainsFragment extends TitledFragment
		implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
{
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

			final String selection = DomainsDbHelper.DomainEntry.COLUMN_NAME_URL + " LIKE ?";
			final String[] arguments = {knownDomain.getDomain()};

			db.update( DomainsDbHelper.DomainEntry.TABLE_NAME, values, selection, arguments);
		}
		else
		{
			db.insert(DomainsDbHelper.DomainEntry.TABLE_NAME, null, values);
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

						final SQLiteDatabase db = databaseHelper.getWritableDatabase();
						final String selection = DomainsDbHelper.DomainEntry.COLUMN_NAME_URL + " LIKE ?";
						final String[] arguments = {knownDomain.getDomain()};

						db.delete(DomainsDbHelper.DomainEntry.TABLE_NAME, selection, arguments);
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
}
