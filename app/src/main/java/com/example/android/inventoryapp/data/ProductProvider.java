package com.example.android.inventoryapp.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class ProductProvider extends ContentProvider {

    private static final String LOG_TAG = "PRODUCTPROVIDER";
    //table matcher code
    private static final int PRODUCTS = 100;
    //single field matcher code
    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PRODUCTS_PATH, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PRODUCTS_PATH + "/#", PRODUCT_ID);
    }

    private ProductDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase sqLiteDatabase = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = sqLiteDatabase.query(ProductContract.ProductEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry.COLUMN_PRODUCT_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = sqLiteDatabase.query(ProductContract.ProductEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot quesy unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int match = sUriMatcher.match((uri));
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion failed for uri: " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        String productName = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        //check for fields that can't be null
        if (productName == null) {
            throw new IllegalArgumentException("Product name can't be null");
        }
        Integer productPrice = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        if (productPrice == null || productPrice < 0) {
            throw new IllegalArgumentException("Price can't have negative or null value");
        }
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();

        long rowId = sqLiteDatabase.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
        if (rowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for uri" + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, rowId);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry.COLUMN_PRODUCT_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update failed for uri: " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String productName = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            if (productName == null) {
                throw new IllegalArgumentException("Product name can't be null");
            }
        }
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Integer productPrice = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            if (productPrice == null || productPrice < 0) {
                throw new IllegalArgumentException("Price can't have negative or null value");
            }
        }
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer productQuantity = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (productQuantity == null || productQuantity < 0) {
                throw new IllegalArgumentException("Quantity can't be null or negative value");
            }
        }
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();
        int rowsUpdated = sqLiteDatabase.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();

        int rowsRemoved;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsRemoved = sqLiteDatabase.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry.COLUMN_PRODUCT_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsRemoved = sqLiteDatabase.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion failed for uri: " + uri);
        }
        if (rowsRemoved != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsRemoved;
    }


    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        //return MIME TYPE
        switch (match) {
            case PRODUCTS:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Error matching uri: " + uri + " + " + match);
        }
    }
}
