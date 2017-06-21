package io.sphere.sdk.annotations.processors.generators;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Abstract base class for implementing javapoet based generators - to generate a single class.
 */
abstract class AbstractGenerator extends BaseAbstractGenerator {

    AbstractGenerator(final Elements elements, final Types types) {
        super(elements, types);
    }

    /**
     * Generates code for the given annotated type element.
     *
     * @param annotatedTypeElement the annotated type element
     * @return the java file to write
     */
    public final JavaFile generate(final TypeElement annotatedTypeElement) {
        final TypeSpec typeSpec = generateType(annotatedTypeElement);

        final JavaFile javaFile = JavaFile.builder(getPackageName(annotatedTypeElement), typeSpec)
                .build();

        return javaFile;
    }

    public abstract TypeSpec generateType(final TypeElement annotatedTypeElement);

}