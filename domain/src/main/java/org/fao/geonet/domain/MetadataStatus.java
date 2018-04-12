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

package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.AbstractEntityListenerManager;
import org.jdom.Element;

import javax.persistence.*;

/**
 * An entity that represents a status change of a metadata.
 * <p/>
 *
 * The status tracking is disabled by default. This can be used
 * to tracks the history of user actions on a metadata record (aka workflow).
 * It is composed of a set of steps defined in StatusValue entity.
 *
 *
 * The Id consists of the User, date, metadata and statusvalue of the metadata status change.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "MetadataStatus")
@EntityListeners(MetadataStatus.EntityListener.class)
public class MetadataStatus extends GeonetEntity {
    /**
     * The Root element of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_METADATA_STATUS = "metadataStatus";
    /**
     * One of the child elements of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_STATUS_ID = "statusId";
    /**
     * One of the child elements of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_USER_ID = "userId";
    /**
     * One of the child elements of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_CHANGE_DATE = "changeDate";
    /**
     * One of the child elements of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_CHANGE_MESSAGE = "changeMessage";
    /**
     * One of the child elements of the xml returned by {@link #getAsXml}.
     */
    public static final String EL_NAME = "name";
    private MetadataStatusId id = new MetadataStatusId();
    private String changeMessage;
    private String targetSection;
    private StatusValue statusValue;
    private int _ownerId;
    private ISODate _duedate;
    private ISODate _closeddate;

    /**
     * Get the id object of this metadata status object.
     *
     * @return the id object of this metadata status object.
     */
    @EmbeddedId
    public MetadataStatusId getId() {
        return id;
    }

    /**
     * Set the id object of this metadata status object.
     *
     * @param id the id object of this metadata status object.
     */
    public void setId(MetadataStatusId id) {
        this.id = id;
    }

    /**
     * Get the change message, the message that describes the change in status. It is application
     * specific.
     *
     * @return the change message
     */
    @Column(length = 2048, nullable = true)
    public String getChangeMessage() {
        return changeMessage;
    }

    /**
     * Set the change message, the message that describes the change in status. It is application
     * specific.
     *
     * @param changeMessage the change message
     */
    public void setChangeMessage(String changeMessage) {
        this.changeMessage = changeMessage;
    }



    /**
     * Get the target section. This is a section that has to be covered by
     * the status task. It could be a tab identifier in the editor.
     * Usually a permalink to the editor app.
     *
     * @return the target section
     */
    @Column(length = 2048, nullable = true)
    public String getTargetSection() {
        return targetSection;
    }

    /**
     * Set the target section.
     *
     * @param targetSection the target section to update
     */
    public void setTargetSection(String targetSection) {
        this.targetSection = targetSection;
    }



    /**
     * Get the user who is responsible of doing this status task.
     *
     * @return the user who is responsible of this status task.
     */
    @Column(nullable = true)
    public int getOwnerId() {
        return _ownerId;
    }

    /**
     * Set the user who is responsible of doing this status task.
     *
     * @param userId the user who is responsible of this status task.
     * @return this id object
     */
    public MetadataStatus setOwnerId(int userId) {
        this._ownerId = userId;
        return this;
    }



    /**
     * Get the date of this task status due date in string form.
     *
     * @return the date of this task status due date in string form.
     */
    @AttributeOverride(
        name = "dateAndTime",
        column = @Column(
            name = "dueDate",
            nullable = true,
            length = 30))
    public ISODate getDueDate() {
        return _duedate;
    }

    /**
     * Set the date of this task status due date in string form.
     *
     * @param duedate the date of this task status due date in string form.
     */
    public MetadataStatus setDueDate(ISODate duedate) {
        this._duedate = duedate;
        return this;
    }


    /**
     * Get the date when this task was closed in string form.
     *
     * @return the date when this task was closed in string form.
     */
    @AttributeOverride(
        name = "dateAndTime",
        column = @Column(
            name = "closedDate",
            nullable = true,
            length = 30))
    public ISODate getClosedDate() {
        return _closeddate;
    }

    /**
     * Set the date when this task was closed in string form.
     *
     * @param closeddate the date when this task was closed in string form.
     */
    public MetadataStatus setClosedDate(ISODate closeddate) {
        this._closeddate = closeddate;
        return this;
    }




    @ManyToOne
    @JoinColumn(name = "statusId", nullable = false, insertable = false, updatable = false)
    @MapsId("statusId")
    public StatusValue getStatusValue() {
        return statusValue;
    }

    public void setStatusValue(StatusValue statusValue) {
        this.statusValue = statusValue;
        this.getId().setStatusId(statusValue.getId());
    }

    @Transient
    public Element getAsXml() {
        return new Element(EL_METADATA_STATUS)
            .addContent(new Element(EL_STATUS_ID).setText(String.valueOf(getId().getStatusId())))
            .addContent(new Element(EL_USER_ID).setText(String.valueOf(getId().getUserId())))
            .addContent(new Element(EL_CHANGE_DATE).setText(getId().getChangeDate().getDateAndTime()))
            .addContent(new Element(EL_CHANGE_MESSAGE).setText(getChangeMessage()))
            .addContent(new Element(EL_NAME).setText(getStatusValue().getName()));
    }

    public static class EntityListener extends AbstractEntityListenerManager<MetadataStatus> {
    }
}
