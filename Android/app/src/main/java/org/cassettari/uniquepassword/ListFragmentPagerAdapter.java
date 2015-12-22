package org.cassettari.uniquepassword;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.cassettari.uniquepassword.fragments.TitledFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class ListFragmentPagerAdapter extends FragmentPagerAdapter
{
	private final TitledFragment[] fragments;

	public ListFragmentPagerAdapter(final FragmentManager fm, final TitledFragment... fragments)
	{
		super(fm);

		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(final int position)
	{
		if (position < 0 || position >= fragments.length)
		{
			return null;
		}

		return fragments[position];
	}

	@Override
	public int getCount()
	{
		return fragments.length;
	}

	@Override
	public CharSequence getPageTitle(final int position)
	{
		if (position < 0 || position >= fragments.length)
		{
			return null;
		}

		return fragments[position].getPageTitle();
	}
}
