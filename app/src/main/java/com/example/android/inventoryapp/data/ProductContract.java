package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProductContract {

    //content provider identifier
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    //the base of all uri's used in the app
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //uri path to browse product data
    public static final String PRODUCTS_PATH = "products";

    public static final class ProductEntry implements BaseColumns {

        //uri to access product data
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PRODUCTS_PATH);

        //MIME type for list of products
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PRODUCTS_PATH;

        //MIME type for single product
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PRODUCTS_PATH;

        //products database table
        public static final String TABLE_NAME = "products";

        //unique identifier for each row of data (INTEGER)
        public static final String COLUMN_PRODUCT_ID = BaseColumns._ID;
        //product name to display (TEXT)
        public static final String COLUMN_PRODUCT_NAME = "name";
        //price of the product (INTEGER)
        public static final String COLUMN_PRODUCT_PRICE = "price";
        //quantity of product in inventory (INTEGER)
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
        //user uploaded image of the product (BLOB)
        public static final String COLUMN_PRODUCT_IMAGE = "image";
    }
}
