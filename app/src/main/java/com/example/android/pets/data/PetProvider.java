package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static android.R.attr.id;
import static com.example.android.pets.data.PetsContract.PATH_PETS;
import static com.example.android.pets.data.PetsContract.PetEntry.COLUMN_PET_BREED;
import static com.example.android.pets.data.PetsContract.PetEntry.COLUMN_PET_GENDER;
import static com.example.android.pets.data.PetsContract.PetEntry.COLUMN_PET_NAME;
import static com.example.android.pets.data.PetsContract.PetEntry.COLUMN_PET_WEIGHT;
import static com.example.android.pets.data.PetsContract.PetEntry.CONTENT_ITEM_TYPE;
import static com.example.android.pets.data.PetsContract.PetEntry.CONTENT_LIST_TYPE;
import static com.example.android.pets.data.PetsContract.PetEntry.CONTENT_URI;
import static com.example.android.pets.data.PetsContract.PetEntry.TABLE_NAME;
import static com.example.android.pets.data.PetsContract.PetEntry._ID;
import static com.example.android.pets.data.PetsContract.PetEntry;
import static com.example.android.pets.data.PetsContract.PetEntry.isValidGender;


/**
 * Created on 9/22/2017.
 * {@link ContentProvider} for Pets app.
 */

public class PetProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    private PetDbHelper mDbHelper;

    /** URI matcher code for the content URI for the pets table */
    public static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    public static final int PET_ID = 101;

    /** URI matcher object to match a context URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsContract.PATH_PETS, PETS);

        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PETS_ID}. This URI is used to provide access to ONE single row
        // of the pets table.

        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsContract.PATH_PETS + "/#", PET_ID);
    }

    @Override
    public boolean onCreate() {

        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = _ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name.");
        }

        //Check if gender is valid
        Integer gender = values.getAsInteger(COLUMN_PET_GENDER);
        if (gender == null || !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Invalid gender entered.");
        }

        //Check if weight is not a null
        Integer weight = values.getAsInteger(COLUMN_PET_WEIGHT);
        if (weight == null || weight < 0) {
            throw new IllegalArgumentException("Invalid weight entered.");
        }

        //No need to check breed value, any value is valid even null

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert the new pet with the given values
        long id = database.insert(TABLE_NAME, null, values);

        // notify all listeners of changes:
        getContext().getContentResolver().notifyChange(CONTENT_URI, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        Log.v(LOG_TAG,"ID is: " + id);
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = _ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if(values.containsKey(COLUMN_PET_NAME)) {
            String name = values.getAsString(COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires name");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(COLUMN_PET_GENDER);
            if (gender == null || !isValidGender(gender)) {
                throw new IllegalArgumentException("Invalid pet gender entered");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(COLUMN_PET_WEIGHT)) {
            Integer weight = values.getAsInteger(COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0){
                throw new IllegalArgumentException("Invalid pet weight entered");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        //Get a database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowNumber = database.update(TABLE_NAME, values, selection, selectionArgs);
        Log.v(LOG_TAG, "Updated number of rows : " + rowNumber);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowNumber != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        //return the number of rows updated
        return rowNumber;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        int deletedRows;
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                deletedRows = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                deletedRows = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (deletedRows != 0) {
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
        }
        return deletedRows;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return CONTENT_LIST_TYPE;
            case PET_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri + " with match " + match);
        }
    }
}
