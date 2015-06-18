package com.sequenceiq.cloudbreak.repository;

import org.springframework.data.repository.CrudRepository;

import com.sequenceiq.cloudbreak.domain.SecurityRule;

public interface SecurityRuleRepository extends CrudRepository<SecurityRule, Long> {
}
