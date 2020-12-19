import org.springframework.cloud.contract.spec.Contract
import static org.springframework.cloud.contract.spec.internal.HttpMethods.POST
import static org.springframework.http.HttpStatus.BAD_REQUEST

Contract.make {
    description 'must provide error info if data kit not valid'
    request {
        method POST
        url '/dataKits'
        body [:]
    }
    response {
        status BAD_REQUEST.value()
        body(
            url    : regex('.*/dataKits'),
            method : POST,
            status : "${BAD_REQUEST.value()} ${BAD_REQUEST.reasonPhrase}" as String,
            code   : 'NOT_VALID:dataKitDefinition',
            messages: ['err0']
        )
        bodyMatchers {
            jsonPath('$.messages', byCommand($/assertThat((Iterable)$$it).as("$.messages")
                .allElementsMatch(".*\\S+.*")/$))
        }
    }
}
