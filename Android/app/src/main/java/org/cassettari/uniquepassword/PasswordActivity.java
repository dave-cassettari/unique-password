package org.cassettari.uniquepassword;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.cassettari.uniquepassword.fragments.DomainsFragment;
import org.cassettari.uniquepassword.fragments.PasswordFragment;
import org.cassettari.uniquepassword.fragments.SettingsFragment;
import org.cassettari.uniquepassword.listeners.OnDomainChangedListener;
import org.cassettari.uniquepassword.listeners.OnInputsChangedListener;
import org.cassettari.uniquepassword.listeners.OnPasswordCopiedListener;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class PasswordActivity
		extends FragmentActivity
		implements OnInputsChangedListener, OnDomainChangedListener, OnPasswordCopiedListener
{
	public static final int DEFAULT_VERSION = 1;
	public static final int SPECIAL_CHARACTER_COUNT = 4;

	private ViewPager viewPager;
	private DomainsFragment fragmentDomains;
	private PasswordFragment fragmentPassword;
	private SettingsFragment fragmentSettings;

	private void Compute()
	{
		int length = fragmentSettings.getLength();
		String master = fragmentPassword.getMaster();
		String domain = fragmentPassword.getDomain();
		String specials = fragmentSettings.getSpecials();
		String computed = ComputeHash(master, domain, specials.toCharArray(), length, DEFAULT_VERSION);

		fragmentPassword.setHashed(computed);
	}

	@Override
	public void onInputsChanged()
	{
		Compute();
	}

	@Override
	public void onDomainChanged(KnownDomain knownDomain)
	{
		Integer maxLength = knownDomain.getMaximumLength();
		String specialChars = knownDomain.getSpecialCharacters();

		if (maxLength != null)
		{
			fragmentSettings.setLength(maxLength);
		}
		else
		{
			fragmentSettings.setLength(SettingsFragment.DEFAULT_MAXIMUM_LENGTH);
		}

		if (specialChars != null)
		{
			fragmentSettings.setSpecials(specialChars);
		}
		else
		{
			fragmentSettings.setSpecials(SettingsFragment.DEFAULT_SPECIAL_CHARACTERS);
		}

		fragmentPassword.setDomain(knownDomain.getDomain());
		fragmentPassword.setMaster(null);

		viewPager.setCurrentItem(1, true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_password);

		viewPager = (ViewPager) findViewById(R.id.container);
		fragmentDomains = new DomainsFragment();
		fragmentPassword = new PasswordFragment();
		fragmentSettings = new SettingsFragment();

		final ListFragmentPagerAdapter fragmentPagerAdapter = new ListFragmentPagerAdapter(
				getSupportFragmentManager(),
				fragmentDomains,
				fragmentPassword,
				fragmentSettings);

		viewPager.setAdapter(fragmentPagerAdapter);
		viewPager.setCurrentItem(1, false);
		viewPager.setOffscreenPageLimit(2);

		final FloatingActionButton actionButton = (FloatingActionButton) findViewById(R.id.fab);

		actionButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				fragmentPassword.CopyToClipboard();
			}
		});
	}

	@Override
	public void onPasswordCopied()
	{
		String domain = fragmentPassword.getDomain();
		Integer maxLength = fragmentSettings.getLength();
		String specialChars = fragmentSettings.getSpecials();

		if (Objects.equals(maxLength, SettingsFragment.DEFAULT_MAXIMUM_LENGTH))
		{
			maxLength = null;
		}

		if (Objects.equals(specialChars, SettingsFragment.DEFAULT_SPECIAL_CHARACTERS))
		{
			specialChars = null;
		}

		fragmentDomains.saveKnownDomain(domain, maxLength, specialChars);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		//getMenuInflater().inflate(R.menu.menu_password, menu);

		//return true;

		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private static String ComputeHash(final String master, final String domain, final char[] specialCharacters, final int maxLength, final int version)
	{
		if (master == null || master.length() == 0 || domain == null || domain.length() == 0)
		{
			return null;
		}

		MessageDigest digest;

		try
		{
			digest = MessageDigest.getInstance("SHA-256");
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();

			return null;
		}

		final String plainText = master + domain + String.valueOf(version);

		try
		{
			digest.update(plainText.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();

			return null;
		}

		final byte[] encryptedBytes = digest.digest();

		String password = String.format("%064x", new java.math.BigInteger(1, encryptedBytes));

		final int maximumLength = Math.min(maxLength, password.length());

		password = password.substring(0, maximumLength);

		if (specialCharacters != null && specialCharacters.length > 0)
		{
			final StringBuilder newPassword = new StringBuilder(password);

			int hashedCode = 0;

			java.util.Arrays.sort(specialCharacters);

			for (int i = 0; i < password.length(); i++)
			{
				hashedCode += (int) password.charAt(i);
			}

			int offset = hashedCode % specialCharacters.length;
			int frequency = (int) Math.floor((double) password.length() / SPECIAL_CHARACTER_COUNT);

			for (int i = 0; i < SPECIAL_CHARACTER_COUNT; i++)
			{
				char includeChar = specialCharacters[(offset + i) % specialCharacters.length];
				int includeIndex = (offset + frequency * i) % password.length();

				newPassword.setCharAt(includeIndex, includeChar);
			}

			password = newPassword.toString();
		}

		return password;
	}
}
