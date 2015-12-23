package org.cassettari.uniquepassword.fragments.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.cassettari.uniquepassword.KnownDomain;

import java.util.List;

public class DomainListAdapter extends ArrayAdapter<KnownDomain>
{
	private static final int ITEM_VIEW_RESOURCE = android.R.layout.simple_list_item_1;

	private final LayoutInflater inflater;

	public DomainListAdapter(Context context, List<KnownDomain> objects)
	{
		super(context, ITEM_VIEW_RESOURCE, 0, objects);

		inflater = ((Activity)getContext()).getLayoutInflater();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = convertView;

		if (view == null)
		{
			view = inflater.inflate(ITEM_VIEW_RESOURCE, null);
		}

		final KnownDomain item = getItem(position);
		final TextView textView1 = (TextView) view.findViewById(android.R.id.text1);

		if (item != null)
		{
			textView1.setText(item.getWebsite());
		}

		return view;
	}
}
