# Get started

Khonshu's Codegen is a plugin for [Anvil](https://github.com/square/anvil) that helps with
generating dependency injection related code and common boilerplate for screens.

## Advantages

- eliminate boilerplate that usually needs to be repeated on each screen
- optional integration with Navigator to simplify its setup 
- easily let objects survive configuration changes 

One other general advantage is around Fragments. If an app uses them or has to use them for legacy 
reasons, Codegen will mostly hide them from the developers because it generates the Fragment
for each screen it's used in. This way logic is kept out of them and the actual components like
UI and state machine (presenter/view model) can be tested more easily in isolation. It also makes
a migration away from Fragments easier since generated code can be easily replaced by other 
generated code. In fact migrating an app where each screen uses Codegen from Fragments to 
just using Compose is as easy as replacing a single annotation on each screen.


## Dependency

The library provides 2 different runtime implementations. One for `Fragment` based apps and one 
for pure `Compose` apps. If an app uses Compose but the composables are hosted inside fragments 
it falls into the `Fragment` category.

=== "Compose"

    ```groovy
    implementation("com.freeletics.khonshu:codegen-compose:<latest-version>")
    anvil("com.freeletics.khonshu:codegen-compiler:<latest-version>")
    ```

=== "Compose with Fragments"

    ```groovy
    implementation("com.freeletics.khonshu:codegen-fragment:<latest-version>")
    anvil("com.freeletics.khonshu:codegen-compiler:<latest-version>")
    ```

=== "Views with Fragments"

    ```kotlin
    implementation("com.freeletics.codegen:codegen-fragment:<latest-version>")
    anvil("com.freeletics.codegen:codegen-compiler:<latest-version>")
    ```


## Basic usage

=== "Compose"

    The `@ComposeScreen` annotation is added to the top level composable of a screen. This function 
    can have 2 parameters: the state that should be rendered and a lambda that allows
    the composable to send actions for user interactions. This will then generate
    another Composable function called `KhonshuExampleUi` and a Dagger component.
    
    ```kotlin
    @ComposeScreen(
        scope = ExampleScope::class,
        parentScope = AppScope::class, // AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class,
    )
    @Composable
    internal fun ExampleUi(
      state: ExampleState,
      sendAction: (ExampleAction) -> Unit
    ) {
      // composable logic ...
    }
    ```
    *`scope` and `parentScope` are described in the next section*

    The generated `KhonshuExampleUi` function will use the generated component, the
    annotated composable as well as the `stateMachine` parameter from the
    annotation. It will automatically hook up the
    state machine with the composable so that the state from the state machine
    is passed to the composable and actions from the latter are sent back to the
    state machine. The generated composable will use the generated component
    to obtain the state machine.

=== "Compose with Fragments"

    The `@ComposeFragment` annotation is added to the top level composable of a screen. This function 
    can have 2 parameters: the state that should be rendered and a lambda that allows
    the composable to send actions for user interactions. This will then generate
    a `Fragment` called `KhonshuExampleUiFragment` and a Dagger component.
    
    ```kotlin
    @ComposeFragment(
        scope = ExampleScope::class,
        parentScope = AppScope::class, // AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class,
    )
    @Composable
    internal fun ExampleUi(
      state: ExampleState,
      sendAction: (ExampleAction) -> Unit
    ) {
      // composable logic ...
    }
    ```
    *`scope` and `parentScope` are described in the next section*

    The generated `KhonshuExampleUiFragment` will use the generated component, the
    annotated composable as well as the `stateMachine` parameter from the
    annotation. It will use the composable as its view and automatically hook up the
    state machine with the composable so that the state from the state machine
    is passed to the composable and actions from the latter are sent back to the
    state machine. The generated fragment will use the generated component
    to obtain the state machine.

    The annotation has an optional `fragmentBaseClass` parameter that allows to
    specify a class other than `Fragment` to be used as super class for the
    generated Fragment. This allows using `DialogFragment` or `BottomSheetDialogFragment`
    for example.

=== "Views with Fragments"

    This is based on the separate [Renderer library](https://github.com/gabrielittner/renderer)
    which separates the view/ui logic from Fragments or other framework classes.
    Similar to composables above a Renderer receives a state object and emits
    actions. Codegen has a `@RendererFragment` annotation which needs to be added to the 
    Renderer class. This will then generate a `Fragment` called `KhonshuExampleUiFragment` 
    and a Dagger component.
    
    ```kotlin
    @RendererFragment(
        scope = ExampleScope::class,
        parentScope = AppScope::class, // AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class,
    )
    internal class ExampleRenderer @AssistedInject constructor(
        @Assisted private val binding: ExampleViewBinding,
    ) : ViewRenderer<ExampleState, ExampleAction>(binding) {
    
        override fun renderToView(state: ExampleState) {
            // view logic
        }
    
        @AssistedFactory
        abstract class Factory : ViewRenderer.Factory<ExampleViewBinding, ExampleRenderer>(ExampleViewBinding::inflate)
    }
    ```
    
    The generated `KhonshuExampleRendererFragment` will use the generated component, the
    annotated composable as well as the `stateMachine`. It will use the `ViewRenderer.Factory`
    to create an istance of the `Renderer` and use it as its view. It will then
    automatically hook up the state machine with the renderer so that the state from the state machine
    is passed to the composable and actions from the latter are sent back to the
    state machine. The generated fragment will use the generated component
    to obtain the state machine.

    The annotation has an optional `fragmentBaseClass` parameter that allows to
    specify a class other than `Fragment` to be used as super class for the
    generated Fragment. This allows using `DialogFragment` or `BottomSheetDialogFragment`
    for example.


## Generated component

All annotations have a `scope` and a `parentScope` parameter. These will be used in Anvil's 
`@ContributesSubcomponent` annotation on the generated subcomponent, i.e. 
`@ContributesSubcomponent(scope = ExampleScope::class, parentScope = AppScope::class)`.

Since the generated subcomponent is using `@ContributesSubcomponent`, it is possible
to use `@ContributesTo`, `@ContributesBinding` and so on with that same scope
to contribute objects into it.

`scope` is also used for Dagger scopes. The generated component is annotated
with the `@ScopeTo` annotation that ships with the Codegen runtime and uses
the `scope` value as a parameter. To scope a class just add
`@ScopeTo(ExampleScope::class)` to it. Any object using this scope will automatically 
survive configuration changes and will not be recreated together with the UI. In fact any
scoped object that is created in generated component will do so together with component itself.

A factory for the generated subcomponent is automatically generated and contributed to
the component that uses `parentScope` as its own scope. This component will be looked up internally
with `Context.getSystemService(name)` using the fully qualified name of the given `parentScope` as 
key for the lookup. It is expected that the app will provide it through its `Application` class or an
`Activity`.

For convenience purposes the generated component will make a `SavedStateHandle`
available which can be injected to classes like the state machine to save state.


## Example

This is a minimal example of how using Khonshu's Codegen for a screen would look like.

```kotlin
// marker class for the scope
sealed interface ExampleScope

// state machine survives orientation changes
@ScopeTo(ExampleScope::class)
internal class ExampleStateMachine @Inject constructor(
    val bundle: Bundle, // the arguments passed to this screen
    val repository: ExampleRepository, // a repository that pas provided somewhere in the app
) : StateMachine<ExampleState, ExampleAction> { 
    // ... 
}
```

=== "Compose"

    ```kotlin
    @ComposeScreen(
        scope = ExampleScope::class, // uses our marker class
        parentScope = AppScope::class, // the scope of the app level component, AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class, // the state machine used for this ui
    )
    @Composable
    internal fun ExampleUi(
        state: ExampleState,
        sendAction: (ExampleAction) -> Unit,
    ) { 
        // render the ui for ExampleState
    }
    ```

=== "Compose with Fragments"

    ```kotlin
    @ComposeFragment(
        scope = ExampleScope::class, // uses our marker class
        parentScope = AppScope::class, // the scope of the app level component, AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class, // the state machine used for this ui
    )
    @Composable
    internal fun ExampleUi(
        state: ExampleState,
        sendAction: (ExampleAction) -> Unit,
    ) { 
        // render the ui for ExampleState
    }
    ```

=== "Views with Fragments"

    ```kotlin
    @RendererFragment(
        scope = ExampleScope::class, // uses our marker class
        parentScope = AppScope::class, // the scope of the app level component, AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class, // the state machine used for this ui
    )
    internal class ExampleRenderer @AssistedInject constructor(
        @Assisted private val binding: ExampleViewBinding,
    ) : ViewRenderer<ExampleState, ExampleAction>(binding) {
    
        override fun renderToView(state: ExampleState) {
            // render the ui for ExampleState
        }
    
        @AssistedFactory
        abstract class Factory : ViewRenderer.Factory<ExampleViewBinding, ExampleRenderer>(ExampleViewBinding::inflate)
    }
    ```

Using this would require a one time setup in the app so that the screens can look up the `AppScope`
component through `getSystemService` to retrieve the parent component:

```kotlin
@AppScope
@MergeComponent(scope = AppScope::class)
interface AppComponent {
    // allows an Activity to get all generated NavDestinations to set up the NavHost
    val destinations: Set<NavDestination>
    
    @Component.Factory
    interface Factory {
        fun create(): AppComponent
    }
}

class App : Application() {

    private val component: AppComponent = DaggerAppComponent.factory().create(this)
    
    override fun getSystemService(name: String): Any {
        if (name == AppScope::class.qualifiedName) {
            return component
        }
        return super.getSystemService(name)
    }
}
```
