package org.odk.kitaaman.android.adapters;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.utilities.QuestionFontSizeUtils;
import org.odk.kitaaman.android.utilities.ThemeUtils;

public class SpinnerAdapter extends ArrayAdapter<CharSequence> {
    private final Context context;
    private final CharSequence[] items;
    private final ThemeUtils themeUtils;
    private int selectedPosition;

    public SpinnerAdapter(final Context context, final CharSequence[] objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
        this.items = objects;
        this.context = context;
        themeUtils = new ThemeUtils(context);
    }

    @Override
    // Defines the text view parameters for the drop down list entries
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView tv = convertView.findViewById(android.R.id.text1);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, QuestionFontSizeUtils.getQuestionFontSize());
        tv.setPadding(20, 10, 10, 10);
        tv.setText(position == items.length - 1
                ? parent.getContext().getString(R.string.clear_answer)
                : items[position]);

        if (position == (items.length - 1) && selectedPosition == position) {
            tv.setEnabled(false);
        } else {
            tv.setTextColor(selectedPosition == position ? themeUtils.getAccentColor() : themeUtils.getColorOnSurface());
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView tv = convertView.findViewById(android.R.id.text1);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, QuestionFontSizeUtils.getQuestionFontSize());
        tv.setPadding(10, 10, 10, 10);
        tv.setText(items[position]);

        return convertView;
    }

    public void updateSelectedItemPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }
}
