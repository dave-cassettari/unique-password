package org.cassettari.uniquepassword;

import java.util.Objects;

public class KnownDomain
{
	private String domain;
	private Integer maximumLength;
	private String specialCharacters;

	public KnownDomain(String domain)
	{
		this.domain = domain;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof KnownDomain))
		{
			return false;
		}

		KnownDomain other = (KnownDomain)obj;

		return Objects.equals(domain, other.domain);
	}

	public String getDomain()
	{
		return domain;
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
