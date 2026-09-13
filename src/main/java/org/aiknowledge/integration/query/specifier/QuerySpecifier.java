package org.aiknowledge.integration.query.specifier;

import org.aiknowledge.integration.query.model.QuerySpec;

public interface QuerySpecifier {
    QuerySpec classify(String query);
}
