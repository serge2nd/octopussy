import org.springframework.cloud.contract.spec.Contract
import static org.springframework.cloud.contract.spec.internal.HttpMethods.DELETE
import static org.springframework.http.HttpStatus.NOT_FOUND

Contract.make {
    description "must provide error info if data environment not found"
    request {
        method DELETE
        url '/dataEnvironments/db3000'
    }
    response {
        status NOT_FOUND.value()
        body(
            url    : regex('.*/dataEnvironments/db3000'),
            method : DELETE,
            status : "${NOT_FOUND.value()} ${NOT_FOUND.reasonPhrase}" as String,
            code   : 'DATA_ENV_NOT_FOUND',
            message: anyNonBlankString()
        )
    }
}