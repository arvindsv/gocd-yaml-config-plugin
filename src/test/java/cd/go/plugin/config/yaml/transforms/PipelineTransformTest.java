package cd.go.plugin.config.yaml.transforms;

import cd.go.plugin.config.yaml.JsonObjectMatcher;
import cd.go.plugin.config.yaml.YamlUtils;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Map;

import static cd.go.plugin.config.yaml.TestUtils.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PipelineTransformTest {

    private PipelineTransform parser;
    private MaterialTransform materialTransform;
    private StageTransform stageTransform;
    private EnvironmentVariablesTransform environmentTransform;
    private Yaml yaml;

    @Before
    public void SetUp() {
        materialTransform = mock(MaterialTransform.class);
        stageTransform = mock(StageTransform.class);
        environmentTransform = mock(EnvironmentVariablesTransform.class);
        ParameterTransform parameterTransform = mock(ParameterTransform.class);
        parser = new PipelineTransform(materialTransform, stageTransform, environmentTransform, parameterTransform);
        yaml = new Yaml();
    }

    @Test
    public void shouldTransformSimplePipeline() throws IOException {
        testTransform("simple.pipe");
    }

    @Test
    public void shouldTransformRichPipeline() throws IOException {
        testTransform("rich.pipe");
    }

    @Test
    public void shouldTransformRichPipeline2() throws IOException {
        testTransform("lock_behavior.pipe");
    }

    @Test
    public void shouldTransformAPipelineReferencingATemplate() throws IOException {
        testTransform("template_ref.pipe");
    }

    @Test
    public void shouldInverseTransformPipeline() throws IOException {
        Map<String, Object> mats = new LinkedTreeMap<>();
        mats.put("foo", new LinkedTreeMap<>());
        when(materialTransform.inverseTransform(any(LinkedTreeMap.class))).thenReturn(mats);
        testInverseTransform("export.pipe");
    }

    private void testTransform(String caseFile) throws IOException {
        testTransform(caseFile, caseFile);
    }

    private void testInverseTransform(String caseFile) throws IOException {
        testInverseTransform(caseFile, caseFile);
    }

    private void testTransform(String caseFile, String expectedFile) throws IOException {
        JsonObject expectedObject = (JsonObject) readJsonObject("parts/" + expectedFile + ".json");
        JsonObject jsonObject = parser.transform(readYamlObject("parts/" + caseFile + ".yaml"));
        assertThat(jsonObject, is(new JsonObjectMatcher(expectedObject)));
    }

    private void testInverseTransform(String caseFile, String expectedFile) throws IOException {
        String expectedObject = loadString("parts/" + expectedFile + ".yaml");
        Map<String, Object> actual = parser.inverseTransform(readJsonGson("parts/" + caseFile + ".json"));
        assertYamlEquivalent(expectedObject, YamlUtils.dump(actual));
    }
}
