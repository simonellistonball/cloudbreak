package com.sequenceiq.cloudbreak.service.template;

import static com.sequenceiq.cloudbreak.controller.exception.NotFoundException.notFound;
import static com.sequenceiq.cloudbreak.util.SqlUtil.getProperSqlErrorMessage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.model.ResourceStatus;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUserRole;
import com.sequenceiq.cloudbreak.common.type.APIResourceType;
import com.sequenceiq.cloudbreak.controller.exception.BadRequestException;
import com.sequenceiq.cloudbreak.domain.Template;
import com.sequenceiq.cloudbreak.domain.Topology;
import com.sequenceiq.cloudbreak.domain.organization.Organization;
import com.sequenceiq.cloudbreak.domain.organization.User;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.repository.TemplateRepository;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.util.NameUtil;

@Service
public class TemplateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateService.class);

    private static final String TEMPLATE_NOT_FOUND_MSG = "Template '%s' not found.";

    private static final String TEMPLATE_NOT_FOUND_BY_ID_MSG = "Template not found by id '%d'.";

    @Inject
    private TemplateRepository templateRepository;

    @Inject
    private StackService stackService;

    public Set<Template> retrievePrivateTemplates(IdentityUser user) {
        return templateRepository.findForUser(user.getUserId());
    }

    public Set<Template> retrieveAccountTemplates(IdentityUser user) {
        return user.getRoles().contains(IdentityUserRole.ADMIN) ? templateRepository.findAllInAccount(user.getAccount())
                : templateRepository.findForUser(user.getUserId(), user.getAccount());
    }

    public Template get(Long id) {
        return templateRepository.findById(id).orElseThrow(notFound("Template", id));
    }

    public Template create(String owner, String account, User user, Template template, Organization organization) {
        LOGGER.debug("Creating template: [User: '{}']", user.getUserId());

        template.setOwner(owner);
        template.setAccount(account);
        template.setOrganization(organization);

        Template savedTemplate;
        try {
            savedTemplate = templateRepository.save(template);
        } catch (DataIntegrityViolationException ex) {
            String msg = String.format("Error with resource [%s], %s", APIResourceType.BLUEPRINT, getProperSqlErrorMessage(ex));
            throw new BadRequestException(msg, ex);
        }
        return savedTemplate;
    }

    public void delete(Long templateId, IdentityUser user) {
        Template template = Optional.ofNullable(templateRepository.findByIdInAccount(templateId, user.getAccount()))
                .orElseThrow(notFound("Template", templateId));
        delete(template);
    }

    public Template getPrivateTemplate(String name, IdentityUser user) {
        return Optional.ofNullable(templateRepository.findByNameInUser(name, user.getUserId()))
                .orElseThrow(notFound("Template", name));
    }

    public Template getPublicTemplate(String name, IdentityUser user) {
        return Optional.ofNullable(templateRepository.findOneByName(name, user.getAccount()))
                .orElseThrow(notFound("Template", name));
    }

    public void delete(String templateName, IdentityUser user) {
        Template template = Optional.ofNullable(templateRepository.findByNameInAccount(templateName, user.getAccount(), user.getUserId()))
                .orElseThrow(notFound("Template", templateName));
        delete(template);
    }

    public void delete(Template template) {
        LOGGER.info("Deleting template. {} - {}", new Object[]{template.getId(), template.getName()});
        List<Stack> allStackForTemplate = stackService.getAllForTemplate(template.getId());
        if (allStackForTemplate.isEmpty()) {
            template.setTopology(null);
            if (ResourceStatus.USER_MANAGED.equals(template.getStatus())) {
                templateRepository.delete(template);
            } else {
                template.setName(NameUtil.postfixWithTimestamp(template.getName()));
                template.setStatus(ResourceStatus.DEFAULT_DELETED);
                templateRepository.save(template);
            }
        } else if (isRunningStackReferToTemplate(allStackForTemplate)) {
            throw new BadRequestException(String.format(
                    "There are stacks associated with template '%s'. Please remove these before deleting the template.", template.getName()));
        } else {
            template.setName(NameUtil.postfixWithTimestamp(template.getName()));
            template.setTopology(null);
            template.setDeleted(true);
            if (ResourceStatus.DEFAULT.equals(template.getStatus())) {
                template.setStatus(ResourceStatus.DEFAULT_DELETED);
            }
            templateRepository.save(template);
        }
    }

    private boolean isRunningStackReferToTemplate(Collection<Stack> allStackForTemplate) {
        return allStackForTemplate.stream().anyMatch(s -> !s.isDeleteCompleted());
    }

    public Set<Template> findByTopology(Topology topology) {
        return templateRepository.findByTopology(topology);
    }
}
