package com.freeletics.khonshu.text

import android.widget.TextView

/**
 * Set the [textResource] as the [TextView]'s text.
 */
public fun TextView.setText(textResource: TextResource?) {
    text = textResource?.format(context)
}
