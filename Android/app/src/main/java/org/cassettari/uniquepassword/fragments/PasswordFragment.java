package org.cassettari.uniquepassword.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.cassettari.uniquepassword.R;
import org.cassettari.uniquepassword.listeners.OnInputsChangedListener;
import org.cassettari.uniquepassword.listeners.OnPasswordCopiedListener;

public class PasswordFragment extends TitledFragment implements TextWatcher
{
	private EditText textDomain;
	private EditText textMaster;
	private EditText textHashed;
	private OnInputsChangedListener inputsChangedListener;
	private OnPasswordCopiedListener passwordCopiedListener;

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);

		if (context instanceof OnInputsChangedListener)
		{
			inputsChangedListener = (OnInputsChangedListener) context;
		}

		if (context instanceof OnPasswordCopiedListener)
		{
			passwordCopiedListener = (OnPasswordCopiedListener) context;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View fragment = inflater.inflate(R.layout.fragment_password, container, false);

		textMaster = (EditText) fragment.findViewById(R.id.editMaster);
		textDomain = (EditText) fragment.findViewById(R.id.editDomain);
		textHashed = (EditText) fragment.findViewById(R.id.editHashed);

		textMaster.addTextChangedListener(this);
		textDomain.addTextChangedListener(this);

		final Button buttonCopy = (Button) fragment.findViewById(R.id.buttonCopy);

		buttonCopy.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				CopyToClipboard();
			}
		});

		return fragment;
	}

	public String getDomain()
	{
		return textDomain.getText().toString();
	}

	public void setDomain(String domain)
	{
		textDomain.setText(domain);
	}

	public String getMaster()
	{
		return textMaster.getText().toString();
	}

	public void setMaster(String master)
	{
		textMaster.setText(master);
		textMaster.requestFocus();
	}

	public String getHashed()
	{
		return textHashed.getText().toString();
	}

	public void setHashed(String hashed)
	{
		textHashed.setText(hashed);
	}

	public void CopyToClipboard()
	{
		final String hashed = getHashed();

		if (hashed.length() == 0)
		{
			return;
		}

		final ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);

		if (clipboardManager != null)
		{
			final ClipData clipData = ClipData.newPlainText("Password", hashed);

			clipboardManager.setPrimaryClip(clipData);

			final Toast toast = Toast.makeText(getContext(), "Password Copied", Toast.LENGTH_SHORT);

			toast.show();
		}

		if (passwordCopiedListener != null)
		{
			passwordCopiedListener.onPasswordCopied();
		}
	}

	@Override
	public String getPageTitle()
	{
		return "Password";
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		if (inputsChangedListener != null)
		{
			inputsChangedListener.onInputsChanged();
		}
	}

	@Override
	public void afterTextChanged(Editable s)
	{

	}
}
