package com.freeletics.khonshu.codegen.codegen.nav

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.codegen.Generator
import com.freeletics.khonshu.codegen.codegen.util.contributesToAnnotation
import com.freeletics.khonshu.codegen.codegen.util.destinationComponent
import com.freeletics.khonshu.codegen.codegen.util.optInAnnotation
import com.squareup.kotlinpoet.TypeSpec

internal class NavDestinationComponentGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {

    fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder("Khonshu${data.baseName}NavDestinationComponent")
            .addAnnotation(contributesToAnnotation(data.navigation!!.destinationScope))
            .addAnnotation(optInAnnotation())
            .addSuperinterface(destinationComponent)
            .build()
    }
}
