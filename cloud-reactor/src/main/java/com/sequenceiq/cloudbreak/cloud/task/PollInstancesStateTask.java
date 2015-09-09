package com.sequenceiq.cloudbreak.cloud.task;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.sequenceiq.cloudbreak.cloud.InstanceConnector;
import com.sequenceiq.cloudbreak.cloud.event.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.event.instance.InstancesStatusResult;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmInstanceStatus;
import com.sequenceiq.cloudbreak.cloud.model.InstanceStatus;

public class PollInstancesStateTask extends PollTask<InstancesStatusResult> {

    private List<CloudInstance> instances;
    private InstanceConnector instanceConnector;
    private Set<InstanceStatus> completedStatuses;

    public PollInstancesStateTask(AuthenticatedContext authenticatedContext, InstanceConnector instanceConnector, List<CloudInstance> instances) {
        this(authenticatedContext, instanceConnector, instances, Sets.<InstanceStatus>newHashSet());
    }

    public PollInstancesStateTask(AuthenticatedContext authenticatedContext, InstanceConnector instanceConnector, List<CloudInstance> instances,
            Set<InstanceStatus> completedStatuses) {
        super(authenticatedContext);
        this.instances = instances;
        this.instanceConnector = instanceConnector;
        this.completedStatuses = completedStatuses;
    }

    @Override
    public InstancesStatusResult call() throws Exception {
        List<CloudVmInstanceStatus> instanceStatuses = instanceConnector.check(getAuthenticatedContext(), instances);
        return new InstancesStatusResult(getAuthenticatedContext().getCloudContext(), instanceStatuses);
    }

    @Override
    public boolean completed(InstancesStatusResult instancesStatusResult) {
        for (CloudVmInstanceStatus result : instancesStatusResult.getResults()) {
            if (result.getStatus().isTransient() || (!completedStatuses.isEmpty() && !completedStatuses.contains(result.getStatus()))) {
                return false;
            }
        }
        return true;
    }
}
