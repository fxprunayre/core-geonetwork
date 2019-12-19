package org.fao.geonet.api.processing;

import jeeves.server.context.ServiceContext;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import org.fao.geonet.api.API;
import org.fao.geonet.api.records.editing.InspireValidatorUtils;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.springframework.context.ApplicationContext;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;
import org.springframework.transaction.TransactionStatus;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.ByteArrayInputStream;
import java.util.*;

import static jeeves.transaction.TransactionManager.CommitBehavior.ALWAYS_COMMIT;
import static jeeves.transaction.TransactionManager.TransactionRequirement.CREATE_NEW;

@ManagedResource()
public class MInspireEtfValidateProcess implements SelfNaming {

    private final ApplicationContext appContext;
    private ServiceContext serviceContext;
    private String URL;

    private ObjectName probeName;
    private int metadataToAnalyseCount = -1;
    private int metadataAnalysed = 0;
    private int metadataNotInspire = 0;
    private int metadataNotAllowed = 0;
    private int metadataAnalysedInError = 0;
    private long deleteAllDate = Long.MAX_VALUE;
    private long analyseMdDate = Long.MAX_VALUE;


    @ManagedAttribute
    public int getMetadataToAnalyseCount() {
        return metadataToAnalyseCount;
    }


    @ManagedAttribute
    public int getMetadataAnalysed() {
        return metadataAnalysed;
    }

    @ManagedAttribute
    public int getMetadataNotInspire() {
        return metadataNotInspire;
    }

    @ManagedAttribute
    public int getMetadataNotAllowed() {
        return metadataNotAllowed;
    }

    @ManagedAttribute
    public int getMetadataAnalysedInError() {
        return metadataAnalysedInError;
    }

    @ManagedAttribute
    public long getDeleteAllDate() {
        return deleteAllDate;
    }

    @ManagedAttribute
    public long getAnalyseMdDate() {
        return analyseMdDate;
    }


    @ManagedAttribute
    public ObjectName getObjectName() {
        return this.probeName;
    }

    public MInspireEtfValidateProcess(String URL,
                                      ServiceContext serviceContext, ApplicationContext appContext) {
        this.URL = URL;
        this.serviceContext = serviceContext;
        this.appContext = appContext;

        try {
            this.probeName = new ObjectName(String.format("geonetwork:name=batch-etf-inspire,idx=%s", this.hashCode()));
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    public void deleteAll() {
        runInNewTransaction("minspireetfvalidate-deleteall", new TransactionTask<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                deleteAllDate = System.currentTimeMillis();
                //urlAnalyser.deleteAll();
                return null;
            }
        });
    }

    public void processMetadata(Set<String> uuids) throws Exception {
        IMetadataUtils metadataRepository = appContext.getBean(IMetadataUtils.class);
        MetadataValidationRepository metadataValidationRepository = appContext.getBean(MetadataValidationRepository.class);
        AccessManager accessManager = appContext.getBean(AccessManager.class);
        InspireValidatorUtils inspireValidatorUtils = appContext.getBean(InspireValidatorUtils.class);
        SchemaManager schemaManager = appContext.getBean(SchemaManager.class);
        DataManager dataManager = appContext.getBean(DataManager.class);
        IMetadataSchemaUtils metadataSchemaUtils = appContext.getBean(IMetadataSchemaUtils.class);

        metadataToAnalyseCount = uuids.size();
        analyseMdDate = System.currentTimeMillis();

        for (String uuid : uuids) {
            if (!metadataRepository.existsMetadataUuid(uuid)) {
                metadataAnalysed++;
                metadataNotAllowed++;
                continue;
            }

            for (AbstractMetadata record : metadataRepository.findAllByUuid(uuid)) {
                try {
                    if (!accessManager.canEdit(serviceContext, String.valueOf(record.getId()))) {
                        metadataAnalysed++;
                        metadataNotAllowed++;
                    } else {
                        runInNewTransaction("minspireetfvalidate-process-metadata", new TransactionTask<Object>() {
                            @Override
                            public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                                // Evaluate test conditions for INSPIRE test suites to apply to the metadata
                                Map<String, String> testsuiteConditions =
                                    inspireValidatorUtils.calculateTestsuitesToApply(record.getDataInfo().getSchemaId(), metadataSchemaUtils);

                                boolean reindexMetadata = false;

                                try {
                                    boolean inspireMetadata = false;

                                    for (Map.Entry<String, String> entry : testsuiteConditions.entrySet()) {
                                        boolean applyCondition = false;
                                        try {
                                            applyCondition = Xml.selectBoolean(record.getXmlData(false),
                                                entry.getValue(),
                                                schemaManager.getSchema(record.getDataInfo().getSchemaId()).getNamespaces());
                                        } catch (Exception ex) {
                                            Log.error(API.LOG_MODULE_NAME, String.format("Error checking INSPIRE rule %s to apply to metadata: %s",
                                                entry.getKey(), record.getUuid()), ex);
                                        }

                                        if (applyCondition) {
                                            String testId = inspireValidatorUtils.submitFile(serviceContext, URL,
                                                new ByteArrayInputStream(record.getData().getBytes()), entry.getKey(), record.getUuid());

                                            inspireValidatorUtils.waitUntilReady(serviceContext, URL, testId);

                                            String reportUrl = inspireValidatorUtils.getReportUrl(URL, testId);
                                            String reportXmlUrl = inspireValidatorUtils.getReportUrlXML(URL, testId);
                                            String reportXml = inspireValidatorUtils.retrieveReport(serviceContext, reportXmlUrl);

                                            String validationStatus = inspireValidatorUtils.isPassed(serviceContext, URL, testId);

                                            MetadataValidationStatus metadataValidationStatus =
                                                inspireValidatorUtils.calculateValidationStatus(validationStatus);

                                            MetadataValidation metadataValidation = new MetadataValidation()
                                                .setId(new MetadataValidationId(record.getId(), "inspire"))
                                                .setStatus(metadataValidationStatus).setRequired(false)
                                                .setReportUrl(reportUrl).setReportContent(reportXml);

                                            metadataValidationRepository.save(metadataValidation);

                                            //new RecordValidationTriggeredEvent(record.getId(),
                                            //    ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(),
                                            //    metadataValidation.getStatus().getCode()).publish(appContext);

                                            reindexMetadata = true;
                                            inspireMetadata = true;
                                        }
                                    }


                                    if (!inspireMetadata) {
                                        metadataNotInspire++;

                                        MetadataValidation metadataValidation = new MetadataValidation()
                                            .setId(new MetadataValidationId(record.getId(), "inspire"))
                                            .setStatus(MetadataValidationStatus.DOES_NOT_APPLY).setRequired(false);

                                        metadataValidationRepository.save(metadataValidation);

                                        //new RecordValidationTriggeredEvent(record.getId(),
                                        //    ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(),
                                        //    metadataValidation.getStatus().getCode()).publish(appContext);

                                        reindexMetadata = true;
                                    }

                                    if (reindexMetadata) {
                                        dataManager.indexMetadata(new ArrayList<>(Arrays.asList(record.getId() + "")));
                                    }

                                } catch (Exception ex) {
                                    metadataAnalysedInError++;
                                    Log.error(API.LOG_MODULE_NAME,
                                        String.format("Error validating metadata %s in INSPIRE validator: %s",
                                            record.getUuid(), ex.getMessage()), ex);
                                }

                                metadataAnalysed++;

                                return null;
                            }
                        });
                    }

                } catch (Exception ex) {
                    metadataAnalysedInError++;
                    Log.error(API.LOG_MODULE_NAME,
                        String.format("Error validating metadata %s in INSPIRE validator: %s",
                            record.getUuid(), ex.getMessage()), ex);
                }


            }
        }
    }

    private final void runInNewTransaction(String name, TransactionTask<Object> transactionTask) {
        TransactionManager.runInTransaction(name, appContext, CREATE_NEW,  ALWAYS_COMMIT, false, transactionTask);
    }


}
