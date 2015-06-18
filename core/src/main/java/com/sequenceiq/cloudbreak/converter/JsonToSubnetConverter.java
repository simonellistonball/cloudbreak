package com.sequenceiq.cloudbreak.converter;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.controller.json.SecurityRuleJson;
import com.sequenceiq.cloudbreak.domain.SecurityRule;

@Component
public class JsonToSubnetConverter extends AbstractConversionServiceAwareConverter<SecurityRuleJson, SecurityRule> {
    @Override
    public SecurityRule convert(SecurityRuleJson json) {
        return new SecurityRule(json.getSubnet());
    }
}
