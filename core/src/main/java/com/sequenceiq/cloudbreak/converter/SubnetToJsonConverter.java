package com.sequenceiq.cloudbreak.converter;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.controller.json.SecurityRuleJson;
import com.sequenceiq.cloudbreak.domain.SecurityRule;

@Component
public class SubnetToJsonConverter extends AbstractConversionServiceAwareConverter<SecurityRule, SecurityRuleJson> {

    @Override
    public SecurityRuleJson convert(SecurityRule entity) {
        return new SecurityRuleJson(entity.getCidr());
    }
}
