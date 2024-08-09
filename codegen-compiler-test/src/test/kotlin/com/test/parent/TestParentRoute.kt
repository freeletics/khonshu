package com.test.parent

import android.os.Parcel
import com.freeletics.khonshu.navigation.NavRoute

public class TestParentRoute : NavRoute {
    override fun describeContents(): Int = 0

    override fun writeToParcel(p0: Parcel, p1: Int) {}
}
