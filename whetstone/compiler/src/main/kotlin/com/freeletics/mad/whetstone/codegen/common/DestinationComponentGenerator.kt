package com.freeletics.mad.whetstone.codegen.common

import com.freeletics.mad.whetstone.CommonData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.util.contributesToAnnotation
import com.freeletics.mad.whetstone.codegen.util.destinationComponent
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.squareup.kotlinpoet.TypeSpec


internal class DestinationComponentGenerator(
    override val data: CommonData,
) : Generator<CommonData>() {

    fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder("NavEntry${data.baseName}DestinationComponent")
            .addAnnotation(contributesToAnnotation(data.navigation!!.destinationScope))
            .addAnnotation(optInAnnotation())
            .addSuperinterface(destinationComponent)
            .build()
    }
}
