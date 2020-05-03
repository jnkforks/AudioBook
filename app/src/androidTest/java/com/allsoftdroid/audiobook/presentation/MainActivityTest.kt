package com.allsoftdroid.audiobook.presentation

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.allsoftdroid.audiobook.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest{

    @Test
    fun openActivity_DisplayInUi(){
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.toolbar_title)).check(matches(withText("Latest Audio Books")))

        onView(withId(R.id.toolbar_downloads)).perform(click())

        pressBack()

        activityScenario.close()
    }
}