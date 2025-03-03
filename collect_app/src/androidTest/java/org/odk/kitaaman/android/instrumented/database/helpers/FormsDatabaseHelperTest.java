package org.odk.kitaaman.android.instrumented.database.helpers;

import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.odk.kitaaman.android.dao.FormsDao;
import org.odk.kitaaman.android.database.helpers.FormsDatabaseHelper;
import org.odk.kitaaman.android.forms.Form;
import org.odk.kitaaman.android.utilities.FileUtils;
import org.odk.kitaaman.android.utilities.SQLiteUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.odk.kitaaman.android.database.helpers.FormsDatabaseHelper.FORMS_TABLE_NAME;
import static org.odk.kitaaman.android.instrumented.database.helpers.FormsDatabaseHelperTest.Action.DOWNGRADE;
import static org.odk.kitaaman.android.instrumented.database.helpers.FormsDatabaseHelperTest.Action.UPGRADE;
import static org.odk.kitaaman.android.support.FileUtils.copyFileFromAssets;

@RunWith(Parameterized.class)
public class FormsDatabaseHelperTest extends SqlLiteHelperTest {
    enum Action { UPGRADE, DOWNGRADE }

    private static final String DATABASE_PATH = FormsDatabaseHelper.getDatabasePath();

    @Parameterized.Parameter
    public Action action;

    @Parameterized.Parameter(1)
    public String description;

    @Parameterized.Parameter(2)
    public String dbFilename;

    @Before
    public void saveRealDb() {
        FileUtils.copyFile(new File(DATABASE_PATH), new File(DATABASE_PATH + TEMPORARY_EXTENSION));
    }

    @After
    public void restoreRealDb() {
        FileUtils.copyFile(new File(DATABASE_PATH + TEMPORARY_EXTENSION), new File(DATABASE_PATH));
    }

    @Parameterized.Parameters(name = "{1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {DOWNGRADE, "Downgrading from version with extra column drops that column", "forms_v7000_added_fakeColumn.db"},
                {DOWNGRADE, "Downgrading from version with missing column adds that column", "forms_v7000_removed_jrVersion.db"},

                {UPGRADE, "Upgrading from version with extra column and missing columns", "forms_v4.db"},
                {UPGRADE, "Upgrading from version with the same columns", "forms_v4_with_columns_from_v7.db"}
        });
    }

    @Test
    public void testMigration() throws IOException {
        copyFileFromAssets("database" + File.separator + dbFilename, DATABASE_PATH);
        FormsDatabaseHelper databaseHelper = new FormsDatabaseHelper();
        ensureMigrationAppliesFully(databaseHelper);

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        assertThat(db.getVersion(), is(FormsDatabaseHelper.DATABASE_VERSION));

        List<String> newColumnNames = SQLiteUtils.getColumnNames(db, FORMS_TABLE_NAME);

        assertThat(newColumnNames, contains(FormsDatabaseHelper.CURRENT_VERSION_COLUMN_NAMES));

        if (action.equals(UPGRADE)) {
            assertThatFormsAreKeptAfterUpgrading();
        }
    }

    private void assertThatFormsAreKeptAfterUpgrading() {
        FormsDao formsDao = new FormsDao();
        List<Form> forms = formsDao.getFormsFromCursor(formsDao.getFormsCursor());
        assertEquals(1, forms.size());
        assertEquals("2019051302", forms.get(0).getJrVersion());
        assertEquals("92ba8106dcb779943c1de163d73e1069", forms.get(0).getMD5Hash());
    }
}