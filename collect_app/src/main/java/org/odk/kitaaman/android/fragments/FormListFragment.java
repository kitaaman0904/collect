/*

Copyright 2017 Shobhit
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.odk.kitaaman.android.fragments;

import org.odk.kitaaman.android.provider.FormsProviderAPI.FormsColumns;

import static org.odk.kitaaman.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.odk.kitaaman.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.odk.kitaaman.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static org.odk.kitaaman.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;

public abstract class FormListFragment extends FileManagerFragment {
    protected String getSortingOrder() {
        String sortOrder = FormsColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";
        switch (getSelectedSortingOrder()) {
            case BY_NAME_ASC:
                sortOrder = FormsColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";
                break;
            case BY_NAME_DESC:
                sortOrder = FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC";
                break;
            case BY_DATE_ASC:
                sortOrder = FormsColumns.DATE + " ASC";
                break;
            case BY_DATE_DESC:
                sortOrder = FormsColumns.DATE + " DESC";
                break;
        }
        return sortOrder;
    }
}
