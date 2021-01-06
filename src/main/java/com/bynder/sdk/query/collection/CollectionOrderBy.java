/*
 * Copyright (c) 2017 Bynder B.V. All rights reserved.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */
package com.bynder.sdk.query.collection;

/**
 * Enum to represent the different types of ordering for collections.
 */
public enum CollectionOrderBy {

    DATE_CREATED_ASC("dateCreated asc"),
    DATE_CREATED_DESC("dateCreated desc"),
    NAME_ASC("name asc"),
    NAME_DESC("name desc");

    private final String orderType;

    CollectionOrderBy(final String orderType) {
        this.orderType = orderType;
    }

    @Override
    public String toString() {
        return orderType;
    }

}
