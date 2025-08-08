@file:Suppress("ktlint:standard:filename")
package com.freeletics.khonshu.navigation

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

/**
 * Class that exposes a [results] [Flow] that can be used to observe results for
 * the given [key].
 *
 * See [ResultOwner] and [ResultNavigator.registerForNavigationResult].
 */
public class NavigationResultRequest<R : Parcelable> @InternalNavigationTestingApi constructor(
    public val key: Key<R>,
    @property:InternalNavigationTestingApi
    public val savedStateHandle: SavedStateHandle,
) : ResultOwner<R> {
    override val results: Flow<R>
        get() = savedStateHandle.getStateFlow<Parcelable>(key.requestKey, InitialValue)
            .mapNotNull {
                if (it != InitialValue) {
                    savedStateHandle[key.requestKey] = InitialValue
                    @Suppress("UNCHECKED_CAST")
                    it as R
                } else {
                    null
                }
            }

    /**
     * Use to identify where the result should be delivered to.
     */
    @Poko
    @Parcelize
    public class Key<R : Parcelable> @InternalNavigationTestingApi constructor(
        internal val destinationId: DestinationId<*>,
        @property:InternalNavigationTestingApi
        public val requestKey: String,
    ) : Parcelable {
        private companion object : Parceler<Key<*>> {
            override fun Key<*>.write(parcel: Parcel, flags: Int) {
                parcel.writeSerializable(destinationId.route.java)
                parcel.writeString(requestKey)
            }

            override fun create(parcel: Parcel): Key<*> {
                @Suppress("UNCHECKED_CAST", "DEPRECATION")
                val cls = (parcel.readSerializable() as Class<out BaseRoute>).kotlin
                return Key<Parcelable>(DestinationId(cls), parcel.readString()!!)
            }
        }
    }
}

@Parcelize
private object InitialValue : Parcelable
