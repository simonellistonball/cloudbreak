package com.sequenceiq.cloudbreak.controller.json;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel("SecurityRule")
public class SecurityRuleJson implements JsonEntity {

    @ApiModelProperty(required = true)
    private String subnet;

    public SecurityRuleJson() {
    }

    public SecurityRuleJson(String subnet) {
        this.subnet = subnet;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }
}
