package org.odk.kitaaman.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.kitaaman.android.support.CopyFormRule;
import org.odk.kitaaman.android.support.ResetStateRule;
import org.odk.kitaaman.android.support.pages.MainMenuPage;

import java.util.Collections;

// Issue number NODK-207
@RunWith(AndroidJUnit4.class)
public class CascadingSelectWithNumberInHeaderTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("numberInCSV.xml", Collections.singletonList("itemSets.csv")));

    @Test
    public void fillForm_ShouldFillFormWithNumberInCsvHeader() {

        new MainMenuPage(rule)
                .startBlankForm("numberInCSV")
                .swipeToNextQuestion()
                .clickOnText("Venda de animais")
                .assertText("1a")
                .swipeToNextQuestion()
                .clickOnText("Vendas agrícolas")
                .assertText("2a")
                .swipeToNextQuestion()
                .clickOnText("Pensão")
                .assertText("3a")
                .swipeToNextQuestion()
                .swipeToEndScreen()
                .clickSaveAndExit();
    }
}
