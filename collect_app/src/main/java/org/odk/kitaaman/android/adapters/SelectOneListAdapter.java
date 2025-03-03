/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.kitaaman.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.audio.AudioHelper;
import org.odk.kitaaman.android.widgets.AbstractSelectOneWidget;

import java.util.List;

public class SelectOneListAdapter extends AbstractSelectListAdapter
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private String selectedValue;
    private final int playColor;
    private final Boolean autoAdvance;
    private RadioButton selectedRadioButton;
    private View selectedItem;

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public SelectOneListAdapter(List<SelectChoice> items, String selectedValue, AbstractSelectOneWidget widget, int numColumns, FormEntryPrompt formEntryPrompt, ReferenceManager referenceManager, int answerFontSize, AudioHelper audioHelper, int playColor, Context context, Boolean autoAdvance) {
        super(items, widget, numColumns, formEntryPrompt, referenceManager, answerFontSize, audioHelper, context);
        this.selectedValue = selectedValue;
        this.playColor = playColor;
        this.autoAdvance = autoAdvance;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (noButtonsMode) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.select_item_layout, null));
        } else {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.quick_select_layout, null));
        }
    }

    @Override
    public void onClick(View v) {
        ((AbstractSelectOneWidget) widget).onClick();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (selectedRadioButton != null && buttonView != selectedRadioButton) {
                selectedRadioButton.setChecked(false);
                ((AbstractSelectOneWidget) widget).clearNextLevelsOfCascadingSelect();
            }
            selectedRadioButton = (RadioButton) buttonView;
            selectedValue = items.get((int) selectedRadioButton.getTag()).getValue();
        }
    }

    class ViewHolder extends AbstractSelectListAdapter.ViewHolder {
        ImageView autoAdvanceIcon;

        ViewHolder(View v) {
            super(v);
            if (noButtonsMode) {
                view = (FrameLayout) v;
            } else {
                autoAdvanceIcon = v.findViewById(R.id.auto_advance_icon);
                autoAdvanceIcon.setVisibility(autoAdvance ? View.VISIBLE : View.GONE);
                audioVideoImageTextLabel = v.findViewById(R.id.mediaLayout);
                audioVideoImageTextLabel.setPlayTextColor(playColor);
            }
        }

        void bind(final int index) {
            super.bind(index);
            if (noButtonsMode && filteredItems.get(index).getValue().equals(selectedValue)) {
                view.getChildAt(0).setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.select_item_border));
                selectedItem = view.getChildAt(0);
            }
        }
    }

    @Override
    RadioButton createButton(final int index, ViewGroup parent) {
        RadioButton radioButton = (RadioButton) LayoutInflater.from(parent.getContext()).inflate(R.layout.select_one_item, null);
        setUpButton(radioButton, index);
        radioButton.setOnClickListener(this);
        radioButton.setOnCheckedChangeListener(this);

        String value = filteredItems.get(index).getValue();

        if (value != null && value.equals(selectedValue)) {
            radioButton.setChecked(true);
        }

        return radioButton;
    }

    @Override
    void onItemClick(Selection selection, View view) {
        if (!selection.getValue().equals(selectedValue)) {
            if (selectedItem != null) {
                selectedItem.setBackground(null);
            }
            view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.select_item_border));
            selectedItem = view;
            selectedValue = selection.getValue();
        }
        ((AbstractSelectOneWidget) widget).onClick();
    }

    public void clearAnswer() {
        if (selectedRadioButton != null) {
            selectedRadioButton.setChecked(false);
        }
        selectedValue = null;
        if (selectedItem != null) {
            selectedItem.setBackground(null);
            selectedItem = null;
        }
        ((AbstractSelectOneWidget) widget).clearNextLevelsOfCascadingSelect();
    }

    public SelectChoice getSelectedItem() {
        if (selectedValue != null) {
            for (SelectChoice item : items) {
                if (selectedValue.equalsIgnoreCase(item.getValue())) {
                    return item;
                }
            }
        }
        return null;
    }
}