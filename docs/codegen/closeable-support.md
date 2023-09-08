# Support for Closeables

Khonshu's Codegen supports `java.io.Closeable`s by propagating each `Closeable` contributed
to a scoped Component to be bound to its internal `androidx.lifecycle.ViewModel`. When the
corresponding NavDestination is cleared from the back stack and hence `ViewModel.onCleared()`
is called, all bound `Closeable`s will be automatically closed.

This can be used to run any kind of cleanup you normally do in `ViewModel.onCleared()`.

=== "Kotlin CoroutineScope"

    ```kotlin
    // marker class for the scope
    sealed interface ExampleScope

    @Module
    @ContributesTo(ExampleScope::class)
    object ExampleModule {
        @Provides
        @SingleIn(ExampleScope::class)
        fun provideCoroutineScope(): CoroutineScope = MainScope()

        // Closeable to cancel CoroutineScope
        @Provides
        @IntoSet
        fun bindCoroutineScope(scope: CoroutineScope): Closeable = Closeable { scope.cancel() }
    }
    ```

=== "RxJava CompositeDisposable"

    ```kotlin
    // marker class for the scope
    sealed interface ExampleScope

    @Module
    @ContributesTo(ExampleScope::class)
    object ExampleModule {
        @Provides
        @SingleIn(ExampleScope::class)
        fun provideCompositeDisposable(): CompositeDisposable = CompositeDisposable()

        // Closeable to clear CompositeDisposable
        @Provides
        @IntoSet
        fun bindCompositeDisposable(disposable: CompositeDisposable): Closeable = Closeable { disposable.clear() }
    }
    ```
