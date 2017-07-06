package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract;

import java.io.ByteArrayOutputStream;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER_ID = 0;
    //uri of currently displayed product
    private Uri mCurrentProductUri;
    //imagepicker result identifier
    private static final int RESULT_LOAD_IMAGE = 1;

    private ImageView mEditImage;
    private EditText mEditName;
    private EditText mEditPrice;
    private EditText mEditQuantity;
    //boolean value to track if the current product if being modified or not
    private boolean mIsProductModified = false;
    //set product status to being under modification when data fields are focused
    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mIsProductModified = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        //extract product uri from intent
        final Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        //edit activity title according to uri action
        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.activity_title_new));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.activity_title_edit));
            getLoaderManager().initLoader(PRODUCT_LOADER_ID, null, this);
        }
        //initialize data fields
        mEditImage = (ImageView) findViewById(R.id.product_edit_image);
        mEditName = (EditText) findViewById(R.id.edittext_product_name);
        mEditPrice = (EditText) findViewById(R.id.edittext_product_price);
        mEditQuantity = (EditText) findViewById(R.id.edittext_product_quantity);
        //set listener to data fields
        mEditImage.setOnTouchListener(mTouchListener);
        mEditName.setOnTouchListener(mTouchListener);
        mEditPrice.setOnTouchListener(mTouchListener);
        mEditQuantity.setOnTouchListener(mTouchListener);
        //set email intent on send order button
        TextView sendOrder = (TextView) findViewById(R.id.textview_send_order);
        sendOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailText = getEmailBody();
                if (emailText == null) {
                    Toast.makeText(EditActivity.this, getString(R.string.toast_order_input_error), Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferences preferences = view.getContext().getSharedPreferences("com.example.inventoryapp.email", Context.MODE_PRIVATE);
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", preferences.getString("com.example.inventoryapp.email", ""), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_email_subject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, emailText);
                startActivity(emailIntent);
            }
        });
        //tapping on image opens gallery to pick an image
        mEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//
                Intent i = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, ""), RESULT_LOAD_IMAGE);
            }
        });

        TextView decrementButton = (TextView) findViewById(R.id.decrement_button);
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //reduce quantity by one, in case of empty edittext defaults to 0
                mIsProductModified = true;
                int amount;
                if (!mEditQuantity.getText().toString().equals("")) {
                    amount = Integer.parseInt(mEditQuantity.getText().toString());
                } else {
                    amount = 1;
                }
                if (amount >= 1) {
                    mEditQuantity.setText(String.valueOf(amount - 1));
                }
            }
        });
        TextView incrementButton = (TextView) findViewById(R.id.increment_button);
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //increase quantity by one, in case of empty field set to 1
                mIsProductModified = true;
                int amount;
                if (!mEditQuantity.getText().toString().equals("")) {
                    amount = Integer.parseInt(mEditQuantity.getText().toString());
                } else {
                    amount = 0;
                }
                mEditQuantity.setText(String.valueOf(++amount));
            }
        });
    }

    //create string for e-mail body
    private String getEmailBody() {
        String productName = mEditName.getText().toString();
        EditText orderQuantityEt = (EditText) findViewById(R.id.edittext_order_quantity);
        String productQuantity = orderQuantityEt.getText().toString();
        if (TextUtils.isEmpty(productName) || TextUtils.isEmpty(productQuantity)) {
            return null;
        }
        return getString(R.string.order_email_body1) + productQuantity + " "
                + productName + getString(R.string.order_email_body2);
    }

    private void saveProduct() {
        //get data from edittext and image fields
        String nameString = mEditName.getText().toString().trim();
        String priceString = mEditPrice.getText().toString().trim();
        String quantityString = mEditQuantity.getText().toString().trim();
        if ((TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString)) ||
                mEditImage.getDrawable() == null || TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.toast_input_error), Toast.LENGTH_LONG).show();
            return;
        }
        //convert drawable from imageview for storing in database
        byte[] imageInByte;
        //convert image to bytearray for database storage
        Bitmap bitmap = ((BitmapDrawable) mEditImage.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOs);
        imageInByte = byteArrayOs.toByteArray();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(quantityString) && mEditImage.getDrawable() == null) {
            //currently not editing existing product and all fields are blank so nothing to save
            finish();
            return;
        }
        //creating ContentValues to store values to write into database
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        int price = Integer.parseInt(priceString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, price);
        int quantity = Integer.parseInt(quantityString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, imageInByte);

        if (mCurrentProductUri == null) {  //create new entry in database
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.insert_data_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.insert_data_success), Toast.LENGTH_SHORT).show();
            }
        } else { //update existing entry in database
            int rowsUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsUpdated == 0) {
                Toast.makeText(this, getString(R.string.update_data_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.update_data_success), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            //get image picked from gallery
            Uri selectedImage = data.getData();
            String picturePath = getImagePath(selectedImage);
            //resize and display image
            Bitmap bm = BitmapFactory.decodeFile(picturePath);
            Matrix m = new Matrix();
            m.setRectToRect(new RectF(0, 0, bm.getWidth(), bm.getHeight()), new RectF(0, 0, 160, 160), Matrix.ScaleToFit.CENTER);
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            mEditImage.setImageBitmap(bm);
        }
    }

    public String getImagePath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    private void deleteProduct() {
        //check if there are an existing product
        if (mCurrentProductUri != null) {
            int rowsRemoved = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsRemoved == 0) {
                Toast.makeText(this, getString(R.string.delete_data_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_data_success), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE
        };
        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            byte[] image = cursor.getBlob(imageColumnIndex);
            //display database values on edittext fields
            mEditName.setText(name);
            mEditPrice.setText(Integer.toString(price));
            mEditQuantity.setText(Integer.toString(quantity));
            if (image != null) {
                mEditImage.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mEditName.setText("");
        mEditPrice.setText("");
        mEditQuantity.setText("");
        mEditImage.setImageResource(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //only show delete option if we are in editing mode
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                //save and exit activity
                saveProduct();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                //return to main menu if no data to save
                if (!mIsProductModified) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }
                //show confirmation dialog to discard changes
                DialogInterface.OnClickListener discardButtonListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mIsProductModified) {
            super.onBackPressed();
            return;
        }
        //confirmation dialog for discarding changes
        DialogInterface.OnClickListener discardButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonListener) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(getString(R.string.discard_dialog_title));
        dialogBuilder.setPositiveButton(getString(R.string.discard_dialog_discard), discardButtonListener);
        dialogBuilder.setNegativeButton(getString(R.string.discard_dialog_cancel), new DialogInterface.OnClickListener() {
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

    private void showDeleteConfirmationDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(getString(R.string.delete_dialog_title));
        dialogBuilder.setPositiveButton(getString(R.string.delete_dialog_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteProduct();
            }
        });
        dialogBuilder.setNegativeButton(getString(R.string.delete_dialog_cancel), new DialogInterface.OnClickListener() {
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
}
