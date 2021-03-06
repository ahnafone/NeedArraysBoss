package com.example.moodtracker;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertFalse;

/**
 * Test class for MainActivity. All the UI tests are written here. Robotium test framework is
 used
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private Solo solo;
    @Rule
    public ActivityTestRule<MainActivity> rule =
            new ActivityTestRule<>(MainActivity.class, true, true);
    public ActivityTestRule<Login> rule2 =
            new ActivityTestRule<>(Login.class, false, false);

    /**
     * Runs before all tests and creates solo instance.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception{

        solo = new Solo(InstrumentationRegistry.getInstrumentation(),rule.getActivity());
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            //solo = new Solo(InstrumentationRegistry.getInstrumentation(),rule2.getActivity());
            logTestUserIn();
        }
        solo = new Solo(InstrumentationRegistry.getInstrumentation(),rule.getActivity());

    }

    /**
     * Runs if the user isn't logged in for any of the tests.
     */
    public void logTestUserIn() {
        solo.typeText((EditText) solo.getView(R.id.email),"test@mood.com");
        solo.enterText((EditText) solo.getView(R.id.password), "123456");
        solo.clickOnButton("Sign In");
        solo.sleep(2000);
    }

    /**
     * Gets the Activity
     * @throws Exception
     */
    @Test
    public void start() throws Exception{
        Activity activity = rule.getActivity();
    }

    /**
     * Add a mood event and check thoughts using assertTrue
     */

    @Test
    public void checkAddMood(){

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnActionBarHomeButton();
        View addMood = solo.getView(R.id.addMoodEvent);
        solo.clickOnView(addMood); //Click Floating Action Button that adds a new mood event
        solo.pressSpinnerItem(2,4);
        solo.enterText((EditText) solo.getView(R.id.reasonET), "UITest1");
        solo.pressSpinnerItem(3,2);
        solo.clickOnButton("Add Event"); //Click ADD EVENT Button
        assertTrue(solo.waitForText("UITest1", 1, 2000));
        //Cleanup
        solo.clickInRecyclerView(0);
        solo.clickOnButton("Delete Event"); //Click DELETE EVENT Button
    }

    @Test
    public void checkAddMoodWithoutReason(){

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnActionBarHomeButton();
        View addMood = solo.getView(R.id.addMoodEvent);
        solo.clickOnView(addMood); //Click Floating Action Button that adds a new mood event
        solo.pressSpinnerItem(2,4);
        solo.pressSpinnerItem(3,2);
        solo.clickOnButton("Add Event"); //Click ADD EVENT Button
        assertTrue(solo.waitForText("happy", 1, 2000));
        //Cleanup
        solo.clickInRecyclerView(0);
        solo.clickOnButton("Delete Event"); //Click DELETE EVENT Button
    }

    /**
     * Edit mood event and check thoughts using assertTrue
     */

    @Test
    public void checkEditMood(){

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        // Initialize test element
        solo.clickOnActionBarHomeButton();
        View addMood = solo.getView(R.id.addMoodEvent);
        solo.clickOnView(addMood); //Click Floating Action Button that adds a new mood event
        solo.pressSpinnerItem(2,4);
        solo.enterText((EditText) solo.getView(R.id.reasonET), "UITest1");
        solo.pressSpinnerItem(3,2);
        solo.clickOnButton("Add Event"); //Click ADD EVENT Button
        // Test
        assertTrue(solo.waitForText("UITest1", 1, 2000));
        solo.clickInRecyclerView(0);
        solo.pressSpinnerItem(0,3);
        solo.clearEditText((EditText) solo.getView(R.id.reasonET2));
        solo.enterText((EditText) solo.getView(R.id.reasonET2), "UITest2");
        solo.pressSpinnerItem(1,1);
        solo.clickOnButton("Edit Event"); //Click EDIT EVENT Button
        solo.sleep(1000);
        assertFalse(solo.waitForText("UITest1", 1, 2000));
        solo.sleep(1000);
        assertTrue(solo.waitForText("UITest2", 1, 2000));
        //Cleanup
        solo.clickInRecyclerView(0);
        solo.clickOnButton("Delete Event"); //Click DELETE EVENT Button
    }

    /**
     * Delete mood event and check thoughts using assertTrue
     */

    @Test
    public void checkDeleteMood(){

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        // Initialize test element
        solo.clickOnActionBarHomeButton();
        View addMood = solo.getView(R.id.addMoodEvent);
        solo.clickOnView(addMood); //Click Floating Action Button that adds a new mood event
        solo.pressSpinnerItem(2, 4);
        solo.enterText((EditText) solo.getView(R.id.reasonET), "UITest2");
        solo.pressSpinnerItem(3, 2);
        solo.clickOnButton("Add Event"); //Click ADD EVENT Button
        // Test
        assertTrue(solo.waitForText("UITest2", 1, 2000));
        solo.clickInRecyclerView(0);
        solo.clickOnButton("Delete Event"); //Click DELETE EVENT Button
        solo.sleep(1000);
        assertFalse(solo.waitForText("UITest2", 1, 2000));
    }

    @Test
    public void checkNavigation(){
        
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        // Initialize test element
        solo.clickOnActionBarHomeButton();
        solo.clickOnActionBarItem(1);
        solo.clickOnActionBarItem(2);
    }

    /**
     * Closes the activity after each test
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }

}
