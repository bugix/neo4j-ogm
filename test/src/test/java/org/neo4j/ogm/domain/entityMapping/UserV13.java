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

package org.neo4j.ogm.domain.entityMapping;

/**
 * No method or field annotated.
 *
 * @author Luanne Misquitta
 */
public class UserV13 extends Entity {

    private UserV13 knows;

    public UserV13() {
    }

    public UserV13 getKnows() {
        return knows;
    }

    public void setKnows(UserV13 knows) {
        this.knows = knows;
    }
}
