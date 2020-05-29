package org.odk.kitaaman.android.formentry;

import android.content.Context;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

import com.google.common.collect.ImmutableList;

import org.odk.kitaaman.android.R;
import org.odk.kitaaman.android.adapters.IconMenuListAdapter;
import org.odk.kitaaman.android.adapters.model.IconMenuItem;
import org.odk.kitaaman.android.javarosawrapper.FormController;
import org.odk.kitaaman.android.preferences.AdminKeys;
import org.odk.kitaaman.android.preferences.AdminSharedPreferences;
import org.odk.kitaaman.android.utilities.DialogUtils;

import java.util.List;

public class QuitFormDialog {

    private QuitFormDialog() {
    }

    public static AlertDialog show(Context context, FormController formController, Listener listener) {
        String title = (formController == null) ? null : formController.getFormTitle();
        if (title == null) {
            title = context.getString(R.string.no_form_loaded);
        }

        List<IconMenuItem> items;
        if ((boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_SAVE_MID)) {
            items = ImmutableList.of(new IconMenuItem(R.drawable.ic_save, R.string.keep_changes),
                    new IconMenuItem(R.drawable.ic_delete, R.string.do_not_save));
        } else {
            items = ImmutableList.of(new IconMenuItem(R.drawable.ic_delete, R.string.do_not_save));
        }

        ListView listView = DialogUtils.createActionListView(context);

        final IconMenuListAdapter adapter = new IconMenuListAdapter(context, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            IconMenuItem item = (IconMenuItem) adapter.getItem(position);
            if (item.getTextResId() == R.string.keep_changes) {
                listener.onSaveChangedClicked();
            } else {
                listener.onIgnoreChangesClicked();
            }
        });

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(
                        context.getString(R.string.quit_application, title))
                .setPositiveButton(context.getString(R.string.do_not_exit), (dialog, id) -> {
                    dialog.cancel();
                })
                .setView(listView)
                .create();
        alertDialog.show();
        return alertDialog;
    }

    public interface Listener {
        void onSaveChangedClicked();

        void onIgnoreChangesClicked();
    }
}
