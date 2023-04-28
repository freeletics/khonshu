package com.freeletics.mad.whetstone.codegen.nav

import com.freeletics.mad.whetstone.BaseData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.util.contributesToAnnotation
import com.freeletics.mad.whetstone.codegen.util.destinationComponent
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.squareup.kotlinpoet.TypeSpec

internal class DestinationComponentGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {

    fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder("Whetstone${data.baseName}DestinationComponent")
            .addAnnotation(contributesToAnnotation(data.navigation!!.destinationScope))
            .addAnnotation(optInAnnotation())
            .addSuperinterface(destinationComponent)
            .build()
    }
}
