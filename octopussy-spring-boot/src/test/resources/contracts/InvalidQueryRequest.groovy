import org.springframework.cloud.contract.spec.Contract
import static org.springframework.cloud.contract.spec.internal.HttpMethods.POST
import static org.springframework.http.HttpStatus.BAD_REQUEST

Contract.make {
    description "must provide error info if query request not valid"
    request {
        method POST
        url "/dataEnvironments/db1000/query"
        body [:]
    }
    response {
        status BAD_REQUEST.value()
        body(
            url    : regex('.*/dataEnvironments/db1000/query'),
            method : POST,
            status : "${BAD_REQUEST.value()} ${BAD_REQUEST.reasonPhrase}" as String,
            code   : 'NOT_VALID',
            message: anyNonBlankString()
        )
    }
}