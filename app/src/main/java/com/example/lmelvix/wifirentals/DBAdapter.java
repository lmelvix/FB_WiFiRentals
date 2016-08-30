
package com.example.lmelvix.wifirentals;

public class DBAdapter {


	private static final String TAG = "DBAdapter";
	
	// DB Fields
	public static final String KEY_ROWID = "_id";
	public static final int COL_ROWID = 0;

	// DB Column Names
	public static final String KEY_NAME = "name";
	public static final String KEY_MOBILENUM = "mobile_num";
	
	// DB Column numbers
	public static final int COL_NAME = 1;
	public static final int COL_MOBILE = 2;

	
	public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_NAME, KEY_MOBILENUM};
	
	// DB Initialization
	public static final String DATABASE_NAME = "MyDb";
	public static final String DATABASE_TABLE = "mainTable";

	// DB version - Update when changed
	public static final int DATABASE_VERSION = 2;	

    // CREATE LOCAL DB TABLE
	private static final String DATABASE_CREATE_SQL = 
			"create table " + DATABASE_TABLE 
			+ " (" + KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_NAME + " text not null, "
			+ KEY_MOBILENUM + " string not null"
			+ ");";
	
	// Get context from WiFiRentals which uses it.
	private final android.content.Context context;
	
	private com.example.lmelvix.wifirentals.DBAdapter.DatabaseHelper myDBHelper;
	private android.database.sqlite.SQLiteDatabase db;

    //Construct Database adapter object
	public DBAdapter(android.content.Context ctx) {
		this.context = ctx;
		myDBHelper = new com.example.lmelvix.wifirentals.DBAdapter.DatabaseHelper(context);
	}
	
	// Open the database connection.
	public com.example.lmelvix.wifirentals.DBAdapter open() {
		db = myDBHelper.getWritableDatabase();
		return this;
	}
	
	// Close the database connection.
	public void close() {
		myDBHelper.close();
	}
	
	// Add a new set of values to the database.
	public long insertRow(String name, String mobilenum) {

		// Create row's data:
		android.content.ContentValues initialValues = new android.content.ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_MOBILENUM, mobilenum);
		
		// Insert it into the database.
		return db.insert(DATABASE_TABLE, null, initialValues);
	}
	
	// Delete a row from the database, by rowId
	public boolean deleteRow(long rowId) {
		String where = KEY_ROWID + "=" + rowId;
		return db.delete(DATABASE_TABLE, where, null) != 0;
	}

    // Delete all entries in the table
	public void deleteAll() {
		android.database.Cursor c = getAllRows();
		long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
		if (c.moveToFirst()) {
			do {
				deleteRow(c.getLong((int) rowId));				
			} while (c.moveToNext());
		}
		c.close();
	}
	
	// Return all data in the database.
	public android.database.Cursor getAllRows() {
		String where = null;
		android.database.Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
							where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Get a specific row (by rowId)
	public android.database.Cursor getRow(long rowId) {
		String where = KEY_ROWID + "=" + rowId;
		android.database.Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
						where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	
	/**
	 * Private class to handle database creation and upgrading.
	 */
	private static class DatabaseHelper extends android.database.sqlite.SQLiteOpenHelper
	{
		DatabaseHelper(android.content.Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(android.database.sqlite.SQLiteDatabase _db) {
			_db.execSQL(DATABASE_CREATE_SQL);			
		}

		@Override
		public void onUpgrade(android.database.sqlite.SQLiteDatabase _db, int oldVersion, int newVersion) {
			android.util.Log.w(TAG, "Upgrading application's database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data!");
			
			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			
			// Recreate new database:
			onCreate(_db);
		}
	}
}
