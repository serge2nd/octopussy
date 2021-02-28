package ru.serge2nd.octopussy

import groovy.lang.DelegatesTo as D
import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Body
import org.springframework.cloud.contract.spec.internal.MatchingStrategy
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.spec.internal.Response
import org.springframework.cloud.contract.spec.internal.ResponseBodyMatchers
import org.springframework.http.HttpStatus

import static java.util.Collections.emptyMap
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

@CompileStatic
class ContractDslExtensions {
    static final String MSGS_PATH = '$.messages'

    static void $(Contract _, String endpoint, @D(Request) Closure request) {
        _.request request
        def methodAndUrl = endpoint.split(' ')
        _.request.method methodAndUrl[0]
        _.request.url    methodAndUrl[1]
    }

    static void body(Request _, @D(Request) Closure b) {
        b.delegate = _; def body = b.call()
        if (body == null) {
            _.body = new Body(emptyMap())
        } else if (body instanceof Map) {
            _.body = new Body(_.convertObjectsToDslProperties(body as Map))
        } else if (body instanceof List) {
            _.body = new Body(_.convertObjectsToDslProperties(body as List))
        } else if (body instanceof MatchingStrategy) {
            _.body = new Body(body)
        } else {
            _.body = new Body(body)
        }
    }

    static void '200 OK'(Contract _, @D(Response) Closure r) {
        rs _, OK.value(), r
    }
    static void '201 Created'(Contract _, @D(Response) Closure r) {
        rs _, CREATED.value(), r
    }
    static void '204 No Content'(Contract _, @D(Response) Closure r) {
        rs _, NO_CONTENT.value(), r
        _.response.body ''
    }
    static void '400 Bad Request'(Contract _, @D(Response) Closure r) {
        rs _, BAD_REQUEST.value(), r
    }
    static void '404 Not Found'(Contract _, @D(Response) Closure r) {
        rs _, NOT_FOUND.value(), r
    }

    static void notBlankMessages(Response _) {
        def body = _.bodyMatchers ?: new ResponseBodyMatchers()
        body.jsonPath(MSGS_PATH, body.byCommand($/
            assertThat((Iterable<String>)$$it)
                .as("$MSGS_PATH")
                .isNotEmpty()
                .allMatch(StringUtils::isNotBlank)/$))
        _.bodyMatchers = body
    }

    static getTitle(Integer i) { i + ' ' + HttpStatus.valueOf(i).reasonPhrase }

    private static rs(Contract _, int status, Closure response) {
        _.response response
        _.response.status status
    }
}
