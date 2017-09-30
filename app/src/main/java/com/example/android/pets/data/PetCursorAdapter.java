package com.example.android.pets.data;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.R;

import static com.example.android.pets.data.PetsContract.PetEntry.COLUMN_PET_BREED;
import static com.example.android.pets.data.PetsContract.PetEntry.COLUMN_PET_NAME;
import static com.example.android.pets.data.PetsContract.PetEntry._ID;

/**
 * Created on 9/26/2017.
 *
 * {@link PetCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class PetCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link PetCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Find the pet name view and the pet breed/summary view in the list view
        TextView petNameView = (TextView) view.findViewById(R.id.name);
        TextView petSummaryView = (TextView) view.findViewById(R.id.summary);

        //get the pet name and summary from the database cursor
        String petName = cursor.getString(cursor.getColumnIndex(COLUMN_PET_NAME));
        String petSummary = cursor.getString(cursor.getColumnIndex(COLUMN_PET_BREED));

        //Check if the breed is unknown by checking if it was blank
        //and set it as unknown breed if it is blank
        if(TextUtils.isEmpty(petSummary)) {
            petSummary = context.getString(R.string.string_unknown_breed);
        }

        //Set the pet name and the pet summary in the text views
        petNameView.setText(petName);
        petSummaryView.setText(petSummary);
    }
}
