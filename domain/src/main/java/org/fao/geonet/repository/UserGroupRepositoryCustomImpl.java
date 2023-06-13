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

/**
 *
 */
package org.fao.geonet.repository;

import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.fao.geonet.domain.UserGroupId_;
import org.fao.geonet.domain.UserGroup_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;

import java.util.Collection;
import java.util.List;

/**
 * Implementation object for methods in {@link UserGroupRepositoryCustom}.
 *
 * @author Jesse
 */
public class UserGroupRepositoryCustomImpl implements UserGroupRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;

    @Override
    public List<Integer> findGroupIds(Specification<UserGroup> spec) {
        return findIdsBy(spec, UserGroupId_.groupId);

    }

    @Override
    public List<Integer> findUserIds(Specification<UserGroup> spec) {
        return findIdsBy(spec, UserGroupId_.userId);
    }

    @Override
    @Transactional
    public int deleteAllByIdAttribute(SingularAttribute<UserGroupId, Integer> idAttribute, Collection<Integer> ids) {
        String userIdPath = SortUtils.createPath(UserGroup_.id, idAttribute);

        StringBuilder idString = new StringBuilder();

        for (Integer id : ids) {
            if (idString.length() > 0) {
                idString.append(",");
            }
            idString.append(id);
        }
        final String qlString = "DELETE FROM " + UserGroup.class.getSimpleName() + " WHERE " + userIdPath + " IN (" + idString + ")";
        final int deleted = _entityManager.createQuery(qlString).executeUpdate();

        _entityManager.flush();
        _entityManager.clear();

        return deleted;
    }

    private List<Integer> findIdsBy(Specification<UserGroup> spec, SingularAttribute<UserGroupId, Integer> groupId) {
        CriteriaBuilder builder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = builder.createQuery(Integer.class);
        Root<UserGroup> from = query.from(UserGroup.class);
        query.select(from.get(UserGroup_.id).get(groupId));
        Predicate predicate = spec.toPredicate(from, query, builder);
        query.where(predicate);
        query.distinct(true);
        return _entityManager.createQuery(query).getResultList();
    }

}
