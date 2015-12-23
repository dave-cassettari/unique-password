package org.cassettari.uniquepassword;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class KnownDomain
{
	@SerializedName("website")
	private String website;
	@SerializedName("maximumLength")
	private Integer maximumLength;
	@SerializedName("specialCharacters")
	private String specialCharacters;

	public KnownDomain(String website)
	{
		this.website = website;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof KnownDomain))
		{
			return false;
		}

		KnownDomain other = (KnownDomain)obj;

		return Objects.equals(website, other.website);
	}

	public String getWebsite()
	{
		return website;
	}

	public Integer getMaximumLength()
	{
		return maximumLength;
	}

	public void setMaximumLength(Integer maximumLength)
	{
		this.maximumLength = maximumLength;
	}

	public String getSpecialCharacters()
	{
		return specialCharacters;
	}

	public void setSpecialCharacters(String specialCharacters)
	{
		this.specialCharacters = specialCharacters;
	}
}
