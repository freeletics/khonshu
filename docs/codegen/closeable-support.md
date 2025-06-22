# Support for Closeables

Khonshu's Codegen supports providing `AutoCloseable`s to the graph of a destination. When the destination is cleared
from the back stack any provided `AutoCloseable` will be closed.

Examples:

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
        fun bindCoroutineScope(scope: CoroutineScope): AutoCloseable = AutoCloseable { scope.cancel() }
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
        fun bindCompositeDisposable(disposable: CompositeDisposable): AutoCloseable = AutoCloseable { disposable.clear() }
    }
    ```
