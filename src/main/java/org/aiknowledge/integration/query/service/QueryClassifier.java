package org.aiknowledge.integration.query.service;

import org.aiknowledge.integration.query.model.QuerySpec;

public interface QueryClassifier {
    QuerySpec classify(String query);
}
