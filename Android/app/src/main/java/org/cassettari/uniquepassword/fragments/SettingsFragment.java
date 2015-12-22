package org.cassettari.uniquepassword.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.cassettari.uniquepassword.R;
import org.cassettari.uniquepassword.listeners.OnInputsChangedListener;

public class SettingsFragment extends TitledFragment implements TextWatcher
{
	public static final int DEFAULT_MAXIMUM_LENGTH = 64;
	public static final String DEFAULT_SPECIAL_CHARACTERS = "!\"£$%^&*():@~<>?";

	private EditText textLength;
	private EditText textSpecials;
	private OnInputsChangedListener inputsChangedListener;

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);

		if (context instanceof OnInputsChangedListener)
		{
			inputsChangedListener = (OnInputsChangedListener) context;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View fragment = inflater.inflate(R.layout.fragment_settings, container, false);

		textLength = (EditText) fragment.findViewById(R.id.editLength);
		textSpecials = (EditText) fragment.findViewById(R.id.editSpecials);

		textLength.addTextChangedListener(this);
		textSpecials.addTextChangedListener(this);

		setLength(DEFAULT_MAXIMUM_LENGTH);
		setSpecials(DEFAULT_SPECIAL_CHARACTERS);

		return fragment;
	}

	public int getLength()
	{
		final String text = textLength.getText().toString();

		try
		{
			return Integer.parseInt(text);
		}
		catch (NumberFormatException ex)
		{
			return DEFAULT_MAXIMUM_LENGTH;
		}
	}

	public void setLength(int length)
	{
		textLength.setText(String.valueOf(length));
	}

	public String getSpecials()
	{
		return textSpecials.getText().toString();
	}

	public void setSpecials(String specials)
	{
		textSpecials.setText(specials);
	}

	@Override
	public String getPageTitle()
	{
		return "Options";
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
