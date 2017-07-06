package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.ProductContract;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //tag for logging
    private static final String LOG_TAG = InventoryActivity.class.getSimpleName();
    //id for database loader
    private static final int PRODUCT_LOADER_ID = 0;
    //for saving supplier e-mail
    private SharedPreferences mPreferences;
    private ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        FloatingActionButton floatingButton = (FloatingActionButton) findViewById(R.id.floating_button);
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent activityIntent = new Intent(InventoryActivity.this, EditActivity.class);
                startActivity(activityIntent);
            }
        });

        ListView productList = (ListView) findViewById(R.id.list_view);
        //set view to show if list is empty
        TextView emptyView = (TextView) findViewById(R.id.empty_view);
        productList.setEmptyView(emptyView);
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productList.setAdapter(mCursorAdapter);
        //open editing activity of selected product
        productList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent activityIntent = new Intent(InventoryActivity.this, EditActivity.class);
                Uri currentUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, l);
                activityIntent.setData(currentUri);
                startActivity(activityIntent);
            }
        });
        //start up loader
        getLoaderManager().initLoader(PRODUCT_LOADER_ID, null, this);
    }

    private void deleteAllDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(getString(R.string.delete_all_message));
        dialogBuilder.setPositiveButton(getString(R.string.delete_all_continue), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int rowsRemoved = getContentResolver().delete(ProductContract.ProductEntry.CONTENT_URI, null, null);
                if (rowsRemoved == -1) {
                    Log.e(LOG_TAG, "Deleting entries failed");
                }
            }
        });
        dialogBuilder.setNegativeButton(getString(R.string.delete_all_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductContract.ProductEntry.COLUMN_PRODUCT_ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE
        };
        return new CursorLoader(this, ProductContract.ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //update cursor with product info
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //empty cursor
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all:
                deleteAllDialog();
                return true;
            case R.id.action_add_supplier:
                showSupplierEditor();
                return true;
        }
        return false;
    }

    private void showSupplierEditor() {
        //initialize sharedpreferences to read and edit
        mPreferences = this.getSharedPreferences("com.example.inventoryapp.email", Context.MODE_PRIVATE);
        //display dialog with edittext
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(getString(R.string.edit_supplier_dialogmessage));
        final EditText input = new EditText(InventoryActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        dialogBuilder.setView(input);
        //display existing supplier email address
        input.setText(mPreferences.getString("com.example.inventoryapp.email", ""));
        dialogBuilder.setPositiveButton(getString(R.string.edit_supplier_save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //save info into sharedpreferences
                String newMail = input.getText().toString();
                mPreferences.edit().putString("com.example.inventoryapp.email", newMail).apply();
            }
        });
        dialogBuilder.setNegativeButton(getString(R.string.edit_supplier_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //exit alertdialog without saving
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
}
