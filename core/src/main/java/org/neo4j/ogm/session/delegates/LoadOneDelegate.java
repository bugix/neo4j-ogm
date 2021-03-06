/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */
package org.neo4j.ogm.session.delegates;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.context.GraphEntityMapper;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.request.GraphModelRequest;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.request.strategy.QueryStatements;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class LoadOneDelegate {

    private static final Logger logger = LoggerFactory.getLogger(LoadOneDelegate.class);

    private Neo4jSession session;

    public LoadOneDelegate(Neo4jSession session) {
        this.session = session;
    }

    public <T, ID extends Serializable> T load(Class<T> type, ID id) {
        return load(type, id, 1);
    }

    public <T, ID extends Serializable> T load(Class<T> type, ID id, int depth) {

        ClassInfo classInfo = session.metaData().classInfo(type.getName());
        if (classInfo == null) {
            throw new IllegalArgumentException(type + " is not a managed entity.");
        }
        final FieldInfo primaryIndexField = classInfo.primaryIndexField();
        if (primaryIndexField != null && !primaryIndexField.isTypeOf(id.getClass())) {
            throw new IllegalArgumentException("Supplied id does not match primary index type on supplied class " + type.getName());
        }

        if (primaryIndexField == null && !(id instanceof Long)) {
            throw new IllegalArgumentException("Supplied id must be of type Long (native graph id) when supplied class "
                    + "does not have primary id" + type.getName());
        }

        QueryStatements<ID> queryStatements = session.queryStatementsFor(type, depth);
        String entityType = session.entityType(type.getName());
        if (entityType == null) {
            logger.warn("Unable to find database label for entity " + type.getName()
                + " : no results will be returned. Make sure the class is registered, "
                + "and not abstract without @NodeEntity annotation");
        }
        PagingAndSortingQuery qry = queryStatements.findOneByType(entityType, id, depth);

        GraphModelRequest request = new DefaultGraphModelRequest(qry.getStatement(), qry.getParameters());
        try (Response<GraphModel> response = session.requestHandler().execute(request)) {
            new GraphEntityMapper(session.metaData(), session.context()).map(type, response);
            return lookup(type, id);
        }
    }

    private <T, U> T lookup(Class<T> type, U id) {
        Object ref;
        ClassInfo typeInfo = session.metaData().classInfo(type.getName());

        FieldInfo primaryIndex = typeInfo.primaryIndexField();
        if (typeInfo.annotationsInfo().get(RelationshipEntity.class) == null) {
            if (primaryIndex == null) {
                ref = session.context().getNodeEntity((Long) id);
            } else {
                ref = session.context().getNodeEntityById(typeInfo, id);
            }
        } else {
            if (primaryIndex == null) {
                // Coercing to Long. identityField.convertedType() yields no parametrised type to call cast() with.
                // But we know this will always be Long.
                ref = session.context().getRelationshipEntity((Long) id);
            } else {
                ref = session.context().getRelationshipEntityById(typeInfo, id);
            }
        }
        try {
            return type.cast(ref);
        } catch (ClassCastException cce) {
            logger.warn("Could not cast entity {} for id {} to {}", ref, id, type);
            return null;
        }
    }

}
