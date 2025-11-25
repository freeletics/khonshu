# Back clicks

`HostNavigator` has a `backPresses()` method that returns `Flow<Unit>` which will emit
whenever Android's back button is used. While this `Flow` is collected the default back handling
is disabled. This can be used to for example show a confirmation dialog before navigating back.

!!!note

    It's recommended that the `Flow` is only collected when the wanted behavior is to not
    navigate back when the Android back button is used. Collecting it and then making
    a decision of whether to do something else or call `navigateBack` should be avoided
    as it interferes with things like predictive back navigation.
