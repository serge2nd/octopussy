import org.springframework.cloud.contract.spec.Contract

import static org.springframework.cloud.contract.spec.internal.HttpMethods.POST
import static org.springframework.http.HttpStatus.BAD_REQUEST

Contract.make {
    description "must provide error info if update request not valid"
    request {
        method POST
        url "/dataKits/db1000/update"
        body [:]
    }
    response {
        status BAD_REQUEST.value()
        body(
            url    : regex('.*/dataKits/db1000/update'),
            method : POST,
            status : "${BAD_REQUEST.value()} ${BAD_REQUEST.reasonPhrase}" as String,
            code   : 'NOT_VALID:queries',
            messages: ['err0']
        )
        bodyMatchers {
            jsonPath('$.messages', byCommand($/assertThat((Iterable)$$it).as("$.messages")
                .allElementsMatch(".*\\S+.*")/$))
        }
    }
}