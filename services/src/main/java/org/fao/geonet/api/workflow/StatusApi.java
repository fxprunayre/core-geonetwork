/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.workflow;

import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.HarvestHistory_;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatus_;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.metadata.StatusActions;
import org.fao.geonet.kernel.metadata.StatusActionsFactory;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.StatusValueRepository;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jeeves.server.context.ServiceContext;

import javax.servlet.http.HttpServletRequest;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;

@RequestMapping(value = {
    "/api",
    "/api/" + API.VERSION_0_1
})
@Api(value = "workflow",
    tags = "workflow",
    description = "Workflow & tasks operations")
@Controller("status")
public class StatusApi {

    @Autowired
    LanguageUtils languageUtils;


    @ApiOperation(
        value = "Get task types",
        notes = "",
        nickname = "getTaskTypes")
    @RequestMapping(
            value = "/workflow/tasks/steps",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<StatusValue> getTaskTypes(HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        return context.getBean(StatusValueRepository.class).findAll();
    }


    @ApiOperation(
        value = "Get all tasks",
        notes = "",
        nickname = "getTasks")
    @RequestMapping(
        value = "/workflow/tasks",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<MetadataStatus> getAllTasks(
        @ApiParam(
            value = "Index of the first element returned.",
            required = false,
            defaultValue = "0")
        @RequestParam(
            defaultValue = "0",
            required = false
        )
        int from,
        @ApiParam(
            value = "Number of elements to return.",
            required = false,
            defaultValue = "20")
        @RequestParam(
            defaultValue = "20",
            required = false
        )
        int size,
        @ApiParam(
            value = "Get tasks related to records.",
            required = false)
        @RequestParam(
            required = false
        )
        List<String> record,
        @ApiParam(
            value = "Get tasks assigned to a user.",
            required = false)
        @RequestParam(
            required = false
        )
        List<Integer> owner,
        @ApiParam(
            value = "Get tasks created by a user.",
            required = false)
        @RequestParam(
            required = false
        )
        List<Integer> author,
        @ApiParam(
            value = "Get tasks due between to dates.",
            required = false)
        @RequestParam(
            required = false
        )
        String dateRange,
        HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        return searchTasks(from, size);
    }

    public List<MetadataStatus> searchTasks(int from, int size) {

        // TODO add filtering capacity
        MetadataStatusRepository metadataStatusRepository =
            ApplicationContextHolder.get().getBean(MetadataStatusRepository.class);

        Pageable pageRequest = new PageRequest(from, size,
            new Sort(Sort.Direction.DESC,
                SortUtils.createPath(MetadataStatus_.dueDate)));
        final Page<MetadataStatus> page = metadataStatusRepository.findAll(pageRequest);
//        final Page<HarvestHistory> page = metadataStatusRepository.findAll(spec, pageRequest);
        if (page.hasContent()) {
            return page.getContent();
        }
        return null;
    }

    @ApiOperation(
        value = "Add a new record task",
        notes = "",
        nickname = "tasks")
    @RequestMapping(value = "/records/{metadataUuid}/tasks",
        method = RequestMethod.PUT
    )
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Task added."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void status(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Status",
            required = true
        )
        // TODO: RequestBody could be more appropriate ?
        @RequestParam(
            required = true
        )
            MetadataStatus status,
        HttpServletRequest request
    )
        throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        ServiceContext context = ApiUtils.createServiceContext(request, locale.getISO3Language());


        AccessManager am = appContext.getBean(AccessManager.class);
        //--- only allow the owner of the record to set its status
        if (!am.isOwner(context, String.valueOf(metadata.getId()))) {
            throw new SecurityException(String.format(
                "Only the owner of the metadata can set the status. User is not the owner of the metadata"
            ));
        }

        ISODate changeDate = new ISODate();

        //--- use StatusActionsFactory and StatusActions class to
        //--- change status and carry out behaviours for status changes
        StatusActionsFactory saf = appContext.getBean(StatusActionsFactory.class);

        StatusActions sa = saf.createStatusActions(context);

        Set<Integer> metadataIds = new HashSet<Integer>();
        metadataIds.add(metadata.getId());

        // TODO: Checks
        // Task id is a valid task type
        // Task does not exist already
        sa.statusChange(
            status.getId() + "", metadataIds, status.getOwnerId(), changeDate, status.getDueDate(),
            status.getChangeMessage(), status.getTargetSection());

        //--- reindex metadata
        DataManager dataManager = appContext.getBean(DataManager.class);
        dataManager.indexMetadata(String.valueOf(metadata.getId()), true, null);
    }

//    GET /api/tasks: Rechercher des tâches (avec tri, filtre, pagination)
//    GET /api/users/{userid}/tasks: Récupérer les tâches d'un utilisateur
//    GET /api/records/{uuid}/tasks: Récupérer les tâches d'une fiche
//    PUT /api/records/{uuid}/tasks: Créer une nouvelle tâche
//    POST /api/records/{uuid}/tasks/{taskId}: Mettre à jour une tâche existante
//    PUT /api/records/{uuid}/tasks/{taskId}/transfertTo/{userId}: Déléguer une tâche
//    DELETE /api/records/{uuid}/tasks/{taskId}: Supprimer une tâche existante (et notifie le propriétaire)
}
