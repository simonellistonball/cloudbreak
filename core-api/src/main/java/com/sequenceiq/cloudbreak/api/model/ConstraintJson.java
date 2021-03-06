package com.sequenceiq.cloudbreak.api.model;


import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions.HostGroupModelDescription;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("Constraint")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConstraintJson {

    @ApiModelProperty(HostGroupModelDescription.INSTANCE_GROUP)
    private String instanceGroupName;

    @ApiModelProperty(HostGroupModelDescription.CONSTRAINT_NAME)
    private String constraintTemplateName;

    @NotNull
    @ApiModelProperty(value = HostGroupModelDescription.HOST_COUNT, required = true)
    private Integer hostCount;

    public String getInstanceGroupName() {
        return instanceGroupName;
    }

    public void setInstanceGroupName(String instanceGroupName) {
        this.instanceGroupName = instanceGroupName;
    }

    public String getConstraintTemplateName() {
        return constraintTemplateName;
    }

    public void setConstraintTemplateName(String constraintTemplateName) {
        this.constraintTemplateName = constraintTemplateName;
    }

    public Integer getHostCount() {
        return hostCount;
    }

    public void setHostCount(Integer hostCount) {
        this.hostCount = hostCount;
    }
}
