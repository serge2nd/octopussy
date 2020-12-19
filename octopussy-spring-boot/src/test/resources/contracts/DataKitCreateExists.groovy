import org.springframework.cloud.contract.spec.Contract
import static org.springframework.cloud.contract.spec.internal.HttpMethods.POST
import static org.springframework.http.HttpStatus.BAD_REQUEST

Contract.make {
    description 'must provide error info if data kit already exists'
    request {
        method POST
        url '/dataKits'
        body(
            kitId: 'db3000',
            properties: [
                abc: 'xyz'
            ]
        )
    }
    response {
        status BAD_REQUEST.value()
        body(
            url     : regex('.*/dataKits'),
            method  : POST,
            status  : "${BAD_REQUEST.value()} ${BAD_REQUEST.reasonPhrase}" as String,
            code    : 'DATA_KIT_EXISTS:db3000',
            messages: ['err0']
        )
        bodyMatchers {
            jsonPath('$.messages', byCommand($/assertThat((Iterable)$$it).as("$.messages")
                .allElementsMatch(".*\\S+.*")/$))
        }
    }
}