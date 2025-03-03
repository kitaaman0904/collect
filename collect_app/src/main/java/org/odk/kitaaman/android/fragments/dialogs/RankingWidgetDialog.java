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

package org.odk.kitaaman.android.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ItemTouchHelper.Callback;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.R.string;
import org.odk.kitaaman.android.adapters.RankingListAdapter;
import org.odk.kitaaman.android.utilities.QuestionFontSizeUtils;
import org.odk.kitaaman.android.utilities.RankingItemTouchHelperCallback;

import java.io.Serializable;
import java.util.List;

public class RankingWidgetDialog extends DialogFragment {

    private static final String ITEMS = "items";
    private static final String FORM_INDEX = "form_index";

    private RankingListener listener;

    private RankingListAdapter rankingListAdapter;
    private List<SelectChoice> items;
    private FormIndex formIndex;

    public interface RankingListener {
        void onRankingChanged(List<SelectChoice> items);
    }

    public static RankingWidgetDialog newInstance(List<SelectChoice> items, FormIndex formIndex) {
        RankingWidgetDialog dialog = new RankingWidgetDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ITEMS, (Serializable) items);
        bundle.putSerializable(FORM_INDEX, formIndex);
        dialog.setArguments(bundle);

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RankingListener) {
            listener = (RankingListener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        items = (List<SelectChoice>) (savedInstanceState == null
                ? getArguments().getSerializable(ITEMS)
                : savedInstanceState.getSerializable(ITEMS));
        formIndex = (FormIndex) (savedInstanceState == null
                ? getArguments().getSerializable(FORM_INDEX)
                : savedInstanceState.getSerializable(FORM_INDEX));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity())
                .setView(setUpRankingLayout())
                .setPositiveButton(string.ok, (dialog, id) -> {
                    listener.onRankingChanged(rankingListAdapter.getItems());
                    dismiss();
                })
                .setNegativeButton(string.cancel, (dialog, id) -> dismiss())
                .create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ITEMS, (Serializable) rankingListAdapter.getItems());
        outState.putSerializable(FORM_INDEX, formIndex);
        super.onSaveInstanceState(outState);
    }

    private NestedScrollView setUpRankingLayout() {
        LinearLayout rankingLayout = new LinearLayout(getContext());
        rankingLayout.setOrientation(LinearLayout.HORIZONTAL);
        rankingLayout.addView(setUpPositionsLayout());
        rankingLayout.addView(setUpRecyclerView());
        rankingLayout.setPadding(10, 0, 10, 0);

        NestedScrollView scrollView = new NestedScrollView(getContext());
        scrollView.addView(rankingLayout);
        return scrollView;
    }

    private LinearLayout setUpPositionsLayout() {
        LinearLayout positionsLayout = new LinearLayout(getContext());
        positionsLayout.setOrientation(LinearLayout.VERTICAL);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 10, 0);
        positionsLayout.setLayoutParams(layoutParams);

        for (SelectChoice item : items) {
            FrameLayout positionLayout = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.ranking_item, positionsLayout, false);
            TextView textView = positionLayout.findViewById(R.id.rank_item_text);
            textView.setText(String.valueOf(items.indexOf(item) + 1));
            textView.setTextSize(QuestionFontSizeUtils.getQuestionFontSize());

            positionsLayout.addView(positionLayout);
        }
        return positionsLayout;
    }

    private RecyclerView setUpRecyclerView() {
        rankingListAdapter = new RankingListAdapter(items, formIndex);

        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(rankingListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Callback callback = new RankingItemTouchHelperCallback(rankingListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return recyclerView;
    }
}
