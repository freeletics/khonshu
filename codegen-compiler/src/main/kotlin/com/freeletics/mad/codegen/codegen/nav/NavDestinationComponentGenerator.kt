package com.freeletics.mad.codegen.codegen.nav

import com.freeletics.mad.codegen.BaseData
import com.freeletics.mad.codegen.codegen.Generator
import com.freeletics.mad.codegen.codegen.util.contributesToAnnotation
import com.freeletics.mad.codegen.codegen.util.destinationComponent
import com.freeletics.mad.codegen.codegen.util.optInAnnotation
import com.squareup.kotlinpoet.TypeSpec

internal class NavDestinationComponentGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {

    fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder("Mad${data.baseName}NavDestinationComponent")
            .addAnnotation(contributesToAnnotation(data.navigation!!.destinationScope))
            .addAnnotation(optInAnnotation())
            .addSuperinterface(destinationComponent)
            .build()
    }
}
