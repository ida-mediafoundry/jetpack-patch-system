package be.ida.jetpack.patchsystem.services.impl;

import be.ida.jetpack.patchsystem.models.PatchFile;
import be.ida.jetpack.patchsystem.models.PatchFileWithResultResource;
import be.ida.jetpack.patchsystem.models.PatchResult;
import be.ida.jetpack.patchsystem.repositories.PatchFileRepository;
import be.ida.jetpack.patchsystem.repositories.PatchResultRepository;
import com.icfolson.aem.groovy.console.GroovyConsoleService;
import com.icfolson.aem.groovy.console.response.RunScriptResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.servlethelpers.MockSlingHttpServletRequest;
import org.apache.sling.servlethelpers.MockSlingHttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class PatchSystemServiceImplTest {

    @InjectMocks
    private PatchSystemServiceImpl patchSystemService;

    @Mock
    private PatchResultRepository patchResultRepository;
    @Mock
    private PatchFileRepository patchFileRepository;
    @Mock
    private GroovyConsoleService groovyConsoleService;
    @Mock
    private ResourceResolverFactory resourceResolverFactory;

    @Test
    public void test_getPatchesToExecute_2Scripts_alreadyExecuted_notModified() {
        //given
        PatchFile patchFile1 = mock(PatchFile.class);
        given(patchFile1.getMd5()).willReturn("100");
        PatchFile patchFile2 = mock(PatchFile.class);
        given(patchFile2.getMd5()).willReturn("200");
        List<PatchFile> patchFiles = new ArrayList<>();
        patchFiles.add(patchFile1);
        patchFiles.add(patchFile2);

        PatchResult patchResult1 = new PatchResult();
        patchResult1.setMd5("100");

        PatchResult patchResult2 = new PatchResult();
        patchResult2.setMd5("200");

        given(patchResultRepository.getResult(patchFile1)).willReturn(patchResult1);
        given(patchResultRepository.getResult(patchFile2)).willReturn(patchResult2);

        given(patchFileRepository.getPatches()).willReturn(patchFiles);

        //test
        List<PatchFile> patchFilesToExecute = patchSystemService.getPatchesToExecute();

        //check
        assertThat(patchFilesToExecute).isEmpty();
    }

    @Test
    public void test_getPatchesToExecute_2Scripts_alreadyExecuted_1Modified() {
        //given
        PatchFile patchFile1 = mock(PatchFile.class);
        given(patchFile1.getMd5()).willReturn("100");
        PatchFile patchFile2 = mock(PatchFile.class);
        given(patchFile2.getMd5()).willReturn("999");
        List<PatchFile> patchFiles = new ArrayList<>();
        patchFiles.add(patchFile1);
        patchFiles.add(patchFile2);

        PatchResult patchResult1 = new PatchResult();
        patchResult1.setMd5("100");

        PatchResult patchResult2 = new PatchResult();
        patchResult2.setMd5("200");

        given(patchResultRepository.getResult(patchFile1)).willReturn(patchResult1);
        given(patchResultRepository.getResult(patchFile2)).willReturn(patchResult2);

        given(patchFileRepository.getPatches()).willReturn(patchFiles);

        //test
        List<PatchFile> patchFilesToExecute = patchSystemService.getPatchesToExecute();

        //check
        assertThat(patchFilesToExecute).isNotEmpty();
        assertThat(patchFilesToExecute.size()).isEqualTo(1);
        assertThat(patchFilesToExecute.get(0).getMd5()).isEqualTo("999");
    }

    @Test
    public void test_getPatchesToExecute_2Scripts_1Executed_1New() {
        //given
        PatchFile patchFile1 = mock(PatchFile.class);
        given(patchFile1.getMd5()).willReturn("100");
        PatchFile patchFile2 = mock(PatchFile.class);
        given(patchFile2.getMd5()).willReturn("200");
        List<PatchFile> patchFiles = new ArrayList<>();
        patchFiles.add(patchFile1);
        patchFiles.add(patchFile2);

        PatchResult patchResult1 = new PatchResult();
        patchResult1.setMd5("100");

        given(patchResultRepository.getResult(patchFile1)).willReturn(patchResult1);
        given(patchResultRepository.getResult(patchFile2)).willReturn(null);

        given(patchFileRepository.getPatches()).willReturn(patchFiles);

        //test
        List<PatchFile> patchFilesToExecute = patchSystemService.getPatchesToExecute();

        //check
        assertThat(patchFilesToExecute).isNotEmpty();
        assertThat(patchFilesToExecute.size()).isEqualTo(1);
        assertThat(patchFilesToExecute.get(0).getMd5()).isEqualTo("200");
    }

    @Test
    public void testGetPatches() {
        //given
        PatchFile patchFile1 = mock(PatchFile.class);
        given(patchFile1.getMd5()).willReturn("100");
        given(patchFile1.getScriptName()).willReturn("Script 1.groovy");
        given(patchFile1.getProjectName()).willReturn("Project A");
        PatchFile patchFile2 = mock(PatchFile.class);
        given(patchFile2.getMd5()).willReturn("200");
        given(patchFile2.getScriptName()).willReturn("Script 2.groovy");
        given(patchFile2.getProjectName()).willReturn("Project B");
        PatchFile patchFile3 = mock(PatchFile.class);
        given(patchFile3.getScriptName()).willReturn("Script 3.groovy");
        given(patchFile3.getProjectName()).willReturn("Project C");
        List<PatchFile> patchFiles = new ArrayList<>();
        patchFiles.add(patchFile1);
        patchFiles.add(patchFile2);
        patchFiles.add(patchFile3);

        given(patchResultRepository.getResult(patchFile1)).willReturn(createPatchResult("001", "100"));
        given(patchResultRepository.getResult(patchFile2)).willReturn(createPatchResult("002", "999"));
        given(patchResultRepository.getResult(patchFile3)).willReturn(null);

        given(patchFileRepository.getPatches()).willReturn(patchFiles);

        //test
        List<PatchFileWithResultResource> patches = patchSystemService.getPatches(mock(ResourceResolver.class));

        //check
        assertThat(patches).isNotEmpty();
        assertThat(patches.size()).isEqualTo(3);

        //patch 0 = already executed
        assertThat(patches.get(0).getValueMap()).hasSize(7);
        assertThat(patches.get(0).getValueMap().get("status")).isEqualTo("SUCCESS");
        assertThat(patches.get(0).getValueMap().get("projectName")).isEqualTo("Project A");
        assertThat(patches.get(0).getValueMap().get("scriptName")).isEqualTo("Script 1.groovy");

        //patch 1 = already executed, but modified
        assertThat(patches.get(1).getValueMap()).hasSize(7);
        assertThat(patches.get(1).getValueMap().get("status")).isEqualTo("RE-RUN");
        assertThat(patches.get(1).getValueMap().get("projectName")).isEqualTo("Project B");
        assertThat(patches.get(1).getValueMap().get("scriptName")).isEqualTo("Script 2.groovy");

        //patch 2 = new script
        assertThat(patches.get(2).getValueMap()).hasSize(3);
        assertThat(patches.get(2).getValueMap().get("status")).isEqualTo("NEW");
        assertThat(patches.get(2).getValueMap().get("projectName")).isEqualTo("Project C");
        assertThat(patches.get(2).getValueMap().get("scriptName")).isEqualTo("Script 3.groovy");
    }

    @Test
    public void testRunPatch_firstRun_success() throws Exception {
        PatchFile patchFile = mock(PatchFile.class);
        given(patchFile.getMd5()).willReturn("100");
        given(patchFile.getPath()).willReturn("/etc/patch/patchfile.groovy");

        given(patchFileRepository.getPatch("/etc/patch/patchfile.groovy")).willReturn(patchFile);
        PatchResult patchResult = new PatchResult(patchFile.getResultPath(), "RUNNING", Calendar.getInstance());
        patchResult.setMd5(patchFile.getMd5());

        given(patchResultRepository.createResult(patchFile)).willReturn(patchResult);
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        given(resourceResolverFactory.getServiceResourceResolver(any())).willReturn(resourceResolver);

        RunScriptResponse response = new RunScriptResponse("script", "data", "result", "output", null, "3000", "userId");
        given(groovyConsoleService.runScript(any(MockSlingHttpServletRequest.class), any(MockSlingHttpServletResponse.class), eq("/etc/patch/patchfile.groovy"))).willReturn(response);

        //test
        PatchResult patchResultReturned = patchSystemService.runPatch("/etc/patch/patchfile.groovy");

        //check
        assertThat(patchResultReturned).isNotNull();
        assertThat(patchResultReturned.getStatus()).isEqualTo("SUCCESS");
        assertThat(patchResultReturned.getOutput()).isEqualTo("output");
        assertThat(patchResultReturned.getRunningTime()).isEqualTo("3000");
    }

    @Test
    public void testRunPatch_patchSystemNotRunning() throws Exception {
        PatchFile patchFile = mock(PatchFile.class);
        given(patchFile.getMd5()).willReturn("100");

        given(patchFileRepository.getPatch("/etc/patch/patchfile.groovy")).willReturn(patchFile);
        PatchResult patchResult = new PatchResult(patchFile.getResultPath(), "RUNNING", Calendar.getInstance());
        patchResult.setMd5(patchFile.getMd5());

        given(patchResultRepository.createResult(patchFile)).willReturn(patchResult);
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        given(resourceResolverFactory.getServiceResourceResolver(any())).willReturn(resourceResolver);

        patchSystemService.unbindGroovyConsole();

        //test
        PatchResult patchResultReturned = patchSystemService.runPatch("/etc/patch/patchfile.groovy");

        //check
        assertThat(patchResultReturned).isNotNull();
        assertThat(patchResultReturned.getStatus()).isEqualTo("ERROR");
        assertThat(patchResultReturned.getOutput()).isEqualTo("Groovy Console is not installed.");
    }

    @Test
    public void testRunPatch_firstRun_failed() throws Exception {
        PatchFile patchFile = mock(PatchFile.class);
        given(patchFile.getMd5()).willReturn("100");
        given(patchFile.getPath()).willReturn("/etc/patch/patchfile.groovy");

        given(patchFileRepository.getPatch("/etc/patch/patchfile.groovy")).willReturn(patchFile);
        PatchResult patchResult = new PatchResult(patchFile.getResultPath(), "RUNNING", Calendar.getInstance());
        patchResult.setMd5(patchFile.getMd5());

        given(patchResultRepository.createResult(patchFile)).willReturn(patchResult);
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        given(resourceResolverFactory.getServiceResourceResolver(any())).willReturn(resourceResolver);

        RunScriptResponse response = new RunScriptResponse("script", "data", "result", null, "stack trace", "3000", "userId");
        given(groovyConsoleService.runScript(any(MockSlingHttpServletRequest.class), any(MockSlingHttpServletResponse.class), eq("/etc/patch/patchfile.groovy"))).willReturn(response);

        //test
        PatchResult patchResultReturned = patchSystemService.runPatch("/etc/patch/patchfile.groovy");

        //check
        assertThat(patchResultReturned).isNotNull();
        assertThat(patchResultReturned.getStatus()).isEqualTo("ERROR");
        assertThat(patchResultReturned.getOutput()).isEqualTo("stack trace");
        assertThat(patchResultReturned.getRunningTime()).isEqualTo("3000");
    }

    @Test
    public void testRunPatch_firstRun_exception() throws Exception {
        PatchFile patchFile = mock(PatchFile.class);
        given(patchFile.getMd5()).willReturn("100");
        given(patchFile.getPath()).willReturn("/etc/patch/patchfile.groovy");

        given(patchFileRepository.getPatch("/etc/patch/patchfile.groovy")).willReturn(patchFile);
        PatchResult patchResult = new PatchResult(patchFile.getResultPath(), "RUNNING", Calendar.getInstance());
        patchResult.setMd5(patchFile.getMd5());

        given(patchResultRepository.createResult(patchFile)).willReturn(patchResult);
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        given(resourceResolverFactory.getServiceResourceResolver(any())).willReturn(resourceResolver);

        given(groovyConsoleService.runScript(any(MockSlingHttpServletRequest.class), any(MockSlingHttpServletResponse.class), eq("/etc/patch/patchfile.groovy"))).willThrow(NullPointerException.class);

        //test
        PatchResult patchResultReturned = patchSystemService.runPatch("/etc/patch/patchfile.groovy");

        //check
        assertThat(patchResultReturned).isNotNull();
        assertThat(patchResultReturned.getStatus()).isEqualTo("ERROR");
        assertThat(patchResultReturned.getOutput()).isEqualTo("Script Execution error, check log files");
        assertThat(patchResultReturned.getRunningTime()).isNull();
    }

    private static PatchResult createPatchResult(String id, String md5) {
        PatchResult patchResult = new PatchResult(id, "RUNNING", Calendar.getInstance());
        patchResult.setMd5(md5);
        patchResult.setStatus("SUCCESS");
        patchResult.setEndDate(Calendar.getInstance());
        patchResult.setRunningTime("2000");
        patchResult.setOutput("output");
        return patchResult;
    }

}
