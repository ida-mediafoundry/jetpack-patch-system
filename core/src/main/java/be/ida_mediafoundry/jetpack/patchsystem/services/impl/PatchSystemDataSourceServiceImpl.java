package be.ida_mediafoundry.jetpack.patchsystem.services.impl;

import be.ida_mediafoundry.jetpack.patchsystem.models.PatchFileWithResultResource;
import be.ida_mediafoundry.jetpack.patchsystem.ondeploy.services.OnDeployScriptSystemService;
import be.ida_mediafoundry.jetpack.patchsystem.services.PatchSystemDataSourceService;
import be.ida_mediafoundry.jetpack.patchsystem.groovy.services.GroovyPatchSystemService;
import com.adobe.granite.ui.components.ComponentHelper;
import com.adobe.granite.ui.components.Config;
import com.adobe.granite.ui.components.ExpressionHelper;
import com.adobe.granite.ui.components.PagingIterator;
import com.adobe.granite.ui.components.ds.AbstractDataSource;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.iterators.TransformIterator;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceWrapper;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component(
        name = "Jetpack - Patch System DataSource Service",
        service = PatchSystemDataSourceService.class)
public class PatchSystemDataSourceServiceImpl implements PatchSystemDataSourceService {

    private static final Logger LOG = LoggerFactory.getLogger(PatchSystemDataSourceService.class);

    @Reference(cardinality = ReferenceCardinality.OPTIONAL,
            policyOption = ReferencePolicyOption.GREEDY)
    private GroovyPatchSystemService groovyPatchSystemService;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL,
            policyOption = ReferencePolicyOption.GREEDY)
    private OnDeployScriptSystemService onDeployScriptSystemService;

    @Override
    public DataSource getDataSource(HttpServletRequest request, Object cmp, Resource resource) {
        ExpressionHelper ex = ((ComponentHelper)cmp).getExpressionHelper();
        Config dsCfg = new Config(resource.getChild(Config.DATASOURCE));

        final String itemRT = dsCfg.get("itemResourceType", String.class);
        final Integer offset = ex.get(dsCfg.get("offset", String.class), Integer.class);
        final Integer limit = ex.get(dsCfg.get("limit", String.class), Integer.class);

        ResourceResolver resourceResolver = resource.getResourceResolver();

        try {
            final List<PatchFileWithResultResource> patchResources = getPatches(resourceResolver);
            final Iterator<PatchFileWithResultResource> iterator = patchResources.iterator();

            @SuppressWarnings("unchecked")
            DataSource datasource = new AbstractDataSource() {
                public Iterator<Resource> iterator() {
                    Iterator<PatchFileWithResultResource> it = new PagingIterator<>(iterator, offset, limit);

                    return new TransformIterator(it, new Transformer() {
                        public Object transform(Object o) {
                            PatchFileWithResultResource r = ((PatchFileWithResultResource) o);

                            return new ResourceWrapper(r) {
                                public String getResourceType() {
                                    return itemRT;
                                }
                            };
                        }
                    });
                }
            };

            return datasource;
        } catch (Exception e) {
            LOG.error("Error while reading patches list. " + e);
        }

        return EmptyDataSource.instance();
    }

    private List<PatchFileWithResultResource> getPatches(ResourceResolver resourceResolver) {
        List<PatchFileWithResultResource> patches = new ArrayList<>();

        if (groovyPatchSystemService != null) {
            patches.addAll(groovyPatchSystemService.getPatches(resourceResolver));
        }
        if (onDeployScriptSystemService != null) {
            patches.addAll(onDeployScriptSystemService.getPatches(resourceResolver));
        }

        return patches;
    }
}
