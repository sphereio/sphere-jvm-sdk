package io.sphere.sdk.annotations.processors.generators;

import io.sphere.sdk.annotations.processors.generators.examples.ExampleDraft;
import io.sphere.sdk.annotations.processors.generators.examples.ExampleDraftWithAbstractClass;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ResourceDraftValueGeneratorTest extends AbstractGeneratorTest {

    @Before
    public void setup() {
        generator = new ResourceDraftValueGenerator(compilationRule.getElements());
    }

    @Test
    public void generateExampleResource() throws Exception {
        final String content = generateAsString(ExampleDraft.class);

        assertThat(content).isEqualTo(expectedContent(ExampleDraft.class));
    }

    @Test
    public void generateExampleResourceWithAbstractClass() throws Exception {
        final String content = generateAsString(ExampleDraftWithAbstractClass.class);

        assertThat(content).isEqualTo(expectedContent(ExampleDraftWithAbstractClass.class));
    }

}