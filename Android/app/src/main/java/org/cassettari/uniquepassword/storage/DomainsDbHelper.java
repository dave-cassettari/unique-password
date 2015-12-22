package org.cassettari.uniquepassword.storage;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DomainsDbHelper extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "settings.db";
	private static final String SQL_CREATE =
			"CREATE TABLE " + DomainEntry.TABLE_NAME + " (" +
					DomainEntry.COLUMN_NAME_URL + " TEXT PRIMARY KEY NOT NULL, " +
					DomainEntry.COLUMN_NAME_LENGTH + " INTEGER, " +
					DomainEntry.COLUMN_NAME_SPECIALS + " TEXT," +
					DomainEntry.COLUMN_NAME_UPDATED_ON + " DATE DEFAULT CURRENT_DATE);";
	private static final String SQL_DELETE =
			"DROP TABLE IF EXISTS " + DomainEntry.TABLE_NAME;

	public DomainsDbHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL(SQL_DELETE);

		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		onUpgrade(db, oldVersion, newVersion);
	}

	public static abstract class DomainEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "domain";
		public static final String COLUMN_NAME_URL = "url";
		public static final String COLUMN_NAME_LENGTH = "length";
		public static final String COLUMN_NAME_SPECIALS = "specials";
		public static final String COLUMN_NAME_UPDATED_ON = "updated_on";
	}
}