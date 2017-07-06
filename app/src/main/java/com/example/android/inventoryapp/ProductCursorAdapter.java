package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.product_title);
        int nameIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        nameTextView.setText(cursor.getString(nameIndex));

        TextView priceTextView = (TextView) view.findViewById(R.id.product_price);
        int priceIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        priceTextView.setText(String.valueOf(cursor.getInt(priceIndex) + context.getString(R.string.currency)));

        final TextView quantityTextView = (TextView) view.findViewById(R.id.product_quantity);
        int quantityIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        quantityTextView.setText(cursor.getString(quantityIndex));

        final TextView saleButton = (TextView) view.findViewById(R.id.product_sell);

        final int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        final int idColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_ID);
        final int productId = cursor.getInt(idColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modifiedQuantity;
                if (quantity > 0) {
                    modifiedQuantity = quantity - 1;
                } else {
                    Toast.makeText(context, context.getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
                    return;
                }
                //update database with new quantity value
                Uri currentUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, productId);
                ContentValues values = new ContentValues();
                values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, modifiedQuantity);

                context.getContentResolver().update(currentUri, values, null, null);
            }
        });
        int imageIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
        ImageView imageView = (ImageView) view.findViewById(R.id.product_image);
        byte[] blob = cursor.getBlob(imageIndex);
        if (blob != null) {
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(cursor.getBlob(imageIndex), 0, cursor.getBlob(imageIndex).length));
        }
    }
}
