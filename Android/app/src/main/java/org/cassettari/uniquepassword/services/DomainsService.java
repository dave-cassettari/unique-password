package org.cassettari.uniquepassword.services;

import org.cassettari.uniquepassword.KnownDomain;

import java.util.List;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Query;

public interface DomainsService
{
	String END_POINT = "domains";
	String KEY_WEBSITE = "website";

	@GET(END_POINT)
	Call<List<KnownDomain>> get();

	@GET(END_POINT)
	Call<KnownDomain> get(@Query(KEY_WEBSITE) String website);

	@POST(END_POINT)
	Call<KnownDomain> create(@Body KnownDomain knownDomain);

	@PUT(END_POINT)
	Call<KnownDomain> update(@Query(KEY_WEBSITE) String website, @Body KnownDomain knownDomain);

	@DELETE(END_POINT)
	Call<KnownDomain> delete(@Query(KEY_WEBSITE) String website);
}