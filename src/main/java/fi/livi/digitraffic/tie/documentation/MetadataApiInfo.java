package fi.livi.digitraffic.tie.documentation;

import java.util.Collections;

import fi.livi.digitraffic.tie.service.BuildVersionService;
import fi.livi.digitraffic.tie.service.MessageService;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;

public class MetadataApiInfo extends ApiInfo {

    private final BuildVersionService buildVersionService;
    protected MessageService messageService;

    public MetadataApiInfo(final MessageService messageService, final BuildVersionService buildVersionService) {
        super(null, //title,
              null, //description,
              null, //version,
              null, //termsOfServiceUrl,
              (Contact) null, //contact,
              null, //license,
              null,
              Collections.emptyList()); //vendorExtensions
        this.messageService = messageService;
        this.buildVersionService = buildVersionService;
    }

    @Override
    public String getTitle() {
        return messageService.getMessage("apiInfo.title");
    }

    @Override
    public String getDescription() {
        return messageService.getMessage("apiInfo.description");
    }

    @Override
    public String getVersion() {
            return buildVersionService.getAppFullVersion();
    }

    @Override
    public Contact getContact() {
        return new Contact(
                messageService.getMessage("apiInfo.contact.name"),
                messageService.getMessage("apiInfo.contact.url"),
                messageService.getMessage("apiInfo.contact.mail"));
    }

    @Override
    public String getTermsOfServiceUrl() {
        return messageService.getMessage("apiInfo.terms.of.service.url");
    }


    @Override
    public String getLicense() {
        return messageService.getMessage("apiInfo.licence");
    }

    @Override
    public String getLicenseUrl() {
        return messageService.getMessage("apiInfo.licence.url");
    }

}
