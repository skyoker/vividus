/*
 * Copyright 2019-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.http.validation;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.HttpStatus;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.softassert.ISoftAssert;

public class ResourceValidator<T extends AbstractResourceValidation<T>>
{
    private static final Set<Integer> NOT_ALLOWED_HEAD_STATUS_CODES = Set.of(
            // twitter.com for HEAD requests returns response with 403 status code
            HttpStatus.SC_FORBIDDEN,
            HttpStatus.SC_NOT_FOUND,
            HttpStatus.SC_METHOD_NOT_ALLOWED,
            HttpStatus.SC_NOT_IMPLEMENTED,
            HttpStatus.SC_SERVICE_UNAVAILABLE
    );

    private static final String EXCEPTION_MESSAGE = "Exception occured during check of: %s";

    private final IHttpClient httpClient;

    private final ISoftAssert softAssert;

    private final Set<Integer> allowedStatusCodes = Set.of(HttpStatus.SC_OK);

    private final Map<URI, T> cache = new ConcurrentHashMap<>();
    private boolean publishResponseBody;

    public ResourceValidator(IHttpClient httpClient, ISoftAssert softAssert)
    {
        this.httpClient = httpClient;
        this.softAssert = softAssert;
    }

    public T perform(T resourceValidation)
    {
        return cache.compute(resourceValidation.getUriOrError().getLeft(), (uri, rv) -> Optional.ofNullable(rv)
                .map(r -> {
                    T cachedResult = r.copy();
                    cachedResult.setCheckStatus(r.getCheckStatus());

                    switch (r.getCheckStatus())
                    {
                        case FAILED -> assertForStatusCode(uri, r.getStatusCode().getAsInt());
                        case BROKEN -> softAssert.recordFailedAssertion(EXCEPTION_MESSAGE.formatted(uri)
                                + System.lineSeparator() + r.getUriOrError().getRight());
                        default -> cachedResult.setCheckStatus(CheckStatus.SKIPPED);
                    }

                    return cachedResult;
                }).orElseGet(() -> {
                    validateResource(uri, resourceValidation);
                    return resourceValidation;
                })
        );
    }

    private void validateResource(URI uri, T resourceValidation)
    {
        try
        {
            int statusCode = httpClient.doHttpHead(uri).getStatusCode();
            HttpResponse response = null;
            if (NOT_ALLOWED_HEAD_STATUS_CODES.contains(statusCode))
            {
                response = httpClient.doHttpGet(uri);
                statusCode = response.getStatusCode();
            }
            resourceValidation.setStatusCode(OptionalInt.of(statusCode));

            CheckStatus checkStatus = assertForStatusCode(uri, statusCode);

            if (publishResponseBody && response != null && checkStatus == CheckStatus.FAILED)
            {
                resourceValidation.setResponseBody(response.getResponseBodyAsString());
            }
            resourceValidation.setCheckStatus(checkStatus);
        }
        catch (IOException e)
        {
            softAssert.recordFailedAssertion(EXCEPTION_MESSAGE.formatted(uri), e);
            resourceValidation.setCheckStatus(CheckStatus.BROKEN);
            resourceValidation.setUriOrError(Pair.of(uri, e.toString()));
        }
    }

    private CheckStatus assertForStatusCode(URI uri, int statusCode)
    {
        return softAssert.assertThat(
                String.format("Status code for %s is %d. expected one of %s", uri, statusCode, allowedStatusCodes),
                statusCode, is(oneOf(allowedStatusCodes.toArray()))) ? CheckStatus.PASSED : CheckStatus.FAILED;
    }

    public void setPublishResponseBody(boolean publishResponseBody)
    {
        this.publishResponseBody = publishResponseBody;
    }
}
