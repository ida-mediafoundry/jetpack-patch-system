package be.ida_mediafoundry.jetpack.patchsystem.ondeploy.models;

import be.ida_mediafoundry.jetpack.carve.annotations.CarveId;
import be.ida_mediafoundry.jetpack.carve.annotations.CarveModel;
import be.ida_mediafoundry.jetpack.carve.manager.pathpolicy.providers.SimplePathPolicyProvider;
import be.ida_mediafoundry.jetpack.patchsystem.models.PatchResult;
import be.ida_mediafoundry.jetpack.patchsystem.models.PatchStatus;
import be.ida_mediafoundry.jetpack.patchsystem.utils.DateUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Calendar;

@CarveModel(pathPolicyProvider = SimplePathPolicyProvider.class, location = "/var/acs-commons/on-deploy-scripts-status")
@Model(adaptables = Resource.class)
public class OnDeployPatchResult implements PatchResult {

    private static final String FAIL = "fail";

    @CarveId
    @Inject
    @Optional
    private String id;

    @Inject
    private String status;

    @Inject
    private Calendar startDate;

    @Inject
    @Optional
    private Calendar endDate;

    @Inject
    @Optional
    private String output;

    private String runningTime;

    @PostConstruct
    protected void initModel() {
        if (FAIL.equals(this.status)) {
            this.status = PatchStatus.ERROR.displayName();
        }
        this.status = status.toUpperCase();

        this.runningTime = DateUtils.formattedRunningTime(this);
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public String getOutput() {
        return output;
    }

    public String getRunningTime() {
        return runningTime;
    }
}
