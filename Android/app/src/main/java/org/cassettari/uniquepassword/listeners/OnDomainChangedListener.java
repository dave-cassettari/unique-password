package org.cassettari.uniquepassword.listeners;

import org.cassettari.uniquepassword.KnownDomain;

public interface OnDomainChangedListener
{
	void onDomainChanged(KnownDomain knownDomain);
}
